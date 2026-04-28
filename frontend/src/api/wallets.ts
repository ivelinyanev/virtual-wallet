import client from './client'
import type { PrivateWalletDto, WalletCreateReq, TopUpRequest } from '@/types'

export const walletsApi = {
  getMyWallets: () =>
    client.get<PrivateWalletDto[]>('/wallets'),

  getById: (id: number) =>
    client.get<PrivateWalletDto>(`/wallets/${id}`),

  create: (data: WalletCreateReq) =>
    client.post<PrivateWalletDto>('/wallets', data),

  delete: (id: number) =>
    client.delete<void>(`/wallets/${id}`),

  topUp: (data: TopUpRequest) =>
    client.post<PrivateWalletDto>('/wallets/top-up', data),
}
