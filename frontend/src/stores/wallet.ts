import { defineStore } from 'pinia'
import { ref } from 'vue'
import { walletsApi } from '@/api/wallets'
import type { PrivateWalletDto, WalletCreateReq, TopUpRequest } from '@/types'

export const useWalletStore = defineStore('wallet', () => {
  const wallets = ref<PrivateWalletDto[]>([])
  const loading = ref(false)

  async function fetchWallets() {
    loading.value = true
    try {
      const { data } = await walletsApi.getMyWallets()
      wallets.value = data
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
    const { data } = await walletsApi.topUp(payload)
    const idx = wallets.value.findIndex((w) => w.id === payload.wallet_id)
    if (idx !== -1) wallets.value[idx] = data
  }

  return { wallets, loading, fetchWallets, createWallet, deleteWallet, topUp }
})
