import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
import type { PrivateUserResponse, LoginRequest, RegisterRequest, VerifyRequest } from '@/types'

export const useAuthStore = defineStore(
  'auth',
  () => {
    const token = ref<string | null>(null)
    const user = ref<PrivateUserResponse | null>(null)
    const emailForVerification = ref<string | null>(null)

    const isAuthenticated = computed(() => !!token.value)
    const isAdmin = computed(() => user.value?.roles?.includes('ADMIN') ?? false)
    const isVerified = computed(() => user.value?.is_verified ?? false)

    async function login(credentials: LoginRequest) {
      const { data } = await authApi.login(credentials)
      token.value = data.token
      user.value = data.user
    }

    async function register(payload: RegisterRequest) {
      await authApi.register(payload)
    }

    async function verify(payload: VerifyRequest) {
      await authApi.verify(payload)
      emailForVerification.value = null
    }

    async function fetchMe() {
      const { data } = await authApi.me()
      user.value = data
    }

    function setEmail(email: string) {
      emailForVerification.value = email
    }

    function logout() {
      token.value = null
      user.value = null
    }

    return { token, user, emailForVerification, isAuthenticated, isAdmin, isVerified, login, register, verify, fetchMe, setEmail, logout }
  },
  { persist: true },
)
