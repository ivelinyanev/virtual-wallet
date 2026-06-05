import client from './client'
import type { TransferRequest, OwnWalletTransferRequest, TransactionResponse } from '@/types'

export const transfersApi = {
  send: (data: TransferRequest) =>
    client.post<TransactionResponse>('/transfers', data),

  internalTransfer: (data: OwnWalletTransferRequest) =>
    client.post<void>('/transfers/internal', data),
}
