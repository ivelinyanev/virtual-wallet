import client from './client'
import type { TransferReq, TransactionResponse } from '@/types'

export const transfersApi = {
  send: (data: TransferReq) =>
    client.post<TransactionResponse>('/transfers', data),
}
