<template>
  <div class="auth-page">
    <div class="auth-card">
      <h1>Create Account</h1>
      <form @submit.prevent="handleSubmit">
        <div class="row">
          <div class="field">
            <label>First Name</label>
            <input v-model="form.first_name" type="text" required />
          </div>
          <div class="field">
            <label>Last Name</label>
            <input v-model="form.last_name" type="text" required />
          </div>
        </div>
        <div class="field">
          <label>Username</label>
          <input v-model="form.username" type="text" required />
        </div>
        <div class="field">
          <label>Email</label>
          <input v-model="form.email" type="email" required />
        </div>
        <div class="field">
          <label>Phone Number</label>
          <input v-model="form.phone_number" type="tel" required />
        </div>
        <div class="field">
          <label>Password</label>
          <input v-model="form.password" type="password" required />
        </div>
        <p v-if="error" class="error">{{ error }}</p>
        <button type="submit" :disabled="loading">
          {{ loading ? 'Creating account…' : 'Register' }}
        </button>
      </form>
      <p class="link">Already have an account? <RouterLink to="/login">Sign in</RouterLink></p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()

const form = ref({
  first_name: '',
  last_name: '',
  username: '',
  email: '',
  phone_number: '',
  password: '',
})
const error = ref('')
const loading = ref(false)

async function handleSubmit() {
  error.value = ''
  loading.value = true
  try {
    await auth.register(form.value)
    router.push('/verify')
  } catch (e: any) {
    error.value = e.response?.data?.message ?? 'Registration failed. Please try again.'
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
  max-width: 440px;
}

h1 {
  margin: 0 0 1.5rem;
  font-size: 1.5rem;
  color: #1a1a2e;
}

.row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  margin-bottom: 1rem;
}

label {
  font-size: 0.875rem;
  color: #555;
}

input {
  padding: 0.6rem 0.75rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 1rem;
  outline: none;
  transition: border 0.2s;
}

input:focus {
  border-color: #7c83fd;
}

button {
  width: 100%;
  padding: 0.75rem;
  background: #7c83fd;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  cursor: pointer;
  margin-top: 0.5rem;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error {
  color: #e53e3e;
  font-size: 0.875rem;
  margin: 0 0 0.75rem;
}

.link {
  text-align: center;
  margin-top: 1rem;
  font-size: 0.875rem;
  color: #555;
}

.link a {
  color: #7c83fd;
  text-decoration: none;
}
</style>
