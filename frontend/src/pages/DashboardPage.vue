<template>
  <div class="dashboard">
    <!-- Header -->
    <div class="page-header">
      <div>
        <h2>Welcome back, {{ auth.user?.first_name }}</h2>
        <p class="subtitle">Here's your financial overview</p>
      </div>
    </div>

    <!-- Stats -->
    <div class="summary-grid">
      <div class="stat-card accent">
        <div class="stat-icon accent-icon"><CircleDollarSignIcon :size="26" /></div>
        <div>
          <span class="label">{{ isSingleCurrency ? 'Total Balance' : 'Balances' }}</span>
          <!-- Single currency: show normal total -->
          <span v-if="isSingleCurrency" class="value">
            {{ formatCurrency([...balanceByCurrency.entries()][0]?.[1] ?? 0, [...balanceByCurrency.entries()][0]?.[0]) }}
          </span>
          <!-- Multi-currency: show per-currency breakdown -->
          <span v-else class="value multi-balance">
            <span v-for="[currency, amount] in balanceByCurrency" :key="currency" class="balance-line">
              {{ formatCurrency(amount, currency) }}
            </span>
          </span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon"><LandmarkIcon :size="26" /></div>
        <div>
          <span class="label">Wallets</span>
          <span class="value">{{ walletStore.wallets.length }}</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon"><CreditCardIcon :size="26" /></div>
        <div>
          <span class="label">Cards</span>
          <span class="value">{{ cardCount }}</span>
        </div>
      </div>
    </div>

    <!-- Wallets -->
    <div class="section">
      <div class="section-header">
        <h3>My Wallets</h3>
        <RouterLink to="/wallets" class="link-btn">Manage</RouterLink>
      </div>
      <div class="wallet-grid">
        <div v-for="wallet in walletStore.wallets" :key="wallet.id" class="wallet-card">
          <div class="wallet-top">
            <span class="wallet-currency-badge">{{ wallet.currency }}</span>
          </div>
          <span class="wallet-balance">{{ formatCurrency(wallet.balance, wallet.currency) }}</span>
          <span class="wallet-name">{{ wallet.wallet_name }}</span>
        </div>
        <p v-if="!walletStore.wallets.length" class="empty">
          No wallets yet. <RouterLink to="/wallets">Create one</RouterLink>.
        </p>
      </div>
    </div>

    <!-- Recent Transactions -->
    <div class="section">
      <div class="section-header">
        <h3>Recent Transactions</h3>
        <RouterLink to="/transactions" class="link-btn">View all</RouterLink>
      </div>

      <div v-if="recentTransactions.length" class="tx-list">
        <div
          v-for="tx in recentTransactions"
          :key="tx.id"
          class="tx-card"
          :class="{ expanded: expandedId === tx.id }"
        >
          <!-- Summary row (always visible) -->
          <div class="tx-summary" @click="toggle(tx.id)">
            <div class="tx-left">
              <span class="tx-icon" :class="tx.type.toLowerCase()">
                <ArrowDownCircleIcon v-if="tx.type === 'TRANSFER_IN'" :size="28" />
                <ArrowUpCircleIcon v-else-if="tx.type === 'TRANSFER_OUT'" :size="28" />
                <PlusCircleIcon v-else :size="28" />
              </span>
              <div class="tx-info">
                <span class="tx-type-label">{{ txLabel(tx.type) }}</span>
                <span class="tx-counterparty">{{ tx.counterparty_username ?? 'Virtual Wallet' }}</span>
              </div>
            </div>
            <div class="tx-right">
              <span class="tx-amount" :class="amountClass(tx.type)">
                {{ tx.type === 'TRANSFER_OUT' ? '−' : '+' }}{{ formatCurrency(Math.abs(tx.amount), tx.currency) }}
              </span>
              <span class="tx-date">{{ formatDate(tx.timestamp) }}</span>
            </div>
            <ChevronRightIcon class="chevron" :class="{ open: expandedId === tx.id }" :size="18" />
          </div>

          <!-- Detail panel -->
          <div class="tx-detail-outer" :class="{ expanded: expandedId === tx.id }">
            <div class="tx-detail">
              <div v-if="loadingDetail && !details[tx.id]" class="detail-loading">Loading…</div>
              <div v-else-if="details[tx.id]" class="detail-grid">
                <div class="detail-item">
                  <span class="detail-label">Transaction ID</span>
                  <span class="detail-value">#{{ details[tx.id]?.id }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">Status</span>
                  <span class="detail-value status-badge" :class="details[tx.id]?.status.toLowerCase()">
                    {{ details[tx.id]?.status }}
                  </span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">Amount</span>
                  <span class="detail-value">{{ formatCurrency(Math.abs(details[tx.id]?.amount ?? 0), details[tx.id]?.currency) }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">Your wallet</span>
                  <span class="detail-value">{{ details[tx.id]?.wallet_name ?? '—' }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">Type</span>
                  <span class="detail-value">{{ details[tx.id]?.type ? txLabel(details[tx.id]!.type) : '' }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">{{ details[tx.id]?.type === 'TRANSFER_OUT' ? 'Sent to' : details[tx.id]?.type === 'TRANSFER_IN' ? 'Received from' : 'Source' }}</span>
                  <span class="detail-value">{{ details[tx.id]?.counterparty_username ?? '—' }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">Date & Time</span>
                  <span class="detail-value">{{ details[tx.id]?.timestamp ? formatDateTime(details[tx.id]!.timestamp) : '' }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <p v-else class="empty">No recent transactions.</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useDashboard } from '@/composables/useDashboard'
import { useTransactionDetail } from '@/composables/useTransactionDetail'
import { useFormatters } from '@/composables/useFormatters'
import {
  CircleDollarSignIcon,
  LandmarkIcon,
  CreditCardIcon,
  ArrowDownCircleIcon,
  ArrowUpCircleIcon,
  PlusCircleIcon,
  ChevronRightIcon,
} from 'lucide-vue-next'

const auth = useAuthStore()
const { walletStore, recentTransactions, cardCount, balanceByCurrency, isSingleCurrency, loadDashboard } = useDashboard()
const { expandedId, details, loadingDetail, toggle } = useTransactionDetail()
const { formatCurrency, formatDate, formatDateTime, txLabel, amountClass } = useFormatters()

onMounted(loadDashboard)
</script>

<style scoped>
.dashboard {
  max-width: 860px;
  margin: 0 auto;
}

/* Header */
.page-header {
  margin-bottom: 2rem;
}
h2 {
  margin: 0 0 0.25rem;
  font-size: 1.75rem;
  font-weight: 700;
  color: #0f172a;
}
.subtitle {
  margin: 0;
  color: #64748b;
  font-size: 0.95rem;
}

/* Stats */
.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 1.25rem 1.5rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  box-shadow: 0 1px 3px rgba(0,0,0,0.07), 0 1px 2px rgba(0,0,0,0.04);
  border: 1px solid #f1f5f9;
  transition: box-shadow 0.2s;
}
.stat-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.stat-card.accent {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border: none;
}
.stat-card.accent .label { color: rgba(255,255,255,0.75); }
.stat-card.accent .value { color: white; }

.stat-icon { display: flex; align-items: center; color: #6366f1; }
.stat-icon.accent-icon { color: white; }
.stat-card > div { display: flex; flex-direction: column; gap: 0.2rem; }
.label { font-size: 0.75rem; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.06em; font-weight: 600; }
.value { font-size: 1.5rem; font-weight: 700; color: #0f172a; }

.multi-balance {
  display: flex;
  flex-direction: column;
  gap: 0.1rem;
}
.balance-line {
  font-size: 1.1rem;
  font-weight: 700;
  line-height: 1.3;
}

/* Section */
.section { margin-bottom: 2.5rem; }
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}
.section-header h3 { margin: 0; font-size: 1.05rem; font-weight: 600; color: #0f172a; }
.link-btn {
  font-size: 0.8rem;
  font-weight: 600;
  color: #6366f1;
  text-decoration: none;
  padding: 0.3rem 0.8rem;
  border: 1px solid #e0e7ff;
  border-radius: 20px;
  background: #f5f3ff;
  transition: background 0.15s;
}
.link-btn:hover { background: #ede9fe; }

/* Wallets */
.wallet-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1rem;
}

.wallet-card {
  background: linear-gradient(135deg, #1e293b, #334155);
  border-radius: 16px;
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
  color: white;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}
.wallet-top { display: flex; justify-content: flex-end; }
.wallet-currency-badge {
  font-size: 0.7rem;
  font-weight: 700;
  padding: 0.2rem 0.5rem;
  background: rgba(255,255,255,0.15);
  border-radius: 20px;
  letter-spacing: 0.05em;
}
.wallet-balance { font-size: 1.4rem; font-weight: 700; }
.wallet-name { font-size: 0.8rem; color: rgba(255,255,255,0.6); }

/* Transactions */
.tx-list { display: flex; flex-direction: column; gap: 0.5rem; }

.tx-card {
  background: white;
  border-radius: 14px;
  border: 1px solid #f1f5f9;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  overflow: hidden;
  transition: box-shadow 0.2s;
}
.tx-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.09); }
.tx-card.expanded { border-color: #c7d2fe; box-shadow: 0 0 0 2px #e0e7ff; }

.tx-summary {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem 1.25rem;
  cursor: pointer;
  user-select: none;
}

.tx-left { display: flex; align-items: center; gap: 0.875rem; flex: 1; min-width: 0; }
.tx-icon { display: flex; align-items: center; flex-shrink: 0; }
.tx-icon.transfer_in :deep(svg) { color: #16a34a; }
.tx-icon.transfer_out :deep(svg) { color: #dc2626; }
.tx-icon.top_up :deep(svg) { color: #6366f1; }
.tx-info { display: flex; flex-direction: column; gap: 0.1rem; min-width: 0; }
.tx-type-label { font-size: 0.875rem; font-weight: 600; color: #0f172a; }
.tx-counterparty { font-size: 0.78rem; color: #94a3b8; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

.tx-right { display: flex; flex-direction: column; align-items: flex-end; gap: 0.1rem; flex-shrink: 0; }
.tx-amount { font-size: 0.95rem; font-weight: 700; }
.tx-amount.credit { color: #16a34a; }
.tx-amount.debit { color: #dc2626; }
.tx-date { font-size: 0.75rem; color: #94a3b8; }

.chevron {
  color: #cbd5e1;
  transition: transform 0.25s;
  flex-shrink: 0;
}
.chevron.open { transform: rotate(90deg); }

/* Detail panel */
.tx-detail-outer {
  display: grid;
  grid-template-rows: 0fr;
  opacity: 0;
  transition: grid-template-rows 0.3s cubic-bezier(0.4, 0, 0.2, 1), opacity 0.25s ease;
}
.tx-detail-outer.expanded {
  grid-template-rows: 1fr;
  opacity: 1;
}
.tx-detail {
  overflow: hidden;
  padding: 1rem 1.25rem 1.25rem;
  border-top: 1px solid #f1f5f9;
  background: #fafafa;
}

.detail-loading { color: #94a3b8; font-size: 0.85rem; }

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 0.875rem;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}
.detail-label { font-size: 0.7rem; font-weight: 600; text-transform: uppercase; letter-spacing: 0.06em; color: #94a3b8; }
.detail-value { font-size: 0.9rem; font-weight: 500; color: #1e293b; }

.status-badge {
  display: inline-block;
  padding: 0.15rem 0.6rem;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 700;
}
.status-badge.successful { background: #dcfce7; color: #166534; }
.status-badge.pending { background: #fef9c3; color: #854d0e; }
.status-badge.failed { background: #fee2e2; color: #991b1b; }

.empty { color: #94a3b8; font-size: 0.9rem; }
.empty a { color: #6366f1; text-decoration: none; }
</style>
