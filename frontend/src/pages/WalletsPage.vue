<template>
  <div>
    <div class="page-header">
      <h2>Wallets</h2>
      <button class="btn-primary" @click="showCreate = true">+ New Wallet</button>
    </div>

    <div v-if="walletStore.loading" class="loading">Loading wallets…</div>

    <div v-else class="wallet-grid">
      <div v-for="wallet in walletStore.wallets" :key="wallet.id" class="wallet-card">
        <div class="card-header">
          <span class="wallet-name">{{ wallet.name }}</span>
          <span class="currency-badge">{{ wallet.currency }}</span>
        </div>
        <div class="balance">{{ formatCurrency(wallet.balance, wallet.currency) }}</div>
        <div class="card-actions">
          <button class="btn-secondary" @click="openTopUp(wallet)">Top Up</button>
          <button class="btn-danger" @click="confirmDelete(wallet.id)">Delete</button>
        </div>
      </div>

      <p v-if="!walletStore.wallets.length" class="empty">No wallets yet. Create your first one!</p>
    </div>

    <!-- Create Modal -->
    <div v-if="showCreate" class="modal-overlay" @click.self="showCreate = false">
      <div class="modal">
        <h3>New Wallet</h3>
        <form @submit.prevent="handleCreate">
          <div class="field">
            <label>Name</label>
            <input v-model="createForm.name" required />
          </div>
          <div class="field">
            <label>Currency</label>
            <select v-model="createForm.currency">
              <option value="EUR">EUR</option>
              <option value="USD">USD</option>
              <option value="GBP">GBP</option>
            </select>
          </div>
          <p v-if="createError" class="error">{{ createError }}</p>
          <div class="modal-actions">
            <button type="button" @click="showCreate = false">Cancel</button>
            <button type="submit" class="btn-primary" :disabled="createLoading">Create</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Top-Up Modal -->
    <div v-if="topUpWallet" class="modal-overlay" @click.self="topUpWallet = null">
      <div class="modal">
        <h3>Top Up — {{ topUpWallet.name }}</h3>
        <form @submit.prevent="handleTopUp">
          <div class="field">
            <label>Amount ({{ topUpWallet.currency }})</label>
            <input v-model.number="topUpAmount" type="number" min="0.01" step="0.01" required />
          </div>
          <p v-if="topUpError" class="error">{{ topUpError }}</p>
          <div class="modal-actions">
            <button type="button" @click="topUpWallet = null">Cancel</button>
            <button type="submit" class="btn-primary" :disabled="topUpLoading">Confirm</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useWalletStore } from '@/stores/wallet'
import type { PrivateWalletDto, Currency } from '@/types'

const walletStore = useWalletStore()

const showCreate = ref(false)
const createForm = ref<{ name: string; currency: Currency }>({ name: '', currency: 'EUR' })
const createError = ref('')
const createLoading = ref(false)

const topUpWallet = ref<PrivateWalletDto | null>(null)
const topUpAmount = ref(0)
const topUpError = ref('')
const topUpLoading = ref(false)

function formatCurrency(amount: number, currency = 'EUR') {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency }).format(amount)
}

function openTopUp(wallet: PrivateWalletDto) {
  topUpWallet.value = wallet
  topUpAmount.value = 0
  topUpError.value = ''
}

async function confirmDelete(id: number) {
  if (!confirm('Are you sure you want to delete this wallet?')) return
  try {
    await walletStore.deleteWallet(id)
  } catch (e: any) {
    alert(e.response?.data?.message ?? 'Could not delete wallet.')
  }
}

async function handleCreate() {
  createError.value = ''
  createLoading.value = true
  try {
    await walletStore.createWallet(createForm.value)
    showCreate.value = false
    createForm.value = { name: '', currency: 'EUR' }
  } catch (e: any) {
    createError.value = e.response?.data?.message ?? 'Failed to create wallet.'
  } finally {
    createLoading.value = false
  }
}

async function handleTopUp() {
  if (!topUpWallet.value) return
  topUpError.value = ''
  topUpLoading.value = true
  try {
    await walletStore.topUp({ wallet_id: topUpWallet.value.id, amount: topUpAmount.value })
    topUpWallet.value = null
  } catch (e: any) {
    topUpError.value = e.response?.data?.message ?? 'Top-up failed.'
  } finally {
    topUpLoading.value = false
  }
}

onMounted(() => walletStore.fetchWallets())
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

h2 { margin: 0; color: #1a1a2e; }

.wallet-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 1rem;
}

.wallet-card {
  background: white;
  border-radius: 14px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.07);
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.card-header { display: flex; justify-content: space-between; align-items: center; }
.wallet-name { font-weight: 600; font-size: 1rem; }
.currency-badge {
  background: #7c83fd22;
  color: #7c83fd;
  padding: 0.2rem 0.6rem;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
}

.balance { font-size: 1.8rem; font-weight: 700; color: #1a1a2e; }

.card-actions { display: flex; gap: 0.5rem; }

.btn-primary {
  background: #7c83fd;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.875rem;
}

.btn-secondary {
  background: #f0f0f0;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.875rem;
  flex: 1;
}

.btn-danger {
  background: #fff0f0;
  color: #e53e3e;
  border: 1px solid #fed7d7;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.875rem;
}

.empty { color: #888; }
.loading { color: #888; }

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal {
  background: white;
  border-radius: 14px;
  padding: 2rem;
  width: 100%;
  max-width: 360px;
}

.modal h3 { margin: 0 0 1.25rem; }

.field { display: flex; flex-direction: column; gap: 0.25rem; margin-bottom: 1rem; }
label { font-size: 0.875rem; color: #555; }

input, select {
  padding: 0.6rem 0.75rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 1rem;
  outline: none;
}

input:focus, select:focus { border-color: #7c83fd; }

.modal-actions { display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 0.5rem; }

.error { color: #e53e3e; font-size: 0.875rem; }
</style>
