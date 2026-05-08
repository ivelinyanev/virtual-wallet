<template>
  <div class="profile-page">
    <div class="page-header">
      <h2>Profile</h2>
      <p class="subtitle">Your account details</p>
    </div>

    <div class="profile-card">
      <!-- Avatar + identity -->
      <div class="profile-hero">
        <div class="avatar-wrap">
          <div class="avatar">{{ initials }}</div>
          <span v-if="auth.isVerified" class="verified-dot" title="Verified">
            <CheckIcon :size="10" />
          </span>
        </div>
        <div class="hero-info">
          <h3>{{ auth.user?.first_name }} {{ auth.user?.last_name }}</h3>
          <p class="handle">@{{ auth.user?.username }}</p>
          <span v-if="auth.isVerified" class="badge verified">
            <ShieldCheckIcon :size="12" />Verified
          </span>
          <span v-else class="badge unverified">
            <ShieldAlertIcon :size="12" />Not verified —
            <RouterLink to="/verify">verify now</RouterLink>
          </span>
        </div>
      </div>

      <!-- Info rows -->
      <div class="info-section">
        <div class="info-row">
          <span class="info-icon"><MailIcon :size="16" /></span>
          <div class="info-content">
            <span class="info-label">Email</span>
            <span class="info-value">{{ auth.user?.email }}</span>
          </div>
        </div>

        <div class="info-row">
          <span class="info-icon"><PhoneIcon :size="16" /></span>
          <div class="info-content">
            <span class="info-label">Phone</span>
            <span class="info-value">{{ auth.user?.phone_number }}</span>
          </div>
        </div>

        <div class="info-row">
          <span class="info-icon"><AtSignIcon :size="16" /></span>
          <div class="info-content">
            <span class="info-label">Username</span>
            <span class="info-value">{{ auth.user?.username }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { MailIcon, PhoneIcon, AtSignIcon, ShieldCheckIcon, ShieldAlertIcon, CheckIcon } from 'lucide-vue-next'

const auth = useAuthStore()

const initials = computed(() => {
  const f = auth.user?.first_name?.[0] ?? ''
  const l = auth.user?.last_name?.[0] ?? ''
  return (f + l).toUpperCase()
})
</script>

<style scoped>
.profile-page {
  max-width: 560px;
}

.page-header {
  margin-bottom: 2rem;
}

h2 {
  margin: 0 0 0.25rem;
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--c-text);
  letter-spacing: -0.025em;
}

.subtitle {
  margin: 0;
  color: var(--c-text-muted);
  font-size: 0.95rem;
}

/* ── Card ──────────────────────────────────────────────────── */
.profile-card {
  background: var(--c-surface);
  border-radius: var(--radius-xl);
  border: 1px solid var(--c-border-soft);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

/* ── Hero ──────────────────────────────────────────────────── */
.profile-hero {
  display: flex;
  align-items: center;
  gap: 1.25rem;
  padding: 1.75rem 1.75rem 1.5rem;
  border-bottom: 1px solid var(--c-border-soft);
}

.avatar-wrap {
  position: relative;
  flex-shrink: 0;
}

.avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
  font-size: 1.35rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  letter-spacing: 0.03em;
}

.verified-dot {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: var(--c-success);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid var(--c-surface);
}

.hero-info h3 {
  margin: 0 0 0.2rem;
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--c-text);
  letter-spacing: -0.01em;
}

.handle {
  margin: 0 0 0.6rem;
  font-size: 0.875rem;
  color: var(--c-text-muted);
}

.badge {
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  padding: 0.25rem 0.65rem;
  border-radius: 20px;
  font-size: 0.78rem;
  font-weight: 600;
}

.badge.verified {
  background: var(--c-success-soft);
  color: var(--c-success);
  border: 1px solid var(--c-success-border);
}

.badge.unverified {
  background: var(--c-warning-soft);
  color: var(--c-warning);
  border: 1px solid var(--c-warning-border);
}

.badge.unverified a {
  color: var(--c-warning);
  font-weight: 700;
  text-decoration: underline;
}

/* ── Info rows ─────────────────────────────────────────────── */
.info-section {
  padding: 0.5rem 0;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.875rem 1.75rem;
  transition: background 0.15s;
}

.info-row:not(:last-child) {
  border-bottom: 1px solid var(--c-border-soft);
}

.info-row:hover {
  background: var(--c-surface-soft);
}

.info-icon {
  width: 34px;
  height: 34px;
  border-radius: var(--radius-sm);
  background: var(--c-surface-mute);
  color: var(--c-text-muted);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 0.1rem;
  min-width: 0;
}

.info-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--c-text-subtle);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.info-value {
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--c-text);
  word-break: break-all;
}
</style>
