import client from './client'
import type { PrivateUserDto, PublicUserDto, UpdateUserDto, Page } from '@/types'

export const usersApi = {
  getAll: (page = 0, size = 20) =>
    client.get<Page<PublicUserDto>>('/users', { params: { page, size } }),

  search: (q: string, page = 0, size = 20) =>
    client.get<Page<PublicUserDto>>('/users/search', { params: { q, page, size } }),

  getById: (id: number) =>
    client.get<PrivateUserDto>(`/users/${id}`),

  update: (data: UpdateUserDto) =>
    client.put<PrivateUserDto>('/users', data),

  delete: () =>
    client.delete<void>('/users'),
}
