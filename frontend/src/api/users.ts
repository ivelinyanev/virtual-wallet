import client from './client'
import type { PrivateUserResponse, PublicUserResponse, UpdateUserRequest, Page } from '@/types'

export const usersApi = {
  getAll: (page = 0, size = 20) =>
    client.get<Page<PublicUserResponse>>('/users', { params: { page, size } }),

  search: (q: string, page = 0, size = 20) =>
    client.get<Page<PublicUserResponse>>('/users/search', { params: { q, page, size } }),

  getById: (id: number) =>
    client.get<PrivateUserResponse>(`/users/${id}`),

  update: (data: UpdateUserRequest) =>
    client.put<PrivateUserResponse>('/users', data),

  delete: () =>
    client.delete<void>('/users'),
}
