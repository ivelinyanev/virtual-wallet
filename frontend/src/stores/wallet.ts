import { defineStore } from 'pinia'
import { ref } from 'vue'
import { walletsApi } from '@/api/wallets'
import type { WalletResponse, WalletCreateRequest, TopUpRequest, WithdrawRequest } from '@/types'

const TTL_MS = 30_000

export const useWalletStore = defineStore('wallet', () => {
  const wallets = ref<WalletResponse[]>([])
  const loading = ref(false)
  const lastFetchedAt = ref<number | null>(null)

  async function fetchWallets({ force = false } = {}) {
    const fresh = lastFetchedAt.value !== null && Date.now() - lastFetchedAt.value < TTL_MS
    if (!force && fresh && wallets.value.length > 0) return

    loading.value = true
    try {
      const { data } = await walletsApi.getMyWallets()
      wallets.value = data
      lastFetchedAt.value = Date.now()
    } finally {
      loading.value = false
    }
  }

  async function createWallet(payload: WalletCreateRequest) {
    const { data } = await walletsApi.create(payload)
    wallets.value.push(data)
  }

  async function deleteWallet(id: number) {
    await walletsApi.delete(id)
    wallets.value = wallets.value.filter((w) => w.id !== id)
  }

  async function topUp(payload: TopUpRequest) {
    await walletsApi.topUp(payload)
    await fetchWallets({ force: true })
  }

  async function withdraw(payload: WithdrawRequest) {
    await walletsApi.withdraw(payload)
    await fetchWallets({ force: true })
  }

  return { wallets, loading, fetchWallets, createWallet, deleteWallet, topUp, withdraw }
})
