<template>
  <div class="auth-root">
    <!-- Brand panel -->
    <div class="brand-panel">
      <div class="deco deco-a" />
      <div class="deco deco-b" />
      <div class="deco deco-c" />
      <div class="brand-content">
        <div class="brand-mark">
          <WalletIcon :size="30" />
        </div>
        <p class="brand-name">VirtualWallet</p>
        <p class="brand-tagline">Send, receive and manage<br>money with ease.</p>
      </div>
    </div>

    <!-- Form panel -->
    <div class="form-panel">
      <div class="form-box">
        <div class="form-heading">
          <h2>Create account</h2>
          <p>Fill in the details below to get started.</p>
        </div>

        <form @submit.prevent="handleSubmit">
          <div class="row">
            <div class="field">
              <label>First Name</label>
              <div class="input-wrap">
                <span class="input-icon"><UserIcon :size="16" /></span>
                <input v-model="form.first_name" type="text" placeholder="Jane" required />
              </div>
            </div>
            <div class="field">
              <label>Last Name</label>
              <div class="input-wrap">
                <span class="input-icon"><UserIcon :size="16" /></span>
                <input v-model="form.last_name" type="text" placeholder="Doe" required />
              </div>
            </div>
          </div>

          <div class="field">
            <label>Username</label>
            <div class="input-wrap">
              <span class="input-icon"><AtSignIcon :size="16" /></span>
              <input v-model="form.username" type="text" placeholder="janedoe" required />
            </div>
          </div>

          <div class="field">
            <label>Email</label>
            <div class="input-wrap">
              <span class="input-icon"><MailIcon :size="16" /></span>
              <input v-model="form.email" type="email" placeholder="jane@example.com" required />
            </div>
          </div>

          <div class="field">
            <label>Phone Number</label>
            <div class="input-wrap">
              <span class="input-icon"><PhoneIcon :size="16" /></span>
              <input v-model="form.phone_number" type="tel" placeholder="+1 555 000 0000" required />
            </div>
          </div>

          <div class="field">
            <label>Password</label>
            <div class="input-wrap">
              <span class="input-icon"><LockIcon :size="16" /></span>
              <input
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="••••••••"
                required
                autocomplete="new-password"
              />
              <button type="button" class="eye-btn" @click="showPassword = !showPassword" tabindex="-1">
                <EyeOffIcon v-if="showPassword" :size="16" />
                <EyeIcon v-else :size="16" />
              </button>
            </div>

            <!-- Password strength meter -->
            <div v-if="form.password" class="strength-meter">
              <div class="strength-bars">
                <span
                  v-for="i in 4"
                  :key="i"
                  class="strength-bar"
                  :class="{ active: i <= passwordStrength }"
                  :style="i <= passwordStrength ? { background: strengthColor } : {}"
                />
              </div>
              <span class="strength-label" :style="{ color: strengthColor }">{{ strengthLabel }}</span>
            </div>
          </div>

          <p v-if="error" class="form-error">
            <AlertCircleIcon :size="14" />{{ error }}
          </p>

          <button type="submit" class="btn-primary submit-btn" :disabled="loading">
            <LoaderIcon v-if="loading" :size="15" class="spinner" />
            {{ loading ? 'Creating account…' : 'Create Account' }}
          </button>
        </form>

        <p class="switch-link">
          Already have an account? <RouterLink to="/login">Sign in</RouterLink>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  WalletIcon,
  UserIcon,
  AtSignIcon,
  MailIcon,
  PhoneIcon,
  LockIcon,
  EyeIcon,
  EyeOffIcon,
  AlertCircleIcon,
  LoaderIcon,
} from 'lucide-vue-next'

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
const showPassword = ref(false)

const passwordStrength = computed(() => {
  const p = form.value.password
  if (!p) return 0
  let score = 0
  if (p.length >= 8) score++
  if (/[A-Z]/.test(p)) score++
  if (/[0-9]/.test(p)) score++
  if (/[^A-Za-z0-9]/.test(p)) score++
  return score
})

const strengthLabel = computed(() => ['', 'Weak', 'Fair', 'Good', 'Strong'][passwordStrength.value])
const strengthColor = computed(() => ['', '#ef4444', '#f59e0b', '#3b82f6', '#16a34a'][passwordStrength.value])

async function handleSubmit() {
  error.value = ''
  loading.value = true
  try {
    await auth.register(form.value)
    auth.setEmail(form.value.email)
    router.push('/verify')
  } catch (e) {
    error.value = (e as { response?: { data?: { message?: string } } }).response?.data?.message ?? 'Registration failed. Please try again.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-root {
  min-height: 100vh;
  display: flex;
}

/* ── Brand panel ─────────────────────────────────────────── */
.brand-panel {
  position: relative;
  flex: 1;
  background: linear-gradient(150deg, #4338ca 0%, #6366f1 45%, #7c3aed 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  padding: 3rem;
}

.deco {
  position: absolute;
  border-radius: 50%;
}
.deco-a {
  width: 480px;
  height: 480px;
  top: -140px;
  right: -100px;
  background: rgba(255, 255, 255, 0.07);
}
.deco-b {
  width: 300px;
  height: 300px;
  bottom: -80px;
  left: -60px;
  background: rgba(255, 255, 255, 0.06);
}
.deco-c {
  width: 160px;
  height: 160px;
  bottom: 120px;
  right: 60px;
  background: rgba(255, 255, 255, 0.05);
}

.brand-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: white;
}

.brand-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  margin-bottom: 1.25rem;
}

.brand-name {
  font-size: 1.6rem;
  font-weight: 700;
  letter-spacing: -0.02em;
  margin-bottom: 0.75rem;
}

.brand-tagline {
  font-size: 1rem;
  opacity: 0.78;
  line-height: 1.7;
}

/* ── Form panel ──────────────────────────────────────────── */
.form-panel {
  width: 100%;
  max-width: 520px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 3rem 2.5rem;
  background: var(--c-surface);
  overflow-y: auto;
}

.form-box {
  width: 100%;
  max-width: 400px;
}

.form-heading {
  margin-bottom: 2rem;
}

.form-heading h2 {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--c-text);
  letter-spacing: -0.025em;
  margin-bottom: 0.35rem;
}

.form-heading p {
  color: var(--c-text-muted);
  font-size: 0.9rem;
}

form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
}

.eye-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: var(--c-text-subtle);
  padding: 0 0.75rem;
  display: flex;
  align-items: center;
  transition: color 0.15s;
  flex-shrink: 0;
}
.eye-btn:hover {
  color: var(--c-text-secondary);
}

/* Password strength */
.strength-meter {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  margin-top: 0.5rem;
}
.strength-bars {
  display: flex;
  gap: 4px;
  flex: 1;
}
.strength-bar {
  flex: 1;
  height: 3px;
  border-radius: 99px;
  background: var(--c-border);
  transition: background 0.25s;
}
.strength-label {
  font-size: 0.75rem;
  font-weight: 600;
  min-width: 36px;
  text-align: right;
  transition: color 0.25s;
}

.submit-btn {
  width: 100%;
  justify-content: center;
  padding: 0.75rem;
  font-size: 0.95rem;
  margin-top: 0.25rem;
  gap: 0.5rem;
}

.switch-link {
  text-align: center;
  margin-top: 1.75rem;
  font-size: 0.875rem;
  color: var(--c-text-muted);
}

.switch-link a {
  color: var(--c-primary);
  font-weight: 600;
  text-decoration: none;
}

.switch-link a:hover {
  text-decoration: underline;
}

/* ── Mobile ──────────────────────────────────────────────── */
@media (max-width: 700px) {
  .auth-root {
    flex-direction: column;
  }

  .brand-panel {
    flex: none;
    min-height: 180px;
    padding: 2rem 1.5rem;
  }

  .brand-tagline {
    display: none;
  }

  .form-panel {
    max-width: 100%;
    padding: 2rem 1.5rem 3rem;
    align-items: flex-start;
  }
}
</style>
