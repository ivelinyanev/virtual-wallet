import { ref, computed, watch } from 'vue'
import { useWalletStore } from '@/stores/wallet'
import { transfersApi } from '@/api/transfers'
import { conversionsApi } from '@/api/conversions'
import type { WalletResponse } from '@/types'

type InputSide = 'from' | 'to'

export function useInternalTransfer() {
  const walletStore = useWalletStore()

  const fromWallet = ref<WalletResponse | null>(null)
  const toWallet = ref<WalletResponse | null>(null)

  const fromAmount = ref<number | ''>('')
  const toAmount = ref<number | ''>('')
  const activeSide = ref<InputSide>('from')

  const rate = ref<number | null>(null)
  const rateLoading = ref(false)

  const loading = ref(false)
  const error = ref('')
  const success = ref(false)

  // Fetch rate whenever both wallets are selected and currencies differ
  async function fetchRate() {
    if (!fromWallet.value || !toWallet.value) { rate.value = null; return }
    if (fromWallet.value.currency === toWallet.value.currency) { rate.value = 1; return }

    rateLoading.value = true
    try {
      const { data } = await conversionsApi.getRate(fromWallet.value.currency, toWallet.value.currency)
      rate.value = data.rate
      // Recalculate the passive side whenever rate updates
      recalculate()
    } finally {
      rateLoading.value = false
    }
  }

  watch([fromWallet, toWallet], fetchRate)

  function recalculate() {
    if (rate.value === null) return
    if (activeSide.value === 'from' && fromAmount.value !== '') {
      toAmount.value = round(Number(fromAmount.value) * rate.value)
    } else if (activeSide.value === 'to' && toAmount.value !== '') {
      fromAmount.value = round(Number(toAmount.value) / rate.value)
    }
  }

  function onFromInput(val: number | '') {
    activeSide.value = 'from'
    fromAmount.value = val
    if (val === '' || rate.value === null) { toAmount.value = ''; return }
    toAmount.value = round(Number(val) * rate.value)
  }

  function onToInput(val: number | '') {
    activeSide.value = 'to'
    toAmount.value = val
    if (val === '' || rate.value === null) { fromAmount.value = ''; return }
    fromAmount.value = round(Number(val) / rate.value)
  }

  function swapWallets() {
    const tmp = fromWallet.value
    fromWallet.value = toWallet.value
    toWallet.value = tmp
    // Mirror the amounts: what was "to" becomes "from"
    const tmpAmt = fromAmount.value
    fromAmount.value = toAmount.value
    toAmount.value = tmpAmt
    activeSide.value = activeSide.value === 'from' ? 'to' : 'from'
  }

  const canSubmit = computed(() =>
    !!fromWallet.value &&
    !!toWallet.value &&
    fromWallet.value.id !== toWallet.value.id &&
    Number(fromAmount.value) > 0
  )

  async function handleInternalTransfer() {
    if (!fromWallet.value || !toWallet.value || fromAmount.value === '') return
    error.value = ''
    success.value = false
    loading.value = true
    try {
      await transfersApi.internalTransfer({
        from_wallet_id: fromWallet.value.id,
        to_wallet_id: toWallet.value.id,
        amount: Number(fromAmount.value),
      })
      success.value = true
      await walletStore.fetchWallets({ force: true })
      fromAmount.value = ''
      toAmount.value = ''
    } catch (e) {
      error.value = (e as { response?: { data?: { message?: string } } }).response?.data?.message ?? 'Transfer failed.'
    } finally {
      loading.value = false
    }
  }

  function round(n: number) {
    return Math.round(n * 100) / 100
  }

  return {
    walletStore,
    fromWallet, toWallet,
    fromAmount, toAmount,
    rate, rateLoading,
    loading, error, success, canSubmit,
    onFromInput, onToInput, swapWallets, handleInternalTransfer,
  }
}
