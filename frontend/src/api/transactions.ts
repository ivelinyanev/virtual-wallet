import client from './client'
import type { UserTransactionResponse, AdminTransactionResponse, TransactionFilterRequest, Page } from '@/types'

export const transactionsApi = {
  getMyTransactions: (params?: TransactionFilterRequest) =>
    client.get<Page<UserTransactionResponse>>('/transactions', { params }),

  getById: (id: number) =>
    client.get<UserTransactionResponse>(`/transactions/${id}`),

  getAllAdmin: (params?: TransactionFilterRequest) =>
    client.get<Page<AdminTransactionResponse>>('/transactions/all', { params }),
}
