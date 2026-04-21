import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean
    requiresAdmin?: boolean
    requiresGuest?: boolean
  }
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // Guest-only routes
    {
      path: '/login',
      name: 'login',
      component: () => import('@/pages/LoginPage.vue'),
      meta: { requiresGuest: true },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/pages/RegisterPage.vue'),
      meta: { requiresGuest: true },
    },
    {
      path: '/verify',
      name: 'verify',
      component: () => import('@/pages/VerifyPage.vue'),
      meta: { requiresAuth: true },
    },

    // Authenticated routes (wrapped in DashboardLayout)
    {
      path: '/',
      component: () => import('@/layouts/DashboardLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'dashboard',
          component: () => import('@/pages/DashboardPage.vue'),
        },
        {
          path: 'wallets',
          name: 'wallets',
          component: () => import('@/pages/WalletsPage.vue'),
        },
        {
          path: 'cards',
          name: 'cards',
          component: () => import('@/pages/CardsPage.vue'),
        },
        {
          path: 'transactions',
          name: 'transactions',
          component: () => import('@/pages/TransactionsPage.vue'),
        },
        {
          path: 'transfer',
          name: 'transfer',
          component: () => import('@/pages/TransferPage.vue'),
        },
        {
          path: 'profile',
          name: 'profile',
          component: () => import('@/pages/ProfilePage.vue'),
        },
        // Admin
        {
          path: 'admin',
          meta: { requiresAdmin: true },
          children: [
            {
              path: 'users',
              name: 'admin-users',
              component: () => import('@/pages/Admin/UsersPage.vue'),
            },
            {
              path: 'transactions',
              name: 'admin-transactions',
              component: () => import('@/pages/Admin/TransactionsPage.vue'),
            },
          ],
        },
      ],
    },

    // Fallback
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()

  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: 'login' }
  }

  if (to.meta.requiresAdmin && !auth.isAdmin) {
    return { name: 'dashboard' }
  }

  if (to.meta.requiresGuest && auth.isAuthenticated) {
    return { name: 'dashboard' }
  }
})

export default router
