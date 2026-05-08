import { ref, computed } from 'vue'
import { cardsApi } from '@/api/cards'
import { useDialog } from '@/composables/useDialog'
import type { PrivateCardDto } from '@/types'

export function useCards() {
  const dialog = useDialog()
  const cards = ref<PrivateCardDto[]>([])
  const loading = ref(false)
  const showAdd = ref(false)
  const addLoading = ref(false)
  const addError = ref('')
  const hoveredId = ref<number | null>(null)
  const focusedField = ref<string | null>(null)
  const currentYear = new Date().getFullYear()

  const form = ref({
    card_number: '',
    card_holder_name: '',
    exp_month: new Date().getMonth() + 1,
    exp_year: currentYear,
    cvv: '',
  })

  const previewNumber = computed(() => {
    const raw = form.value.card_number.replace(/\s/g, '')
    if (!raw) return '•••• •••• •••• ••••'
    const padded = raw.padEnd(16, '•')
    return padded.match(/.{1,4}/g)!.join(' ')
  })

  const previewExpiry = computed(() => {
    const m = String(form.value.exp_month).padStart(2, '0')
    const y = String(form.value.exp_year)
    return `${m}/${y}`
  })

  const previewHolder = computed(() =>
    form.value.card_holder_name.toUpperCase() || 'YOUR NAME',
  )

  const previewBrand = computed(() => detectBrand(form.value.card_number ?? ''))
  const previewBrandClass = computed(() => brandClass(previewBrand.value))

  function detectBrand(number: string): string {
    const n = (number ?? '').replace(/\s/g, '')
    if (/^4/.test(n)) return 'VISA'
    if (/^5[1-5]/.test(n) || /^2[2-7]/.test(n)) return 'MASTERCARD'
    if (/^3[47]/.test(n)) return 'AMEX'
    return 'CARD'
  }

  function brandClass(brand: string | null | undefined) {
    const b = (brand ?? '').toLowerCase()
    if (b === 'visa') return 'brand-visa'
    if (b === 'mastercard') return 'brand-mastercard'
    if (b === 'amex') return 'brand-amex'
    return 'brand-default'
  }

  function formatCardNumber(e: Event) {
    const input = e.target as HTMLInputElement
    let val = input.value.replace(/\D/g, '').substring(0, 16)
    val = val.match(/.{1,4}/g)?.join(' ') ?? val
    form.value.card_number = val
  }

  function openAdd() {
    form.value = {
      card_number: '',
      card_holder_name: '',
      exp_month: new Date().getMonth() + 1,
      exp_year: currentYear,
      cvv: '',
    }
    addError.value = ''
    showAdd.value = true
  }

  async function fetchCards() {
    loading.value = true
    try {
      const { data } = await cardsApi.getMyCards()
      cards.value = data
    } finally {
      loading.value = false
    }
  }

  async function confirmDelete(id: number) {
    const { isConfirmed } = await dialog.confirm({
      title: 'Remove card?',
      text: 'This card will be unlinked from your account.',
      confirmLabel: 'Remove',
    })
    if (!isConfirmed) return
    await cardsApi.delete(id)
    cards.value = cards.value.filter((c) => c.id !== id)
  }

  async function handleAdd() {
    addError.value = ''
    addLoading.value = true
    try {
      const parts = form.value.card_holder_name.trim().split(/\s+/)
      const payload = {
        card_number: form.value.card_number.replace(/\s/g, ''),
        first_name: parts[0] ?? '',
        last_name: parts.slice(1).join(' ') || (parts[0] ?? ''),
        exp_month: form.value.exp_month,
        exp_year: form.value.exp_year,
        cvv: form.value.cvv,
      }
      const { data } = await cardsApi.create(payload)
      cards.value.push(data)
      showAdd.value = false
    } catch (e: any) {
      addError.value = e.response?.data?.message ?? 'Failed to add card.'
    } finally {
      addLoading.value = false
    }
  }

  return {
    cards, loading, showAdd, addLoading, addError, hoveredId, focusedField, currentYear, form,
    previewNumber, previewExpiry, previewHolder, previewBrand, previewBrandClass,
    detectBrand, brandClass, formatCardNumber,
    openAdd, fetchCards, confirmDelete, handleAdd,
  }
}
