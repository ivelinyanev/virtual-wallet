<template>
  <div class="transfer-page">
    <h2>Send Money</h2>

    <div class="transfer-card">
      <!-- Step 1: Search recipient -->
      <div class="step">
        <label>Recipient</label>
        <div class="search-wrapper">
          <input
            v-model="recipientQuery"
            type="text"
            placeholder="Search by username…"
            @input="searchUsers"
          />
          <div v-if="searchResults.length" class="dropdown">
            <button
              v-for="user in searchResults"
              :key="user.id"
              type="button"
              class="dropdown-item"
              @click="selectRecipient(user)"
            >
              {{ user.username }} — {{ user.first_name }} {{ user.last_name }}
            </button>
          </div>
        </div>
        <div v-if="selectedRecipient" class="selected-user">
          Selected: <strong>{{ selectedRecipient.username }}</strong>
        </div>
      </div>

      <!-- Step 2: Pick sender wallet -->
      <div class="step">
        <label>From Wallet</label>
        <select v-model="senderWalletId">
          <option disabled value="">— choose —</option>
          <option v-for="w in walletStore.wallets" :key="w.id" :value="w.id">
            {{ w.wallet_name }} ({{ formatCurrency(w.balance, w.currency) }})
          </option>
        </select>
      </div>

      <!-- Step 3: Recipient wallet ID -->
      <div class="step">
        <label>Recipient Wallet ID</label>
        <input v-model.number="recipientWalletId" type="number" placeholder="Enter wallet ID" />
      </div>

      <!-- Step 4: Amount -->
      <div class="step">
        <label>Amount</label>
        <input v-model.number="amount" type="number" min="0.01" step="0.01" placeholder="0.00" />
      </div>

      <p v-if="error" class="error">{{ error }}</p>
      <p v-if="success" class="success">Transfer completed successfully!</p>

      <button class="btn-primary" :disabled="!canSubmit || loading" @click="handleTransfer">
        {{ loading ? 'Sending…' : 'Send Money' }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useTransfer } from '@/composables/useTransfer'
import { useFormatters } from '@/composables/useFormatters'

const {
  walletStore,
  recipientQuery, searchResults, selectedRecipient,
  senderWalletId, recipientWalletId, amount,
  error, success, loading, canSubmit,
  searchUsers, selectRecipient, handleTransfer,
} = useTransfer()

const { formatCurrency } = useFormatters()

onMounted(() => walletStore.fetchWallets())
</script>

<style scoped>
h2 { margin: 0 0 1.5rem; color: #1a1a2e; }

.transfer-card {
  background: white;
  border-radius: 14px;
  padding: 2rem;
  max-width: 480px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.07);
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.step { display: flex; flex-direction: column; gap: 0.35rem; }
label { font-size: 0.875rem; color: #555; font-weight: 500; }

.search-wrapper { position: relative; }

input, select {
  padding: 0.65rem 0.75rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 1rem;
  width: 100%;
  box-sizing: border-box;
  outline: none;
  transition: border 0.2s;
}

input:focus, select:focus { border-color: #7c83fd; }

.dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  z-index: 10;
  overflow: hidden;
}

.dropdown-item {
  display: block;
  width: 100%;
  padding: 0.75rem 1rem;
  background: none;
  border: none;
  text-align: left;
  cursor: pointer;
  font-size: 0.9rem;
}

.dropdown-item:hover { background: #f5f6fa; }

.selected-user {
  font-size: 0.875rem;
  color: #38a169;
  margin-top: 0.25rem;
}

/* .btn-primary comes from global — only override layout specifics */
.btn-primary { width: 100%; justify-content: center; padding: 0.85rem; margin-top: 0.5rem; font-size: 1rem; }

.error   { color: var(--c-danger); font-size: 0.875rem; }
.success { color: var(--c-success); font-size: 0.875rem; }
</style>
