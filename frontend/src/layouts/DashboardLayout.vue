<template>
  <div class="app-layout">
    <nav class="sidebar">
      <div class="logo">Virtual Wallet</div>
      <ul class="nav-links">
        <li><RouterLink to="/">Dashboard</RouterLink></li>
        <li><RouterLink to="/wallets">Wallets</RouterLink></li>
        <li><RouterLink to="/cards">Cards</RouterLink></li>
        <li><RouterLink to="/transactions">Transactions</RouterLink></li>
        <li><RouterLink to="/transfer">Transfer</RouterLink></li>
        <li v-if="auth.isAdmin"><RouterLink to="/admin/users">Admin</RouterLink></li>
      </ul>
      <div class="user-section">
        <span>{{ auth.user?.username }}</span>
        <button @click="handleLogout">Logout</button>
      </div>
    </nav>
    <main class="content">
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.app-layout {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  width: 220px;
  background: #1a1a2e;
  color: #eee;
  display: flex;
  flex-direction: column;
  padding: 1.5rem 1rem;
  gap: 1rem;
}

.logo {
  font-size: 1.2rem;
  font-weight: bold;
  color: #7c83fd;
  margin-bottom: 1rem;
}

.nav-links {
  list-style: none;
  padding: 0;
  margin: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.nav-links a {
  color: #ccc;
  text-decoration: none;
  padding: 0.5rem 0.75rem;
  border-radius: 6px;
  display: block;
  transition: background 0.2s;
}

.nav-links a:hover,
.nav-links a.router-link-active {
  background: #7c83fd22;
  color: #7c83fd;
}

.user-section {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  font-size: 0.875rem;
}

.user-section button {
  background: transparent;
  border: 1px solid #555;
  color: #ccc;
  padding: 0.4rem;
  border-radius: 6px;
  cursor: pointer;
}

.user-section button:hover {
  border-color: #7c83fd;
  color: #7c83fd;
}

.content {
  flex: 1;
  padding: 2rem;
  background: #f5f6fa;
  overflow-y: auto;
}
</style>
