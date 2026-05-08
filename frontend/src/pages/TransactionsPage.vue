<template>
  <div class="transactions">
    <!-- Header -->
    <div class="page-header">
      <h2>Transactions</h2>
      <p class="subtitle">Your full transaction history</p>
    </div>

    <!-- Filters -->
    <div class="filters-bar">
      <!-- Type pills -->
      <div class="pill-group">
        <button
          v-for="opt in typeOptions"
          :key="opt.value"
          class="pill"
          :class="{ active: filters.type === opt.value, [opt.color]: filters.type === opt.value }"
          @click="setType(opt.value)"
        >
          <component :is="opt.icon" :size="13" />
          {{ opt.label }}
        </button>
      </div>

      <div class="filter-divider" />

      <!-- Status pills -->
      <div class="pill-group">
        <button
          v-for="opt in statusOptions"
          :key="opt.value"
          class="pill"
          :class="{ active: filters.status === opt.value, [opt.color]: filters.status === opt.value }"
          @click="setStatus(opt.value)"
        >{{ opt.label }}</button>
      </div>

      <div class="filter-divider" />

      <!-- Date range popover -->
      <div ref="dateWrapRef" class="date-popover-wrap">
        <button class="date-trigger" :class="{ active: dateOpen || hasDateFilter }" @click="dateOpen = !dateOpen">
          <CalendarIcon :size="14" />
          <span>{{ dateRangeLabel }}</span>
          <ChevronDownIcon :size="13" :class="['date-chevron', { rotated: dateOpen }]" />
        </button>

        <transition
          enter-active-class="popover-enter-active"
          enter-from-class="popover-enter-from"
          leave-active-class="popover-leave-active"
          leave-to-class="popover-leave-to"
        >
          <div v-if="dateOpen" class="date-panel" @click.stop>
            <div class="date-fields">
              <div class="date-field">
                <label>From</label>
                <input v-model="filters.from" type="datetime-local" @change="resetAndLoad" />
              </div>
              <div class="date-field">
                <label>To</label>
                <input v-model="filters.to" type="datetime-local" @change="resetAndLoad" />
              </div>
            </div>
            <div class="date-panel-footer">
              <button class="panel-clear-btn" @click="clearDates(); dateOpen = false">Clear dates</button>
              <button class="panel-apply-btn" @click="dateOpen = false">Apply</button>
            </div>
          </div>
        </transition>
      </div>

      <button v-if="hasActiveFilters" class="clear-all-btn" @click="clearFilters">
        <XIcon :size="13" /> Reset
      </button>
    </div>

    <!-- Loading skeleton -->
    <div v-if="loading" class="skeleton-list">
      <div v-for="i in 8" :key="i" class="skeleton-row" />
    </div>

    <!-- Transaction list -->
    <div v-else-if="transactions.length" class="tx-list">
      <div
        v-for="tx in transactions"
        :key="tx.id"
        class="tx-card"
        :class="{ expanded: expandedId === tx.id }"
        @click="toggle(tx.id)"
      >
        <div class="tx-summary">
          <span class="tx-icon" :class="tx.type.toLowerCase()">
            <ArrowDownCircleIcon v-if="tx.type === 'TRANSFER_IN'" :size="26" />
            <ArrowUpCircleIcon v-else-if="tx.type === 'TRANSFER_OUT'" :size="26" />
            <PlusCircleIcon v-else :size="26" />
          </span>

          <div class="tx-info">
            <span class="tx-type">{{ txLabel(tx.type) }}</span>
            <span class="tx-counterparty">{{ tx.counterparty_username ?? 'Virtual Wallet' }}</span>
          </div>

          <div class="tx-meta">
            <span class="tx-amount" :class="tx.type === 'TRANSFER_OUT' ? 'debit' : 'credit'">
              {{ tx.type === 'TRANSFER_OUT' ? '−' : '+' }}{{ formatCurrency(Math.abs(tx.amount), tx.currency) }}
            </span>
            <span class="tx-date">{{ formatDate(tx.timestamp) }}</span>
          </div>

          <span class="status-pill" :class="tx.status.toLowerCase()">{{ statusLabel(tx.status) }}</span>
          <ChevronRightIcon class="chevron" :class="{ open: expandedId === tx.id }" :size="16" />
        </div>

        <!-- Expandable detail -->
        <div class="tx-detail-outer" :class="{ expanded: expandedId === tx.id }">
          <div class="tx-detail">
            <div class="detail-grid">
              <div class="detail-item">
                <span class="detail-label">Transaction ID</span>
                <span class="detail-value mono">#{{ tx.id }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">Your wallet</span>
                <span class="detail-value">{{ tx.wallet_name ?? '—' }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">{{ tx.type === 'TRANSFER_OUT' ? 'Sent to' : tx.type === 'TRANSFER_IN' ? 'Received from' : 'Source' }}</span>
                <span class="detail-value">{{ tx.counterparty_username ?? '—' }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">Amount</span>
                <span class="detail-value">{{ formatCurrency(Math.abs(tx.amount), tx.currency) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">Date &amp; Time</span>
                <span class="detail-value">{{ formatDateTime(tx.timestamp) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <ReceiptIcon :size="40" class="empty-icon" />
      <p>No transactions found.</p>
      <span v-if="hasActiveFilters" class="empty-hint">Try clearing the filters.</span>
    </div>

    <!-- Pagination -->
    <div v-if="totalPages > 1" class="pagination">
      <button class="page-btn" :disabled="page === 0" @click="goTo(0)">«</button>
      <button class="page-btn" :disabled="page === 0" @click="goTo(page - 1)">‹</button>
      <button
        v-for="p in visiblePages"
        :key="p"
        class="page-btn"
        :class="{ active: p === page }"
        @click="goTo(p)"
      >{{ p + 1 }}</button>
      <button class="page-btn" :disabled="page >= totalPages - 1" @click="goTo(page + 1)">›</button>
      <button class="page-btn" :disabled="page >= totalPages - 1" @click="goTo(totalPages - 1)">»</button>
      <span class="page-info">{{ page + 1 }} / {{ totalPages }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useTransactions } from '@/composables/useTransactions'
import { useFormatters } from '@/composables/useFormatters'
import {
  ArrowDownCircleIcon,
  ArrowUpCircleIcon,
  PlusCircleIcon,
  ChevronRightIcon,
  ChevronDownIcon,
  XIcon,
  ReceiptIcon,
  CalendarIcon,
} from 'lucide-vue-next'

const {
  transactions, loading, page, totalPages, expandedId, filters,
  typeOptions, statusOptions,
  hasActiveFilters, hasDateFilter, dateRangeLabel, visiblePages,
  load, resetAndLoad, setType, setStatus, clearDates, clearFilters, goTo, toggle,
} = useTransactions()

const { formatCurrency, formatDate, formatDateTime, txLabel, statusLabel } = useFormatters()

const dateOpen = ref(false)
const dateWrapRef = ref<HTMLElement | null>(null)

function onClickOutside(e: MouseEvent) {
  if (dateWrapRef.value && !dateWrapRef.value.contains(e.target as Node)) {
    dateOpen.value = false
  }
}

onMounted(() => {
  load()
  document.addEventListener('click', onClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', onClickOutside)
})
</script>

<style scoped>
.transactions {
  max-width: 860px;
  margin: 0 auto;
}

/* Header */
.page-header { margin-bottom: 1.75rem; }
h2 {
  margin: 0 0 0.25rem;
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--c-text);
}
.subtitle { margin: 0; color: var(--c-text-muted); font-size: 0.95rem; }

/* Filters */
.filters-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  align-items: center;
  margin-bottom: 1.5rem;
  padding: 0.625rem 0.875rem;
  background: white;
  border-radius: 14px;
  border: 1px solid #f1f5f9;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.filter-divider {
  width: 1px;
  height: 24px;
  background: #e2e8f0;
  flex-shrink: 0;
}

/* Pill toggle buttons */
.pill-group {
  display: flex;
  gap: 0.25rem;
}

.pill {
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  padding: 0.35rem 0.75rem;
  border-radius: 20px;
  border: 1px solid transparent;
  background: transparent;
  font-size: 0.8rem;
  font-weight: 500;
  color: #64748b;
  cursor: pointer;
  transition: background 0.15s, color 0.15s, border-color 0.15s, box-shadow 0.15s;
  white-space: nowrap;
}
.pill:hover { background: #f1f5f9; color: #1e293b; }

.pill.active.indigo { background: #eef2ff; color: #4f46e5; border-color: #c7d2fe; }
.pill.active.green  { background: #f0fdf4; color: #16a34a; border-color: #bbf7d0; }
.pill.active.red    { background: #fef2f2; color: #dc2626; border-color: #fecaca; }
.pill.active.amber  { background: #fffbeb; color: #d97706; border-color: #fde68a; }

/* Date range popover trigger */
.date-popover-wrap { position: relative; }

.date-trigger {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.35rem 0.75rem;
  border-radius: 20px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  font-size: 0.8rem;
  font-weight: 500;
  color: #64748b;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s, color 0.15s, box-shadow 0.15s;
  white-space: nowrap;
}
.date-trigger:hover,
.date-trigger.active {
  background: #eef2ff;
  border-color: #c7d2fe;
  color: #4f46e5;
}

.date-chevron {
  transition: transform 0.2s cubic-bezier(0.4,0,0.2,1);
  color: #94a3b8;
}
.date-chevron.rotated { transform: rotate(180deg); }

/* Date popover panel */
.date-panel {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  z-index: 50;
  min-width: 300px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  box-shadow: 0 8px 30px rgba(0,0,0,0.12);
  padding: 1rem;
}

.date-fields {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
  margin-bottom: 0.875rem;
}

.date-field { display: flex; flex-direction: column; gap: 0.3rem; }
.date-field label {
  font-size: 0.68rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: #94a3b8;
}
.date-field input {
  padding: 0.45rem 0.625rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.8rem;
  color: #1e293b;
  background: #f8fafc;
  outline: none;
  transition: border-color 0.15s, box-shadow 0.15s;
}
.date-field input:focus {
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99,102,241,0.12);
  background: white;
}

.date-panel-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  padding-top: 0.75rem;
  border-top: 1px solid #f1f5f9;
}

.panel-clear-btn {
  padding: 0.4rem 0.875rem;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  background: white;
  font-size: 0.8rem;
  font-weight: 500;
  color: #64748b;
  cursor: pointer;
  transition: background 0.15s;
}
.panel-clear-btn:hover { background: #f8fafc; }

.panel-apply-btn {
  padding: 0.4rem 0.875rem;
  border-radius: 8px;
  border: none;
  background: #6366f1;
  font-size: 0.8rem;
  font-weight: 600;
  color: white;
  cursor: pointer;
  transition: background 0.15s;
}
.panel-apply-btn:hover { background: #4f46e5; }

/* Popover transition */
.popover-enter-active,
.popover-leave-active {
  transition: opacity 0.15s ease, transform 0.15s cubic-bezier(0.4,0,0.2,1);
}
.popover-enter-from,
.popover-leave-to {
  opacity: 0;
  transform: translateY(-6px) scale(0.97);
}

/* Reset all button */
.clear-all-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  padding: 0.35rem 0.75rem;
  border-radius: 20px;
  border: 1px solid #fecaca;
  background: #fef2f2;
  font-size: 0.78rem;
  font-weight: 600;
  color: #dc2626;
  cursor: pointer;
  margin-left: auto;
  transition: background 0.15s;
}
.clear-all-btn:hover { background: #fee2e2; }

/* Skeleton */
.skeleton-list { display: flex; flex-direction: column; gap: 0.5rem; }
.skeleton-row {
  height: 68px;
  border-radius: 14px;
  background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}
@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* Transaction cards */
.tx-list { display: flex; flex-direction: column; gap: 0.5rem; }

.tx-card {
  background: white;
  border-radius: 14px;
  border: 1px solid #f1f5f9;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.2s, border-color 0.2s;
}
.tx-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.08); }
.tx-card.expanded {
  border-color: #c7d2fe;
  box-shadow: 0 0 0 2px #e0e7ff;
}

.tx-summary {
  display: flex;
  align-items: center;
  gap: 0.875rem;
  padding: 0.875rem 1.25rem;
  user-select: none;
}

.tx-icon { display: flex; align-items: center; flex-shrink: 0; }
.tx-icon.transfer_in :deep(svg) { color: #16a34a; }
.tx-icon.transfer_out :deep(svg) { color: #dc2626; }
.tx-icon.top_up :deep(svg) { color: #6366f1; }

.tx-info {
  display: flex;
  flex-direction: column;
  gap: 0.1rem;
  flex: 1;
  min-width: 0;
}
.tx-type { font-size: 0.875rem; font-weight: 600; color: #0f172a; }
.tx-counterparty {
  font-size: 0.78rem;
  color: #94a3b8;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.tx-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.1rem;
  flex-shrink: 0;
}
.tx-amount { font-size: 0.95rem; font-weight: 700; }
.tx-amount.credit { color: #16a34a; }
.tx-amount.debit { color: #dc2626; }
.tx-date { font-size: 0.75rem; color: #94a3b8; }

.status-pill {
  flex-shrink: 0;
  font-size: 0.7rem;
  font-weight: 700;
  padding: 0.2rem 0.6rem;
  border-radius: 20px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}
.status-pill.successful { background: #dcfce7; color: #166534; }
.status-pill.pending    { background: #fef9c3; color: #854d0e; }
.status-pill.failed     { background: #fee2e2; color: #991b1b; }

.chevron {
  color: #cbd5e1;
  flex-shrink: 0;
  transition: transform 0.25s cubic-bezier(0.4,0,0.2,1);
}
.chevron.open { transform: rotate(90deg); }

/* Expandable detail — CSS Grid trick, no JS */
.tx-detail-outer {
  display: grid;
  grid-template-rows: 0fr;
  opacity: 0;
  transition: grid-template-rows 0.3s cubic-bezier(0.4,0,0.2,1), opacity 0.25s ease;
}
.tx-detail-outer.expanded {
  grid-template-rows: 1fr;
  opacity: 1;
}
.tx-detail {
  overflow: hidden;
  padding: 0 1.25rem;
  border-top: 0px solid #f1f5f9;
  background: #fafafa;
  transition: padding 0.3s cubic-bezier(0.4,0,0.2,1), border-top-width 0.3s;
}
.tx-detail-outer.expanded .tx-detail {
  padding: 1rem 1.25rem 1.25rem;
  border-top-width: 1px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 0.875rem;
}
.detail-item { display: flex; flex-direction: column; gap: 0.2rem; }
.detail-label {
  font-size: 0.68rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: #94a3b8;
}
.detail-value { font-size: 0.875rem; font-weight: 500; color: #1e293b; }
.detail-value.mono { font-family: monospace; }

/* Empty state */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  padding: 3rem 1rem;
  color: #94a3b8;
}
.empty-icon { color: #cbd5e1; }
.empty-state p { margin: 0; font-size: 0.95rem; font-weight: 500; color: #64748b; }
.empty-hint { font-size: 0.8rem; }

/* Pagination */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.375rem;
  margin-top: 1.5rem;
  flex-wrap: wrap;
}
.page-btn {
  min-width: 36px;
  height: 36px;
  padding: 0 0.5rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: white;
  color: #475569;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s, color 0.15s;
}
.page-btn:hover:not(:disabled) {
  background: #f5f3ff;
  border-color: #c7d2fe;
  color: #6366f1;
}
.page-btn.active {
  background: #6366f1;
  border-color: #6366f1;
  color: white;
}
.page-btn:disabled { opacity: 0.35; cursor: not-allowed; }
.page-info {
  margin-left: 0.5rem;
  font-size: 0.8rem;
  color: #94a3b8;
  font-weight: 500;
}
</style>
