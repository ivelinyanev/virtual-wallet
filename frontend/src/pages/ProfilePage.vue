<template>
  <div>
    <h2>Profile</h2>
    <div class="profile-card">
      <div class="avatar">{{ initials }}</div>
      <div class="info">
        <h3>{{ auth.user?.first_name }} {{ auth.user?.last_name }}</h3>
        <p>@{{ auth.user?.username }}</p>
        <p>{{ auth.user?.email }}</p>
        <p>{{ auth.user?.phone_number }}</p>
        <span v-if="!auth.isVerified" class="badge unverified">
          Not verified — <RouterLink to="/verify">verify now</RouterLink>
        </span>
        <span v-else class="badge verified">Verified</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()

const initials = computed(() => {
  const f = auth.user?.first_name?.[0] ?? ''
  const l = auth.user?.last_name?.[0] ?? ''
  return (f + l).toUpperCase()
})
</script>

<style scoped>
h2 { margin: 0 0 1.5rem; color: #1a1a2e; }

.profile-card {
  background: white;
  border-radius: 14px;
  padding: 2rem;
  display: flex;
  gap: 2rem;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.07);
  max-width: 500px;
}

.avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: #7c83fd;
  color: white;
  font-size: 1.5rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.info h3 { margin: 0 0 0.25rem; font-size: 1.2rem; }
.info p { margin: 0.25rem 0; color: #555; font-size: 0.9rem; }

.badge {
  display: inline-block;
  margin-top: 0.5rem;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
}

.verified { background: #c6f6d5; color: #276749; }
.unverified { background: #fefcbf; color: #744210; }
.unverified a { color: #744210; }
</style>
