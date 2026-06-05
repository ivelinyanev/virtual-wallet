import client from './client'
import type { WalletResponse, WalletCreateRequest, TopUpRequest, WithdrawRequest } from '@/types'

export const walletsApi = {
  getMyWallets: () =>
    client.get<WalletResponse[]>('/wallets'),

  getById: (id: number) =>
    client.get<WalletResponse>(`/wallets/${id}`),

  create: (data: WalletCreateRequest) =>
    client.post<WalletResponse>('/wallets', data),

  delete: (id: number) =>
    client.delete<void>(`/wallets/${id}`),

  topUp: (data: TopUpRequest) =>
    client.post<WalletResponse>('/wallets/top-up', data),

  withdraw: (data: WithdrawRequest) =>
    client.post<void>('/wallets/withdraw', data),
}
