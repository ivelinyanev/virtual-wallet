<template>
  <div>
    <h2>Welcome, {{ auth.user?.first_name }}</h2>
    <p class="subtitle">Here's your financial overview.</p>

    <div class="summary-grid">
      <div class="stat-card">
        <span class="label">Total Balance</span>
        <span class="value">{{ formatCurrency(totalBalance) }}</span>
      </div>
      <div class="stat-card">
        <span class="label">Wallets</span>
        <span class="value">{{ walletStore.wallets.length }}</span>
      </div>
      <div class="stat-card">
        <span class="label">Cards</span>
        <span class="value">{{ cardCount }}</span>
      </div>
    </div>

    <div class="section">
      <h3>My Wallets</h3>
      <div class="wallet-list">
        <div v-for="wallet in walletStore.wallets" :key="wallet.id" class="wallet-card">
          <span class="wallet-name">{{ wallet.name }}</span>
          <span class="wallet-balance">{{ formatCurrency(wallet.balance, wallet.currency) }}</span>
        </div>
        <p v-if="!walletStore.wallets.length" class="empty">No wallets yet. <RouterLink to="/wallets">Create one</RouterLink>.</p>
      </div>
    </div>

    <div class="section">
      <div class="section-header">
        <h3>Recent Transactions</h3>
        <RouterLink to="/transactions">View all</RouterLink>
      </div>
      <div v-if="recentTransactions.length" class="tx-list">
        <div v-for="tx in recentTransactions" :key="tx.id" class="tx-row">
          <span class="tx-type" :class="tx.type">{{ tx.type.replace('_', ' ') }}</span>
          <span class="tx-counterparty">{{ tx.counterparty_username ?? '—' }}</span>
          <span class="tx-amount" :class="{ credit: tx.type === 'TRANSFER_IN' || tx.type === 'TOP_UP' }">
            {{ tx.type === 'TRANSFER_OUT' ? '-' : '+' }}{{ formatCurrency(tx.amount, tx.currency) }}
          </span>
          <span class="tx-date">{{ formatDate(tx.timestamp) }}</span>
        </div>
      </div>
      <p v-else class="empty">No recent transactions.</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useWalletStore } from '@/stores/wallet'
import { transactionsApi } from '@/api/transactions'
import { cardsApi } from '@/api/cards'
import type { TransactionResponse } from '@/types'

const auth = useAuthStore()
const walletStore = useWalletStore()

const recentTransactions = ref<TransactionResponse[]>([])
const cardCount = ref(0)

const totalBalance = computed(() =>
  walletStore.wallets.reduce((sum, w) => sum + w.balance, 0),
)

function formatCurrency(amount: number, currency = 'EUR') {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency }).format(amount)
}

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}

onMounted(async () => {
  await walletStore.fetchWallets()
  const [txRes, cardRes] = await Promise.allSettled([
    transactionsApi.getMyTransactions({ page: 0, size: 5 }),
    cardsApi.getMyCards(),
  ])
  if (txRes.status === 'fulfilled') recentTransactions.value = txRes.value.data.content
  if (cardRes.status === 'fulfilled') cardCount.value = cardRes.value.data.length
})
</script>

<style scoped>
h2 { margin: 0 0 0.25rem; color: #1a1a2e; }
.subtitle { color: #666; margin: 0 0 2rem; }

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  box-shadow: 0 1px 6px rgba(0,0,0,0.06);
}

.label { font-size: 0.8rem; color: #888; text-transform: uppercase; letter-spacing: 0.05em; }
.value { font-size: 1.6rem; font-weight: 700; color: #1a1a2e; }

.section { margin-bottom: 2rem; }
.section h3 { margin: 0 0 1rem; font-size: 1.1rem; color: #1a1a2e; }

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.section-header a { font-size: 0.875rem; color: #7c83fd; text-decoration: none; }

.wallet-list { display: flex; flex-direction: column; gap: 0.5rem; }

.wallet-card {
  background: white;
  border-radius: 10px;
  padding: 1rem 1.25rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
}

.wallet-name { font-weight: 500; }
.wallet-balance { font-weight: 700; color: #1a1a2e; }

.tx-list { display: flex; flex-direction: column; gap: 0.5rem; }

.tx-row {
  background: white;
  border-radius: 10px;
  padding: 0.875rem 1.25rem;
  display: grid;
  grid-template-columns: 130px 1fr auto auto;
  gap: 1rem;
  align-items: center;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
}

.tx-type {
  font-size: 0.8rem;
  font-weight: 600;
  padding: 0.2rem 0.5rem;
  border-radius: 20px;
  background: #eee;
  text-align: center;
}

.tx-type.TOP_UP { background: #c6f6d5; color: #276749; }
.tx-type.TRANSFER_IN { background: #bee3f8; color: #2a69ac; }
.tx-type.TRANSFER_OUT { background: #fed7d7; color: #9b2c2c; }

.tx-counterparty { color: #555; font-size: 0.9rem; }

.tx-amount { font-weight: 700; color: #e53e3e; }
.tx-amount.credit { color: #38a169; }

.tx-date { font-size: 0.8rem; color: #888; white-space: nowrap; }

.empty { color: #888; font-size: 0.9rem; }
.empty a { color: #7c83fd; text-decoration: none; }
</style>
