import client from './client'
import type { TransactionResponse, TransactionFilterRequest, Page } from '@/types'

export const transactionsApi = {
  getMyTransactions: (params?: TransactionFilterRequest) =>
    client.get<Page<TransactionResponse>>('/transactions', { params }),

  getById: (id: number) =>
    client.get<TransactionResponse>(`/transactions/${id}`),

  getAllAdmin: (params?: TransactionFilterRequest) =>
    client.get<Page<TransactionResponse>>('/transactions/all', { params }),
}
