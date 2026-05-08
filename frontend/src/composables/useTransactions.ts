import { ref, computed } from 'vue'
import { transactionsApi } from '@/api/transactions'
import type { UserTransactionResponse, TransactionType, TransactionStatus } from '@/types'
import {
  PlusCircleIcon,
  ArrowDownCircleIcon,
  ArrowUpCircleIcon,
} from 'lucide-vue-next'

export function useTransactions() {
  const transactions = ref<UserTransactionResponse[]>([])
  const loading = ref(false)
  const page = ref(0)
  const totalPages = ref(1)
  const expandedId = ref<number | null>(null)

  const filters = ref<{
    type: TransactionType | ''
    status: TransactionStatus | ''
    from: string
    to: string
  }>({ type: '', status: '', from: '', to: '' })

  const typeOptions = [
    { value: '' as TransactionType | '', label: 'All', icon: null, color: 'indigo' },
    { value: 'TOP_UP' as TransactionType, label: 'Top Up', icon: PlusCircleIcon, color: 'indigo' },
    { value: 'TRANSFER_IN' as TransactionType, label: 'Received', icon: ArrowDownCircleIcon, color: 'green' },
    { value: 'TRANSFER_OUT' as TransactionType, label: 'Sent', icon: ArrowUpCircleIcon, color: 'red' },
  ]

  const statusOptions = [
    { value: '' as TransactionStatus | '', label: 'All statuses', color: 'indigo' },
    { value: 'SUCCESSFUL' as TransactionStatus, label: 'Successful', color: 'green' },
    { value: 'PENDING' as TransactionStatus, label: 'Pending', color: 'amber' },
    { value: 'FAILED' as TransactionStatus, label: 'Failed', color: 'red' },
  ]

  const hasActiveFilters = computed(() =>
    !!(filters.value.type || filters.value.status || filters.value.from || filters.value.to),
  )

  const hasDateFilter = computed(() => !!(filters.value.from || filters.value.to))

  const dateRangeLabel = computed(() => {
    if (!filters.value.from && !filters.value.to) return 'Date range'
    const fmt = (s: string) => new Date(s).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
    if (filters.value.from && filters.value.to) return `${fmt(filters.value.from)} – ${fmt(filters.value.to)}`
    if (filters.value.from) return `From ${fmt(filters.value.from)}`
    return `Until ${fmt(filters.value.to!)}`
  })

  const visiblePages = computed(() => {
    const total = totalPages.value
    const current = page.value
    const delta = 2
    const range: number[] = []
    for (let i = Math.max(0, current - delta); i <= Math.min(total - 1, current + delta); i++) {
      range.push(i)
    }
    return range
  })

  const TTL_MS = 30_000
  interface CacheEntry {
    content: UserTransactionResponse[]
    totalPages: number
    cachedAt: number
  }
  const cache = new Map<string, CacheEntry>()

  function cacheGet(key: string): { content: UserTransactionResponse[]; totalPages: number } | null {
    const entry = cache.get(key)
    if (!entry) return null
    if (Date.now() - entry.cachedAt > TTL_MS) {
      cache.delete(key)
      return null
    }
    return { content: entry.content, totalPages: entry.totalPages }
  }

  function cacheSet(key: string, content: UserTransactionResponse[], totalPages: number) {
    cache.set(key, { content, totalPages, cachedAt: Date.now() })
  }

  function buildParams(p: number) {
    return {
      page: p,
      size: 10,
      ...(filters.value.type   && { type:   filters.value.type }),
      ...(filters.value.status && { status: filters.value.status }),
      ...(filters.value.from   && { from:   filters.value.from }),
      ...(filters.value.to     && { to:     filters.value.to }),
    }
  }

  function cacheKey(p: number) {
    return JSON.stringify(buildParams(p))
  }

  async function load() {
    expandedId.value = null
    const key = cacheKey(page.value)
    const hit = cacheGet(key)

    if (hit) {
      transactions.value = hit.content
      totalPages.value = hit.totalPages
      return
    }

    loading.value = true
    try {
      const { data } = await transactionsApi.getMyTransactions(buildParams(page.value))
      transactions.value = data.content
      totalPages.value = data.total_pages
      cacheSet(key, data.content, data.total_pages)
    } finally {
      loading.value = false
    }
  }

  function resetAndLoad() {
    page.value = 0
    load()
  }

  function setType(v: TransactionType | '') {
    filters.value.type = v
    resetAndLoad()
  }

  function setStatus(v: TransactionStatus | '') {
    filters.value.status = v
    resetAndLoad()
  }

  function clearDates() {
    filters.value.from = ''
    filters.value.to = ''
    resetAndLoad()
  }

  function clearFilters() {
    filters.value = { type: '', status: '', from: '', to: '' }
    resetAndLoad()
  }

  function goTo(p: number) {
    page.value = p
    load()
  }

  function toggle(id: number) {
    expandedId.value = expandedId.value === id ? null : id
  }

  return {
    transactions, loading, page, totalPages, expandedId, filters,
    typeOptions, statusOptions,
    hasActiveFilters, hasDateFilter, dateRangeLabel, visiblePages,
    load, resetAndLoad, setType, setStatus, clearDates, clearFilters, goTo, toggle,
  }
}
