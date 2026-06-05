import client from './client'
import type { CardResponse, CardCreateRequest } from '@/types'

export const cardsApi = {
  getMyCards: () =>
    client.get<CardResponse[]>('/cards'),

  getById: (id: number) =>
    client.get<CardResponse>(`/cards/${id}`),

  create: (data: CardCreateRequest) =>
    client.post<CardResponse>('/cards', data),

  delete: (id: number) =>
    client.delete<void>(`/cards/${id}`),
}
