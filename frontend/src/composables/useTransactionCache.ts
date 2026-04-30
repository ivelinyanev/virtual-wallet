import type { UserTransactionResponse } from '@/types'

const TTL_MS = 30_000

interface CacheEntry {
  content: UserTransactionResponse[]
  totalPages: number
  cachedAt: number
}

// Module-level: survives component unmount / navigation
const cache = new Map<string, CacheEntry>()

export function useTransactionCache() {
  function get(key: string): { content: UserTransactionResponse[]; totalPages: number } | null {
    const entry = cache.get(key)
    if (!entry) return null
    if (Date.now() - entry.cachedAt > TTL_MS) {
      cache.delete(key)
      return null
    }
    return { content: entry.content, totalPages: entry.totalPages }
  }

  function set(key: string, content: UserTransactionResponse[], totalPages: number) {
    cache.set(key, { content, totalPages, cachedAt: Date.now() })
  }

  function clear() {
    cache.clear()
  }

  return { get, set, clear }
}
