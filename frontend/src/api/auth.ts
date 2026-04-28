import client from './client'
import type { LoginUserDto, RegisterUserDto, VerifyUserDto, AuthResponse, PrivateUserDto } from '@/types'

export const authApi = {
  login: (data: LoginUserDto) =>
    client.post<AuthResponse>('/auth/login', data),

  register: (data: RegisterUserDto) =>
    client.post<void>('/auth/register', data),

  verify: (data: VerifyUserDto) =>
    client.post<void>('/auth/verify', data),

  me: () =>
    client.get<PrivateUserDto>('/auth/me'),
}
