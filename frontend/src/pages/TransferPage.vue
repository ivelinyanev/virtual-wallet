<template>
  <div class="transfer-page">
    <div class="page-header">
      <div>
        <h2>Send Money</h2>
        <p class="subtitle">Transfer funds to another user instantly</p>
      </div>
    </div>

    <div class="transfer-card">

      <!-- ── Step 1: Recipient ────────────────────────────────── -->
      <div class="step-section">
        <span class="step-label">
          <span class="step-number">1</span>
          Recipient
        </span>
        <div class="search-wrapper">
          <div class="input-wrap" :class="{ focused: searchFocused }">
            <UserIcon :size="16" class="input-icon" />
            <input
              v-model="recipientQuery"
              placeholder="Search by username…"
              autocomplete="off"
              @input="searchUsers"
              @focus="searchFocused = true"
              @blur="searchFocused = false"
            />
          </div>
          <div v-if="searchResults.length" class="dropdown">
            <button
              v-for="user in searchResults"
              :key="user.id"
              type="button"
              class="dropdown-item"
              @click="selectRecipient(user)"
            >
              <div class="user-avatar">{{ user.username[0]?.toUpperCase() }}</div>
              <div class="user-info">
                <span class="user-name">{{ user.username }}</span>
                <span class="user-full">{{ user.first_name }} {{ user.last_name }}</span>
              </div>
            </button>
          </div>
        </div>

        <div v-if="selectedRecipient" class="selected-chip">
          <div class="chip-avatar">{{ selectedRecipient.username[0]?.toUpperCase() }}</div>
          <div class="chip-info">
            <span class="chip-username">{{ selectedRecipient.username }}</span>
            <span class="chip-name">{{ selectedRecipient.first_name }} {{ selectedRecipient.last_name }}</span>
          </div>
          <button class="chip-clear" @click="selectedRecipient = null; recipientQuery = ''">
            <XIcon :size="14" />
          </button>
        </div>
      </div>

      <!-- ── Step 2: From wallet ──────────────────────────────── -->
      <div class="step-section">
        <span class="step-label">
          <span class="step-number">2</span>
          From Wallet
        </span>
        <div class="wallet-options">
          <button
            v-for="w in walletStore.wallets"
            :key="w.id"
            type="button"
            class="wallet-option"
            :class="{ selected: selectedWalletName === w.wallet_name, [w.currency.toLowerCase()]: true }"
            @click="selectedWalletName = w.wallet_name"
          >
            <span class="wallet-option-currency">{{ w.currency }}</span>
            <span class="wallet-option-name">{{ w.wallet_name }}</span>
            <span class="wallet-option-balance">{{ formatCurrency(w.balance, w.currency) }}</span>
          </button>
          <p v-if="!walletStore.wallets.length" class="no-wallets">
            No wallets yet. <RouterLink to="/wallets">Create one</RouterLink>.
          </p>
        </div>
      </div>

      <!-- ── Step 3: Amount ──────────────────────────────────── -->
      <div class="step-section">
        <span class="step-label">
          <span class="step-number">3</span>
          Amount
        </span>
        <div class="amount-row">
          <div class="input-wrap amount-wrap" :class="{ focused: amountFocused }">
            <span class="currency-prefix">
              {{ selectedWallet ? currencySymbol(selectedWallet.currency) : '€' }}
            </span>
            <input
              v-model.number="amount"
              type="number"
              min="0.01"
              step="0.01"
              placeholder="0.00"
              class="amount-input"
              @focus="amountFocused = true"
              @blur="amountFocused = false"
            />
            <span class="currency-suffix">{{ selectedWallet?.currency ?? '' }}</span>
          </div>
        </div>

        <div class="quick-amounts">
          <button
            v-for="q in quickAmounts"
            :key="q"
            type="button"
            class="quick-btn"
            :class="{ selected: amount === q }"
            @click="amount = q"
          >
            {{ selectedWallet ? currencySymbol(selectedWallet.currency) : '' }}{{ q }}
          </button>
        </div>
      </div>

      <!-- ── Summary preview ─────────────────────────────────── -->
      <div v-if="selectedRecipient && selectedWallet && Number(amount) > 0" class="summary-box">
        <div class="summary-row">
          <span class="summary-label">Sending</span>
          <span class="summary-value">{{ formatCurrency(Number(amount), selectedWallet.currency) }}</span>
        </div>
        <div class="summary-row">
          <span class="summary-label">From</span>
          <span class="summary-value">{{ selectedWallet.wallet_name }}</span>
        </div>
        <div class="summary-row">
          <span class="summary-label">To</span>
          <span class="summary-value">{{ selectedRecipient.username }}</span>
        </div>
        <div class="summary-note">
          <InfoIcon :size="13" />
          If {{ selectedRecipient.first_name }} has no {{ selectedWallet.currency }} wallet, funds are auto-converted.
        </div>
      </div>

      <!-- ── Feedback ────────────────────────────────────────── -->
      <p v-if="error" class="form-error">
        <AlertCircleIcon :size="14" />
        {{ error }}
      </p>

      <div v-if="success" class="success-banner">
        <CheckCircleIcon :size="16" />
        Transfer sent successfully!
      </div>

      <button
        class="btn-primary send-btn"
        :disabled="!canSubmit || loading"
        @click="handleTransfer"
      >
        <LoaderCircleIcon v-if="loading" :size="16" class="spinner" />
        <SendIcon v-else :size="16" />
        {{ loading ? 'Sending…' : 'Send Money' }}
      </button>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useTransfer } from '@/composables/useTransfer'
import { useFormatters } from '@/composables/useFormatters'
import { currencies } from '@/composables/useWallets'
import type { Currency } from '@/types'
import {
  UserIcon,
  XIcon,
  InfoIcon,
  AlertCircleIcon,
  CheckCircleIcon,
  LoaderCircleIcon,
  SendIcon,
} from 'lucide-vue-next'

const {
  walletStore,
  recipientQuery, searchResults, selectedRecipient,
  selectedWalletName, selectedWallet,
  amount, error, success, loading, canSubmit,
  searchUsers, selectRecipient, handleTransfer,
} = useTransfer()

const { formatCurrency } = useFormatters()

const searchFocused = ref(false)
const amountFocused = ref(false)
const quickAmounts = [10, 50, 100, 500]

function currencySymbol(code: Currency) {
  return currencies.find((c) => c.code === code)?.symbol ?? code
}

onMounted(() => walletStore.fetchWallets())
</script>

<style scoped>
.transfer-page { max-width: 560px; margin: 0 auto; }

.page-header { margin-bottom: 2rem; }
h2 { margin: 0 0 0.25rem; font-size: 1.75rem; font-weight: 700; color: var(--c-text); }
.subtitle { margin: 0; color: var(--c-text-muted); font-size: 0.95rem; }

/* ── Card ──────────────────────────────────────────────────── */
.transfer-card {
  background: var(--c-surface);
  border-radius: var(--radius-xl);
  padding: 2rem;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--c-border-soft);
  display: flex;
  flex-direction: column;
  gap: 1.75rem;
}

/* ── Steps ─────────────────────────────────────────────────── */
.step-section { display: flex; flex-direction: column; gap: 0.75rem; }

.step-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--c-text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}
.step-number {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: var(--c-primary-light);
  color: var(--c-primary);
  font-size: 0.7rem;
  font-weight: 700;
}

/* ── Recipient search ──────────────────────────────────────── */
.search-wrapper { position: relative; }

.dropdown {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  right: 0;
  background: var(--c-surface);
  border: 1px solid var(--c-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  z-index: 20;
  overflow: hidden;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  width: 100%;
  padding: 0.75rem 1rem;
  background: none;
  border: none;
  text-align: left;
  cursor: pointer;
  transition: background 0.15s;
}
.dropdown-item:hover { background: var(--c-surface-mute); }

.user-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: var(--c-primary-light);
  color: var(--c-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.85rem;
  font-weight: 700;
  flex-shrink: 0;
}

.user-info { display: flex; flex-direction: column; gap: 0.1rem; }
.user-name { font-size: 0.875rem; font-weight: 600; color: var(--c-text); }
.user-full { font-size: 0.75rem; color: var(--c-text-subtle); }

/* Selected recipient chip */
.selected-chip {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.625rem 0.875rem;
  background: var(--c-primary-soft);
  border: 1px solid var(--c-primary-border);
  border-radius: var(--radius-lg);
}
.chip-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--c-primary);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  font-weight: 700;
  flex-shrink: 0;
}
.chip-info { display: flex; flex-direction: column; gap: 0.05rem; flex: 1; }
.chip-username { font-size: 0.875rem; font-weight: 600; color: var(--c-primary-hover); }
.chip-name { font-size: 0.75rem; color: var(--c-text-muted); }
.chip-clear {
  background: none;
  border: none;
  color: var(--c-text-subtle);
  cursor: pointer;
  padding: 0.2rem;
  border-radius: 4px;
  display: flex;
  align-items: center;
  transition: color 0.15s, background 0.15s;
}
.chip-clear:hover { background: var(--c-primary-border); color: var(--c-primary-hover); }

/* ── Wallet options ────────────────────────────────────────── */
.wallet-options { display: flex; flex-direction: column; gap: 0.5rem; }

.wallet-option {
  display: flex;
  align-items: center;
  gap: 0.875rem;
  padding: 0.875rem 1rem;
  border: 1.5px solid var(--c-border);
  border-radius: var(--radius-lg);
  background: var(--c-surface-soft);
  cursor: pointer;
  text-align: left;
  transition: border-color 0.15s, background 0.15s, box-shadow 0.15s;
  width: 100%;
}
.wallet-option:hover { border-color: var(--c-primary-border); background: var(--c-primary-soft); }
.wallet-option.selected {
  border-color: var(--c-primary);
  background: var(--c-primary-soft);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.wallet-option-currency {
  font-size: 0.7rem;
  font-weight: 700;
  letter-spacing: 0.06em;
  padding: 0.2rem 0.55rem;
  border-radius: 20px;
  flex-shrink: 0;
}
.wallet-option.eur .wallet-option-currency { background: #dbeafe; color: #1d4ed8; }
.wallet-option.usd .wallet-option-currency { background: #dcfce7; color: #15803d; }
.wallet-option.gbp .wallet-option-currency { background: #f3e8ff; color: #7e22ce; }

.wallet-option-name { font-size: 0.9rem; font-weight: 600; color: var(--c-text); flex: 1; }
.wallet-option.selected .wallet-option-name { color: var(--c-primary-hover); }

.wallet-option-balance { font-size: 0.85rem; font-weight: 600; color: var(--c-text-secondary); flex-shrink: 0; }
.wallet-option.selected .wallet-option-balance { color: var(--c-primary); }

.no-wallets { font-size: 0.875rem; color: var(--c-text-subtle); }
.no-wallets a { color: var(--c-primary); text-decoration: none; }

/* ── Amount ────────────────────────────────────────────────── */
.amount-wrap { gap: 0; }

.currency-prefix {
  padding: 0 0.75rem;
  font-size: 1rem;
  font-weight: 600;
  color: var(--c-text-muted);
  border-right: 1.5px solid var(--c-border);
  line-height: 1;
  flex-shrink: 0;
}
.amount-input {
  text-align: right;
  font-size: 1.1rem;
  font-weight: 600;
  padding-right: 0 !important;
}
.currency-suffix {
  padding: 0 0.75rem;
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--c-text-subtle);
  border-left: 1.5px solid var(--c-border);
  line-height: 1;
  flex-shrink: 0;
}

/* Remove browser number input arrows */
.amount-input::-webkit-inner-spin-button,
.amount-input::-webkit-outer-spin-button { -webkit-appearance: none; margin: 0; }
.amount-input[type=number] { -moz-appearance: textfield; }

.quick-amounts {
  display: flex;
  gap: 0.4rem;
  margin-top: 0.625rem;
}
.quick-btn {
  flex: 1;
  padding: 0.45rem 0;
  border: 1.5px solid var(--c-border);
  border-radius: var(--radius-sm);
  background: var(--c-surface-soft);
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--c-text-secondary);
  cursor: pointer;
  transition: all 0.15s;
}
.quick-btn:hover { border-color: var(--c-primary-border); color: var(--c-primary-hover); background: var(--c-primary-soft); }
.quick-btn.selected { border-color: var(--c-primary); background: var(--c-primary-light); color: var(--c-primary-hover); }

/* ── Summary ───────────────────────────────────────────────── */
.summary-box {
  background: var(--c-surface-soft);
  border: 1px solid var(--c-border-soft);
  border-radius: var(--radius-lg);
  padding: 1rem 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
}
.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.875rem;
}
.summary-label { color: var(--c-text-muted); }
.summary-value { font-weight: 600; color: var(--c-text); }
.summary-note {
  display: flex;
  align-items: flex-start;
  gap: 0.4rem;
  font-size: 0.775rem;
  color: var(--c-text-subtle);
  padding-top: 0.4rem;
  border-top: 1px solid var(--c-border-soft);
  margin-top: 0.1rem;
  line-height: 1.4;
}
.summary-note :deep(svg) { flex-shrink: 0; margin-top: 1px; }

/* ── Success banner ────────────────────────────────────────── */
.success-banner {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: var(--c-success-soft);
  border: 1px solid var(--c-success-border);
  border-radius: var(--radius-md);
  color: var(--c-success);
  font-size: 0.875rem;
  font-weight: 600;
}

/* ── Send button ───────────────────────────────────────────── */
.send-btn { width: 100%; justify-content: center; font-size: 1rem; padding: 0.85rem; }
</style>
