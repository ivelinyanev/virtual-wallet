import { ref, computed } from 'vue'
import { useWalletStore } from '@/stores/wallet'
import { usersApi } from '@/api/users'
import { transfersApi } from '@/api/transfers'
import type { PublicUserResponse } from '@/types'

export function useTransfer() {
  const walletStore = useWalletStore()

  const recipientQuery = ref('')
  const searchResults = ref<PublicUserResponse[]>([])
  const selectedRecipient = ref<PublicUserResponse | null>(null)
  const selectedWalletName = ref<string>('')
  const amount = ref<number | ''>('')
  const error = ref('')
  const success = ref(false)
  const loading = ref(false)

  let searchTimeout: ReturnType<typeof setTimeout>

  function searchUsers() {
    clearTimeout(searchTimeout)
    selectedRecipient.value = null
    if (recipientQuery.value.length < 2) {
      searchResults.value = []
      return
    }
    searchTimeout = setTimeout(async () => {
      const { data } = await usersApi.search(recipientQuery.value)
      searchResults.value = data.content
    }, 300)
  }

  function selectRecipient(user: PublicUserResponse) {
    selectedRecipient.value = user
    recipientQuery.value = user.username
    searchResults.value = []
  }

  const selectedWallet = computed(() =>
    walletStore.wallets.find((w) => w.wallet_name === selectedWalletName.value) ?? null,
  )

  const canSubmit = computed<boolean>(
    () => !!(selectedRecipient.value && selectedWalletName.value && Number(amount.value) > 0),
  )

  async function handleTransfer() {
    error.value = ''
    success.value = false
    loading.value = true

    try {
      await transfersApi.send({
        from_wallet_id: selectedWallet.value!.id,
        to_username: selectedRecipient.value!.username,
        amount: Number(amount.value),
      })

      success.value = true
      await walletStore.fetchWallets({ force: true })
      recipientQuery.value = ''
      selectedRecipient.value = null
      selectedWalletName.value = ''
      amount.value = ''
    } catch (e: any) {
      error.value = e.response?.data?.message ?? 'Transfer failed.'
    } finally {
      loading.value = false
    }
  }

  return {
    walletStore,
    recipientQuery, searchResults, selectedRecipient,
    selectedWalletName, selectedWallet,
    amount, error, success, loading, canSubmit,
    searchUsers, selectRecipient, handleTransfer,
  }
}
