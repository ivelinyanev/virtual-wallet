import client from './client'
import type { Currency } from '@/types'

export const conversionsApi = {
  getRate: (from: Currency, to: Currency) =>
    client.get<{ rate: number }>('/conversions/rate', { params: { from, to } }),
}
