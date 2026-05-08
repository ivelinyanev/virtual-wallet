<template>
  <div class="app-layout">
    <!-- Mobile top bar -->
    <header class="mobile-bar">
      <button class="hamburger" @click="sidebarOpen = true" aria-label="Open menu">
        <MenuIcon :size="20" />
      </button>
      <div class="mobile-logo">
        <WalletIcon :size="18" color="#818cf8" />
        <span>Virtual Wallet</span>
      </div>
    </header>

    <!-- Overlay (mobile) -->
    <Transition name="overlay">
      <div v-if="sidebarOpen" class="overlay" @click="sidebarOpen = false" />
    </Transition>

    <aside class="sidebar" :class="{ open: sidebarOpen }">
      <!-- Close button (mobile only) -->
      <button class="sidebar-close" @click="sidebarOpen = false" aria-label="Close menu">
        <XIcon :size="18" />
      </button>

      <!-- Logo -->
      <div class="sidebar-logo">
        <WalletIcon :size="22" color="#818cf8" />
        <span class="logo-text">Virtual Wallet</span>
      </div>

      <!-- Main nav -->
      <nav class="sidebar-nav">
        <span class="nav-group-label">Main</span>
        <RouterLink to="/" exact-active-class="active" class="nav-item" @click="sidebarOpen = false">
          <LayoutDashboardIcon :size="18" />
          <span>Dashboard</span>
        </RouterLink>
        <RouterLink to="/wallets" active-class="active" class="nav-item" @click="sidebarOpen = false">
          <LandmarkIcon :size="18" />
          <span>Wallets</span>
        </RouterLink>
        <RouterLink to="/cards" active-class="active" class="nav-item" @click="sidebarOpen = false">
          <CreditCardIcon :size="18" />
          <span>Cards</span>
        </RouterLink>
        <RouterLink to="/transactions" active-class="active" class="nav-item" @click="sidebarOpen = false">
          <ArrowLeftRightIcon :size="18" />
          <span>Transactions</span>
        </RouterLink>
        <RouterLink to="/transfer" active-class="active" class="nav-item" @click="sidebarOpen = false">
          <SendIcon :size="18" />
          <span>Transfer</span>
        </RouterLink>

        <template v-if="auth.isAdmin">
          <span class="nav-group-label" style="margin-top: 1.25rem">Admin</span>
          <RouterLink to="/admin/users" active-class="active" class="nav-item" @click="sidebarOpen = false">
            <UsersIcon :size="18" />
            <span>Users</span>
          </RouterLink>
          <RouterLink to="/admin/transactions" active-class="active" class="nav-item" @click="sidebarOpen = false">
            <ClipboardListIcon :size="18" />
            <span>All Transactions</span>
          </RouterLink>
        </template>
      </nav>

      <!-- User section -->
      <div class="sidebar-footer">
        <RouterLink to="/profile" active-class="active" class="user-card" @click="sidebarOpen = false">
          <div class="avatar">{{ initials }}</div>
          <div class="user-info">
            <span class="user-name">{{ auth.user?.first_name }} {{ auth.user?.last_name }}</span>
            <span class="user-handle">@{{ auth.user?.username }}</span>
          </div>
        </RouterLink>
        <button class="logout-btn" title="Logout" @click="handleLogout">
          <LogOutIcon :size="16" />
        </button>
      </div>
    </aside>

    <div class="content-wrapper">
      <main class="content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import {
  WalletIcon,
  LayoutDashboardIcon,
  LandmarkIcon,
  CreditCardIcon,
  ArrowLeftRightIcon,
  SendIcon,
  UsersIcon,
  ClipboardListIcon,
  LogOutIcon,
  MenuIcon,
  XIcon,
} from 'lucide-vue-next'

const auth = useAuthStore()
const router = useRouter()
const sidebarOpen = ref(false)

const initials = computed(() => {
  const f = auth.user?.first_name?.[0] ?? ''
  const l = auth.user?.last_name?.[0] ?? ''
  return (f + l).toUpperCase() || '?'
})

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.app-layout {
  display: flex;
  min-height: 100vh;
  background: #f1f5f9;
}

/* ── Mobile top bar ──────────────────────────────────────── */
.mobile-bar {
  display: none;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 56px;
  background: #0f172a;
  align-items: center;
  padding: 0 1rem;
  gap: 0.75rem;
  z-index: 100;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.hamburger {
  background: transparent;
  border: none;
  color: #94a3b8;
  cursor: pointer;
  padding: 0.4rem;
  border-radius: 8px;
  display: flex;
  align-items: center;
  transition: background 0.15s, color 0.15s;
}
.hamburger:hover { background: rgba(255, 255, 255, 0.08); color: #e2e8f0; }

.mobile-logo {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.95rem;
  font-weight: 700;
  color: #f8fafc;
  letter-spacing: -0.01em;
}

/* ── Overlay ─────────────────────────────────────────────── */
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(2px);
  z-index: 150;
}
.overlay-enter-active, .overlay-leave-active { transition: opacity 0.2s; }
.overlay-enter-from, .overlay-leave-to { opacity: 0; }

/* ── Sidebar ─────────────────────────────────────────────── */
.sidebar {
  width: 240px;
  min-width: 240px;
  background: #0f172a;
  display: flex;
  flex-direction: column;
  padding: 1.5rem 1rem;
  gap: 0.5rem;
  position: sticky;
  top: 0;
  height: 100vh;
  overflow-y: auto;
}

.sidebar-close {
  display: none;
  background: transparent;
  border: none;
  color: #475569;
  cursor: pointer;
  padding: 0.35rem;
  border-radius: 8px;
  align-self: flex-end;
  margin-bottom: 0.5rem;
  transition: background 0.15s, color 0.15s;
}
.sidebar-close:hover { background: rgba(255, 255, 255, 0.06); color: #94a3b8; }

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  padding: 0 0.5rem;
  margin-bottom: 1.75rem;
}
.logo-text {
  font-size: 1.05rem;
  font-weight: 700;
  color: #f8fafc;
  letter-spacing: -0.01em;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
  flex: 1;
}

.nav-group-label {
  font-size: 0.65rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: #475569;
  padding: 0 0.75rem;
  margin-bottom: 0.25rem;
  display: block;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.6rem 0.75rem;
  border-radius: 10px;
  color: #94a3b8;
  text-decoration: none;
  font-size: 0.9rem;
  font-weight: 500;
  transition: background 0.15s, color 0.15s;
}
.nav-item :deep(svg) { flex-shrink: 0; color: #475569; transition: color 0.15s; }
.nav-item:hover { background: rgba(255,255,255,0.06); color: #e2e8f0; }
.nav-item:hover :deep(svg) { color: #94a3b8; }
.nav-item.active { background: rgba(129,140,248,0.15); color: #818cf8; }
.nav-item.active :deep(svg) { color: #818cf8; }

.sidebar-footer {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding-top: 1rem;
  border-top: 1px solid rgba(255,255,255,0.07);
  margin-top: auto;
}

.user-card {
  display: flex;
  align-items: center;
  gap: 0.65rem;
  flex: 1;
  min-width: 0;
  padding: 0.5rem 0.6rem;
  border-radius: 10px;
  text-decoration: none;
  transition: background 0.15s;
}
.user-card:hover { background: rgba(255,255,255,0.06); }
.user-card.active { background: rgba(129,140,248,0.15); }

.avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.72rem;
  font-weight: 700;
  flex-shrink: 0;
  letter-spacing: 0.03em;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 0.05rem;
  min-width: 0;
}
.user-name {
  font-size: 0.8rem;
  font-weight: 600;
  color: #e2e8f0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.user-handle {
  font-size: 0.7rem;
  color: #475569;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.logout-btn {
  background: transparent;
  border: 1px solid rgba(255,255,255,0.1);
  color: #475569;
  width: 34px;
  height: 34px;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: background 0.15s, color 0.15s, border-color 0.15s;
}
.logout-btn:hover {
  background: rgba(239,68,68,0.15);
  border-color: rgba(239,68,68,0.3);
  color: #f87171;
}

/* ── Content ─────────────────────────────────────────────── */
.content-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.content {
  flex: 1;
  padding: 2.5rem;
  max-width: 1100px;
}

/* ── Mobile ──────────────────────────────────────────────── */
@media (max-width: 768px) {
  .mobile-bar {
    display: flex;
  }

  .sidebar {
    position: fixed;
    top: 0;
    left: 0;
    height: 100vh;
    z-index: 200;
    transform: translateX(-100%);
    transition: transform 0.25s ease;
    min-width: 0;
    width: 260px;
  }
  .sidebar.open {
    transform: translateX(0);
  }

  .sidebar-close {
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .content-wrapper {
    padding-top: 56px;
  }

  .content {
    padding: 1.5rem 1rem;
  }
}
</style>
