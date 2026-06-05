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
        <p class="brand-tagline">One last step to<br>secure your account.</p>
      </div>
    </div>

    <!-- Form panel -->
    <div class="form-panel">
      <div class="form-box">
        <div class="form-heading">
          <div class="verify-icon">
            <MailCheckIcon :size="28" />
          </div>
          <h2>Check your email</h2>
          <p>
            Enter the verification code we sent to
            <strong>{{ auth.emailForVerification ?? 'your email' }}</strong>.
          </p>
        </div>

        <form @submit.prevent="handleSubmit">
          <div class="field">
            <label>Verification Code</label>
            <div class="input-wrap">
              <span class="input-icon"><KeyRoundIcon :size="16" /></span>
              <input
                v-model="code"
                type="text"
                placeholder="Enter your code"
                required
                autocomplete="one-time-code"
              />
            </div>
          </div>

          <p v-if="error" class="form-error">
            <AlertCircleIcon :size="14" />{{ error }}
          </p>

          <div v-if="success" class="success-banner">
            <CheckCircleIcon :size="16" />
            Email verified! Redirecting…
          </div>

          <button type="submit" class="btn-primary submit-btn" :disabled="loading || success">
            <LoaderIcon v-if="loading" :size="15" class="spinner" />
            <CheckCircleIcon v-else-if="success" :size="15" />
            {{ loading ? 'Verifying…' : success ? 'Verified!' : 'Verify Email' }}
          </button>
        </form>

        <p class="switch-link">
          Wrong email? <RouterLink to="/register">Start over</RouterLink>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  WalletIcon,
  MailCheckIcon,
  KeyRoundIcon,
  AlertCircleIcon,
  CheckCircleIcon,
  LoaderIcon,
} from 'lucide-vue-next'

const auth = useAuthStore()
const router = useRouter()

const code = ref('')
const error = ref('')
const success = ref(false)
const loading = ref(false)

async function handleSubmit() {
  error.value = ''
  loading.value = true

  if (!auth.emailForVerification) {
    error.value = 'Missing email. Please register again.'
    await router.push('/register')
    return
  }

  try {
    await auth.verify({
      email: auth.emailForVerification,
      verification_code: code.value,
    })
    success.value = true
    setTimeout(() => router.push('/login'), 1500)
  } catch (e) {
    error.value = (e as { response?: { data?: { message?: string } } }).response?.data?.message ?? 'Invalid code. Please try again.'
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

.verify-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  border-radius: var(--radius-lg);
  background: var(--c-primary-light);
  color: var(--c-primary);
  margin-bottom: 1.25rem;
}

.form-heading h2 {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--c-text);
  letter-spacing: -0.025em;
  margin-bottom: 0.5rem;
}

.form-heading p {
  color: var(--c-text-muted);
  font-size: 0.9rem;
  line-height: 1.6;
}

.form-heading p strong {
  color: var(--c-text-secondary);
  font-weight: 600;
}

form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.success-banner {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: var(--c-success-soft);
  border: 1px solid var(--c-success-border);
  border-radius: var(--radius-md);
  color: var(--c-success);
  font-size: 0.875rem;
  font-weight: 600;
}

.submit-btn {
  width: 100%;
  justify-content: center;
  padding: 0.75rem;
  font-size: 0.95rem;
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
