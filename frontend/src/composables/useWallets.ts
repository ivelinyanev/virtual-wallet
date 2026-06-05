import { ref } from 'vue'
import { useWalletStore } from '@/stores/wallet'
import { useDialog } from '@/composables/useDialog'
import { cardsApi } from '@/api/cards'
import type { WalletResponse, CardResponse, Currency } from '@/types'

export const currencies = [
  { code: 'EUR' as Currency, symbol: '€', name: 'Euro' },
  { code: 'USD' as Currency, symbol: '$', name: 'US Dollar' },
  { code: 'GBP' as Currency, symbol: '£', name: 'Pound Sterling' },
]

export function useWallets() {
  const walletStore = useWalletStore()
  const dialog = useDialog()

  // Create modal
  const showCreate = ref(false)
  const createForm = ref<{ name: string; currency: Currency }>({ name: '', currency: 'EUR' })
  const createError = ref('')
  const createLoading = ref(false)

  // Top-up modal
  const topUpWallet = ref<WalletResponse | null>(null)
  const topUpAmount = ref(0)
  const topUpCardId = ref<number | null>(null)
  const topUpError = ref('')
  const topUpLoading = ref(false)
  const cards = ref<CardResponse[]>([])

  const quickAmounts = [10, 50, 100, 500]

  function currencySymbol(code: Currency) {
    return currencies.find((c) => c.code === code)?.symbol ?? code
  }

  function cardAccent(currency: Currency) {
    return { eur: currency === 'EUR', usd: currency === 'USD', gbp: currency === 'GBP' }
  }

  function openCreate() {
    createForm.value = { name: '', currency: 'EUR' }
    createError.value = ''
    showCreate.value = true
  }

  async function openTopUp(wallet: WalletResponse) {
    topUpWallet.value = wallet
    topUpAmount.value = 0
    topUpCardId.value = null
    topUpError.value = ''
    if (!cards.value.length) {
      const { data } = await cardsApi.getMyCards()
      cards.value = data
    }
  }

  async function confirmDelete(id: number) {
    const { isConfirmed } = await dialog.confirm({
      title: 'Delete wallet?',
      text: 'This action cannot be undone.',
      confirmLabel: 'Delete',
    })
    if (!isConfirmed) return
    try {
      await walletStore.deleteWallet(id)
    } catch (e: any) {
      await dialog.error('Could not delete wallet', e.response?.data?.message)
    }
  }

  async function handleCreate() {
    createError.value = ''
    createLoading.value = true
    try {
      await walletStore.createWallet(createForm.value)
      showCreate.value = false
    } catch (e: any) {
      createError.value = e.response?.data?.message ?? 'Failed to create wallet.'
    } finally {
      createLoading.value = false
    }
  }

  async function handleTopUp() {
    if (!topUpWallet.value) return
    topUpError.value = ''
    topUpLoading.value = true
    try {
      await walletStore.topUp({ wallet_id: topUpWallet.value.id, card_id: topUpCardId.value!, amount: topUpAmount.value })
      topUpWallet.value = null
    } catch (e: any) {
      topUpError.value = e.response?.data?.message ?? 'Top-up failed.'
    } finally {
      topUpLoading.value = false
    }
  }

  return {
    walletStore,
    showCreate, createForm, createError, createLoading,
    topUpWallet, topUpAmount, topUpCardId, topUpError, topUpLoading, cards,
    quickAmounts, currencies,
    currencySymbol, cardAccent,
    openCreate, openTopUp, confirmDelete, handleCreate, handleTopUp,
  }
}
