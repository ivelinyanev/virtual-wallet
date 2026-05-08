<template>
  <div>
    <h2>Admin — All Transactions</h2>
    <div v-if="loading" class="loading">Loading…</div>
    <table v-else class="table">
      <thead>
        <tr>
          <th>ID</th><th>Date</th><th>Type</th><th>Amount</th><th>Status</th><th>Counterparty</th><th>Counterparty Wallet</th><th>Owner</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="tx in transactions" :key="tx.id">
          <td>{{ tx.id }}</td>
          <td>{{ formatDate(tx.timestamp) }}</td>
          <td>{{ tx.type }}</td>
          <td>{{ formatCurrency(tx.amount, tx.currency) }}</td>
          <td>{{ tx.status }}</td>
          <td>{{ tx.counterparty_wallet_owner_username ?? '—' }}</td>
          <td>{{ tx.counter_party_wallet_name ?? '—' }}</td>
          <td>{{ tx.wallet_owner_username ?? '—' }}</td>
        </tr>
        <tr v-if="!transactions.length"><td colspan="6" class="empty">No transactions.</td></tr>
      </tbody>
    </table>
    <div v-if="totalPages > 1" class="pagination">
      <button :disabled="page === 0" @click="goTo(page - 1)">‹ Prev</button>
      <span>Page {{ page + 1 }} of {{ totalPages }}</span>
      <button :disabled="page >= totalPages - 1" @click="goTo(page + 1)">Next ›</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { transactionsApi } from '@/api/transactions'
import type { AdminTransactionResponse } from '@/types'

const transactions = ref<AdminTransactionResponse[]>([])
const loading = ref(false)
const page = ref(0)
const totalPages = ref(1)

async function load() {
  loading.value = true
  try {
    const { data } = await transactionsApi.getAllAdmin({ page: page.value, size: 20 })
    transactions.value = data.content
    totalPages.value = data.total_pages
  } finally {
    loading.value = false
  }
}

function goTo(p: number) { page.value = p; load() }
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
.loading { color: #888; }

.table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 6px rgba(0,0,0,0.06);
}

.table th {
  text-align: left;
  padding: 0.875rem 1rem;
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: #888;
  background: #fafafa;
  border-bottom: 1px solid #eee;
}

.table td {
  padding: 0.875rem 1rem;
  font-size: 0.9rem;
  border-bottom: 1px solid #f5f5f5;
}

.empty { text-align: center; color: #888; padding: 2rem; }

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

.pagination button:disabled { opacity: 0.4; }
</style>
