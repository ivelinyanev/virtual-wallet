<template>
  <div class="auth-page">
    <div class="auth-card">
      <h1>Verify Email</h1>
      <p class="subtitle">Enter the verification code sent to your email.</p>
      <form @submit.prevent="handleSubmit">
        <div class="field">
          <label>Verification Code</label>
          <input v-model="code" type="text" required />
        </div>
        <p v-if="error" class="error">{{ error }}</p>
        <p v-if="success" class="success">Email verified! Redirecting…</p>
        <button type="submit" :disabled="loading">
          {{ loading ? 'Verifying…' : 'Verify' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()

const code = ref('')
const error = ref('')
const success = ref(false)
const loading = ref(false)

async function handleSubmit() {
  error.value = ''
  loading.value = true
  try {
    await auth.verify({ verification_code: code.value })
    success.value = true
    setTimeout(() => router.push('/'), 1500)
  } catch (e: any) {
    error.value = e.response?.data?.message ?? 'Invalid code. Please try again.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f6fa;
}

.auth-card {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.08);
  width: 100%;
  max-width: 380px;
}

h1 { margin: 0 0 0.5rem; font-size: 1.5rem; color: #1a1a2e; }
.subtitle { color: #666; font-size: 0.9rem; margin-bottom: 1.5rem; }

.field {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  margin-bottom: 1rem;
}

label { font-size: 0.875rem; color: #555; }

input {
  padding: 0.6rem 0.75rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 1rem;
  outline: none;
  transition: border 0.2s;
}

input:focus { border-color: #7c83fd; }

button {
  width: 100%;
  padding: 0.75rem;
  background: #7c83fd;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  cursor: pointer;
}

.error { color: #e53e3e; font-size: 0.875rem; margin: 0 0 0.75rem; }
.success { color: #38a169; font-size: 0.875rem; margin: 0 0 0.75rem; }
</style>
