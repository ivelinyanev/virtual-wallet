import client from './client'
import type { LoginRequest, RegisterRequest, VerifyRequest, AuthResponse, PrivateUserResponse } from '@/types'

export const authApi = {
  login: (data: LoginRequest) =>
    client.post<AuthResponse>('/auth/login', data),

  register: (data: RegisterRequest) =>
    client.post<void>('/auth/register', data),

  verify: (data: VerifyRequest) =>
    client.post<void>('/auth/verify', data),

  me: () =>
    client.get<PrivateUserResponse>('/auth/me'),
}
