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
        <p class="brand-tagline">The smarter way to manage<br>your money.</p>
      </div>
    </div>

    <!-- Form panel -->
    <div class="form-panel">
      <div class="form-box">
        <div class="form-heading">
          <h2>Welcome back</h2>
          <p>Sign in to your account to continue.</p>
        </div>

        <form @submit.prevent="handleSubmit">
          <div class="field">
            <label>Email</label>
            <div class="input-wrap">
              <span class="input-icon"><MailIcon :size="16" /></span>
              <input
                v-model="form.email"
                type="email"
                placeholder="you@example.com"
                required
                autocomplete="email"
              />
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
                autocomplete="current-password"
              />
              <button type="button" class="eye-btn" @click="showPassword = !showPassword" tabindex="-1">
                <EyeOffIcon v-if="showPassword" :size="16" />
                <EyeIcon v-else :size="16" />
              </button>
            </div>
          </div>

          <p v-if="error" class="form-error">
            <AlertCircleIcon :size="14" />{{ error }}
          </p>

          <button type="submit" class="btn-primary submit-btn" :disabled="loading">
            <LoaderIcon v-if="loading" :size="15" class="spinner" />
            {{ loading ? 'Signing in…' : 'Sign In' }}
          </button>
        </form>

        <p class="switch-link">
          Don't have an account? <RouterLink to="/register">Create one</RouterLink>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { WalletIcon, MailIcon, LockIcon, EyeIcon, EyeOffIcon, AlertCircleIcon, LoaderIcon } from 'lucide-vue-next'

const auth = useAuthStore()
const router = useRouter()

const form = ref({ email: '', password: '' })
const error = ref('')
const loading = ref(false)
const showPassword = ref(false)

async function handleSubmit() {
  error.value = ''
  loading.value = true
  try {
    await auth.login(form.value)
    router.push('/')
  } catch (e) {
    error.value = (e as { response?: { data?: { message?: string } } }).response?.data?.message ?? 'Login failed. Check your credentials.'
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
  max-width: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 3rem 2.5rem;
  background: var(--c-surface);
}

.form-box {
  width: 100%;
  max-width: 360px;
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
