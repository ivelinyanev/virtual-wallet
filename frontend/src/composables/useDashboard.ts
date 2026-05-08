import { ref, computed } from 'vue'
import { useWalletStore } from '@/stores/wallet'
import { transactionsApi } from '@/api/transactions'
import { cardsApi } from '@/api/cards'
import type { UserTransactionResponse } from '@/types'

export function useDashboard() {
  const walletStore = useWalletStore()

  const recentTransactions = ref<UserTransactionResponse[]>([])
  const cardCount = ref(0)

  const balanceByCurrency = computed(() => {
    const map = new Map<string, number>()
    for (const w of walletStore.wallets) {
      map.set(w.currency, (map.get(w.currency) ?? 0) + w.balance)
    }
    return map
  })

  const isSingleCurrency = computed(() => balanceByCurrency.value.size <= 1)

  async function loadDashboard() {
    await walletStore.fetchWallets()
    const [txRes, cardRes] = await Promise.allSettled([
      transactionsApi.getMyTransactions({ page: 0, size: 5 }),
      cardsApi.getMyCards(),
    ])
    if (txRes.status === 'fulfilled') recentTransactions.value = txRes.value.data.content
    if (cardRes.status === 'fulfilled') cardCount.value = cardRes.value.data.length
  }

  return { walletStore, recentTransactions, cardCount, balanceByCurrency, isSingleCurrency, loadDashboard }
}
