import { ref } from 'vue'
import { transactionsApi } from '@/api/transactions'
import type { UserTransactionResponse } from '@/types'

export function useTransactionDetail() {
  const expandedId = ref<number | null>(null)
  const details = ref<Record<number, UserTransactionResponse>>({})
  const loadingDetail = ref(false)

  async function toggle(id: number) {
    if (expandedId.value === id) {
      expandedId.value = null
      return
    }
    expandedId.value = id
    if (!details.value[id]) {
      loadingDetail.value = true
      try {
        const res = await transactionsApi.getById(id)
        details.value[id] = res.data
      } finally {
        loadingDetail.value = false
      }
    }
  }

  return { expandedId, details, loadingDetail, toggle }
}
