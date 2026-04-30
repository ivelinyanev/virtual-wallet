import { ref, computed } from 'vue'
import { useWalletStore } from '@/stores/wallet'
import { usersApi } from '@/api/users'
import { transfersApi } from '@/api/transfers'
import type { PublicUserDto } from '@/types'

export function useTransfer() {
  const walletStore = useWalletStore()

  const recipientQuery = ref('')
  const searchResults = ref<PublicUserDto[]>([])
  const selectedRecipient = ref<PublicUserDto | null>(null)
  const senderWalletId = ref<number | ''>('')
  const recipientWalletId = ref<number | ''>('')
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

  function selectRecipient(user: PublicUserDto) {
    selectedRecipient.value = user
    recipientQuery.value = user.username
    searchResults.value = []
  }

  const canSubmit = computed(
    () => selectedRecipient.value && senderWalletId.value && recipientWalletId.value && Number(amount.value) > 0,
  )

  async function handleTransfer() {
    error.value = ''
    success.value = false
    loading.value = true
    try {
      await transfersApi.send({
        sender_wallet_id: Number(senderWalletId.value),
        recipient_wallet_id: Number(recipientWalletId.value),
        amount: Number(amount.value),
      })
      success.value = true
      await walletStore.fetchWallets({ force: true })
      recipientQuery.value = ''
      selectedRecipient.value = null
      senderWalletId.value = ''
      recipientWalletId.value = ''
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
    senderWalletId, recipientWalletId, amount,
    error, success, loading, canSubmit,
    searchUsers, selectRecipient, handleTransfer,
  }
}
