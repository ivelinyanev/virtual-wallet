import client from './client'
import type { PrivateCardDto, CardCreateReq } from '@/types'

export const cardsApi = {
  getMyCards: () =>
    client.get<PrivateCardDto[]>('/cards'),

  getById: (id: number) =>
    client.get<PrivateCardDto>(`/cards/${id}`),

  create: (data: CardCreateReq) =>
    client.post<PrivateCardDto>('/cards', data),

  delete: (id: number) =>
    client.delete<void>(`/cards/${id}`),
}
