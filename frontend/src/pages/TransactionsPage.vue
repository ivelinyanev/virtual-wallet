<template>
  <div>
    <h2>Transactions</h2>

    <div class="filters">
      <input v-model="filters.counterparty" type="text" placeholder="Filter by counterparty" @change="load" />
      <select v-model="filters.direction" @change="load">
        <option value="">All directions</option>
        <option value="IN">Incoming</option>
        <option value="OUT">Outgoing</option>
      </select>
      <input v-model="filters.start_date" type="date" @change="load" />
      <input v-model="filters.end_date" type="date" @change="load" />
    </div>

    <div v-if="loading" class="loading">Loading…</div>

    <div v-else>
      <table class="tx-table">
        <thead>
          <tr>
            <th>Date</th>
            <th>Type</th>
            <th>Counterparty</th>
            <th>Amount</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="tx in transactions" :key="tx.id">
            <td>{{ formatDate(tx.timestamp) }}</td>
            <td><span class="badge" :class="tx.type">{{ tx.type.replace(/_/g, ' ') }}</span></td>
            <td>{{ tx.counterparty_username ?? '—' }}</td>
            <td :class="{ credit: tx.type !== 'TRANSFER_OUT', debit: tx.type === 'TRANSFER_OUT' }">
              {{ tx.type === 'TRANSFER_OUT' ? '-' : '+' }}{{ formatCurrency(tx.amount, tx.currency) }}
            </td>
            <td><span class="status" :class="tx.status.toLowerCase()">{{ tx.status }}</span></td>
          </tr>
          <tr v-if="!transactions.length">
            <td colspan="5" class="empty">No transactions found.</td>
          </tr>
        </tbody>
      </table>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="pagination">
        <button :disabled="page === 0" @click="goTo(page - 1)">‹ Prev</button>
        <span>Page {{ page + 1 }} of {{ totalPages }}</span>
        <button :disabled="page >= totalPages - 1" @click="goTo(page + 1)">Next ›</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { transactionsApi } from '@/api/transactions'
import type { TransactionResponse } from '@/types'

const transactions = ref<TransactionResponse[]>([])
const loading = ref(false)
const page = ref(0)
const totalPages = ref(1)

const filters = ref<{
  counterparty: string
  direction: '' | 'IN' | 'OUT'
  start_date: string
  end_date: string
}>({ counterparty: '', direction: '', start_date: '', end_date: '' })

async function load() {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: 15,
      ...(filters.value.counterparty && { counterparty: filters.value.counterparty }),
      ...(filters.value.direction && { direction: filters.value.direction }),
      ...(filters.value.start_date && { start_date: filters.value.start_date }),
      ...(filters.value.end_date && { end_date: filters.value.end_date }),
    }
    const { data } = await transactionsApi.getMyTransactions(params)
    transactions.value = data.content
    totalPages.value = data.total_pages
  } finally {
    loading.value = false
  }
}

function goTo(p: number) {
  page.value = p
  load()
}

function formatCurrency(amount: number, currency = 'EUR') {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency }).format(amount)
}

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}

onMounted(load)
</script>

<style scoped>
h2 { margin: 0 0 1.5rem; color: #1a1a2e; }

.filters {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
  margin-bottom: 1.5rem;
}

.filters input, .filters select {
  padding: 0.55rem 0.75rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 0.875rem;
  outline: none;
  background: white;
}

.tx-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 6px rgba(0,0,0,0.06);
}

.tx-table th {
  text-align: left;
  padding: 0.875rem 1rem;
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: #888;
  background: #fafafa;
  border-bottom: 1px solid #eee;
}

.tx-table td {
  padding: 0.875rem 1rem;
  font-size: 0.9rem;
  border-bottom: 1px solid #f5f5f5;
  color: #333;
}

.tx-table tr:last-child td { border-bottom: none; }

.badge {
  font-size: 0.75rem;
  padding: 0.2rem 0.6rem;
  border-radius: 20px;
  font-weight: 600;
}

.badge.TOP_UP { background: #c6f6d5; color: #276749; }
.badge.TRANSFER_IN { background: #bee3f8; color: #2a69ac; }
.badge.TRANSFER_OUT { background: #fed7d7; color: #9b2c2c; }

.credit { color: #38a169; font-weight: 600; }
.debit { color: #e53e3e; font-weight: 600; }

.status {
  font-size: 0.75rem;
  padding: 0.2rem 0.5rem;
  border-radius: 20px;
}

.status.completed { background: #c6f6d5; color: #276749; }
.status.pending { background: #fefcbf; color: #744210; }
.status.failed { background: #fed7d7; color: #9b2c2c; }

.empty { text-align: center; color: #888; padding: 2rem; }
.loading { color: #888; }

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 1.25rem;
}

.pagination button {
  background: white;
  border: 1px solid #ddd;
  padding: 0.4rem 0.875rem;
  border-radius: 8px;
  cursor: pointer;
}

.pagination button:disabled { opacity: 0.4; cursor: not-allowed; }
</style>
