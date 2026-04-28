import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
import type { PrivateUserDto, LoginUserDto, RegisterUserDto, VerifyUserDto } from '@/types'

export const useAuthStore = defineStore(
  'auth',
  () => {
    const token = ref<string | null>(null)
    const user = ref<PrivateUserDto | null>(null)

    const isAuthenticated = computed(() => !!token.value)
    const isAdmin = computed(() => user.value?.roles?.includes('ADMIN') ?? false)
    const isVerified = computed(() => user.value?.is_verified ?? false)

    async function login(credentials: LoginUserDto) {
      const { data } = await authApi.login(credentials)
      token.value = data.token
      user.value = data.user
    }

    async function register(payload: RegisterUserDto) {
      await authApi.register(payload)
    }

    async function verify(payload: VerifyUserDto) {
      await authApi.verify(payload)
    }

    async function fetchMe() {
      const { data } = await authApi.me()
      user.value = data
    }

    function logout() {
      token.value = null
      user.value = null
    }

    return { token, user, isAuthenticated, isAdmin, isVerified, login, register, verify, fetchMe, logout }
  },
  { persist: true },
)
