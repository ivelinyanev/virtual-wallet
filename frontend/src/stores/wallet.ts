import { defineStore } from 'pinia'
import { ref } from 'vue'
import { walletsApi } from '@/api/wallets'
import type { PrivateWalletDto, WalletCreateReq, TopUpRequest } from '@/types'

const TTL_MS = 30_000

export const useWalletStore = defineStore('wallet', () => {
  const wallets = ref<PrivateWalletDto[]>([])
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

  async function createWallet(payload: WalletCreateReq) {
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

  return { wallets, loading, fetchWallets, createWallet, deleteWallet, topUp }
})
