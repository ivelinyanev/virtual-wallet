<template>
  <div class="transfer-page">
    <div class="page-header">
      <div>
        <h2>Transfer</h2>
        <p class="subtitle">Send money to someone or move between your wallets</p>
      </div>
    </div>

    <!-- ── Tabs ─────────────────────────────────────────────── -->
    <div class="tabs">
      <button class="tab" :class="{ active: tab === 'user' }" @click="tab = 'user'">
        <SendIcon :size="15" />
        Send to User
      </button>
      <button class="tab" :class="{ active: tab === 'internal' }" @click="tab = 'internal'">
        <ArrowLeftRightIcon :size="15" />
        Between My Wallets
      </button>
    </div>

    <!-- ══════════════════════════════════════════════════════ -->
    <!--  TAB 1: Send to User                                  -->
    <!-- ══════════════════════════════════════════════════════ -->
    <div v-if="tab === 'user'" class="transfer-card">

      <!-- Step 1: Recipient -->
      <div class="step-section">
        <span class="step-label"><span class="step-number">1</span>Recipient</span>
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

      <!-- Step 2: From Wallet -->
      <div class="step-section">
        <span class="step-label"><span class="step-number">2</span>From Wallet</span>
        <div class="wallet-options">
          <button
            v-for="w in transferStore.wallets"
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
          <p v-if="!transferStore.wallets.length" class="no-wallets">
            No wallets yet. <RouterLink to="/wallets">Create one</RouterLink>.
          </p>
        </div>
      </div>

      <!-- Step 3: Amount -->
      <div class="step-section">
        <span class="step-label"><span class="step-number">3</span>Amount</span>
        <div class="input-wrap amount-wrap" :class="{ focused: amountFocused }">
          <span class="currency-prefix">{{ selectedWallet ? currencySymbol(selectedWallet.currency) : '€' }}</span>
          <input
            v-model.number="sendAmount"
            type="number" min="0.01" step="0.01" placeholder="0.00"
            class="amount-input"
            @focus="amountFocused = true"
            @blur="amountFocused = false"
          />
          <span class="currency-suffix">{{ selectedWallet?.currency ?? '' }}</span>
        </div>
        <div class="quick-amounts">
          <button
            v-for="q in quickAmounts" :key="q" type="button"
            class="quick-btn" :class="{ selected: sendAmount === q }"
            @click="sendAmount = q"
          >{{ selectedWallet ? currencySymbol(selectedWallet.currency) : '' }}{{ q }}</button>
        </div>
      </div>

      <!-- Summary -->
      <div v-if="selectedRecipient && selectedWallet && Number(sendAmount) > 0" class="summary-box">
        <div class="summary-row">
          <span class="summary-label">Sending</span>
          <span class="summary-value">{{ formatCurrency(Number(sendAmount), selectedWallet.currency) }}</span>
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

      <p v-if="sendError" class="form-error"><AlertCircleIcon :size="14" />{{ sendError }}</p>
      <div v-if="sendSuccess" class="success-banner"><CheckCircleIcon :size="16" />Transfer sent successfully!</div>

      <button class="btn-primary send-btn" :disabled="!canSubmit || sendLoading" @click="handleTransfer">
        <LoaderCircleIcon v-if="sendLoading" :size="16" class="spinner" />
        <SendIcon v-else :size="16" />
        {{ sendLoading ? 'Sending…' : 'Send Money' }}
      </button>
    </div>

    <!-- ══════════════════════════════════════════════════════ -->
    <!--  TAB 2: Between My Wallets                            -->
    <!-- ══════════════════════════════════════════════════════ -->
    <div v-else class="transfer-card">

      <!-- Wallet pair selector -->
      <div class="step-section">
        <span class="step-label"><span class="step-number">1</span>Wallets</span>

        <div class="wallet-pair">
          <!-- FROM -->
          <div class="wallet-side">
            <p class="side-label">From</p>
            <div class="wallet-options">
              <button
                v-for="w in internalStore.wallets"
                :key="w.id"
                type="button"
                class="wallet-option"
                :class="{
                  selected: fromWallet?.id === w.id,
                  [w.currency.toLowerCase()]: true,
                  disabled: toWallet?.id === w.id,
                }"
                :disabled="toWallet?.id === w.id"
                @click="fromWallet = w"
              >
                <span class="wallet-option-currency">{{ w.currency }}</span>
                <span class="wallet-option-name">{{ w.wallet_name }}</span>
                <span class="wallet-option-balance">{{ formatCurrency(w.balance, w.currency) }}</span>
              </button>
            </div>
          </div>

          <!-- Swap button -->
          <button
            class="swap-btn"
            type="button"
            :disabled="!fromWallet || !toWallet"
            title="Swap wallets"
            @click="swapWallets"
          >
            <ArrowLeftRightIcon :size="18" />
          </button>

          <!-- TO -->
          <div class="wallet-side">
            <p class="side-label">To</p>
            <div class="wallet-options">
              <button
                v-for="w in internalStore.wallets"
                :key="w.id"
                type="button"
                class="wallet-option"
                :class="{
                  selected: toWallet?.id === w.id,
                  [w.currency.toLowerCase()]: true,
                  disabled: fromWallet?.id === w.id,
                }"
                :disabled="fromWallet?.id === w.id"
                @click="toWallet = w"
              >
                <span class="wallet-option-currency">{{ w.currency }}</span>
                <span class="wallet-option-name">{{ w.wallet_name }}</span>
                <span class="wallet-option-balance">{{ formatCurrency(w.balance, w.currency) }}</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Dual-amount input -->
      <div class="step-section">
        <span class="step-label"><span class="step-number">2</span>Amount</span>

        <div class="dual-amount">
          <!-- You send -->
          <div class="dual-field" :class="{ active: fromWallet }">
            <label class="dual-label">You send</label>
            <div class="input-wrap amount-wrap">
              <span class="currency-prefix">{{ fromWallet ? currencySymbol(fromWallet.currency) : '—' }}</span>
              <input
                :value="fromAmount"
                type="number" min="0.01" step="0.01" placeholder="0.00"
                class="amount-input"
                :disabled="!fromWallet || !toWallet"
                @input="onFromInput(($event.target as HTMLInputElement).valueAsNumber || '')"
              />
              <span class="currency-suffix">{{ fromWallet?.currency ?? '' }}</span>
            </div>
          </div>

          <!-- Conversion arrow -->
          <div class="conversion-arrow">
            <ArrowRightIcon v-if="!rateLoading" :size="16" />
            <LoaderCircleIcon v-else :size="16" class="spinner" />
            <span v-if="rate !== null && fromWallet && toWallet && fromWallet.currency !== toWallet.currency" class="rate-hint">
              1 {{ fromWallet.currency }} = {{ rate.toFixed(4) }} {{ toWallet.currency }}
            </span>
          </div>

          <!-- You receive -->
          <div class="dual-field" :class="{ active: toWallet }">
            <label class="dual-label">You receive</label>
            <div class="input-wrap amount-wrap">
              <span class="currency-prefix">{{ toWallet ? currencySymbol(toWallet.currency) : '—' }}</span>
              <input
                :value="toAmount"
                type="number" min="0.01" step="0.01" placeholder="0.00"
                class="amount-input"
                :disabled="!fromWallet || !toWallet"
                @input="onToInput(($event.target as HTMLInputElement).valueAsNumber || '')"
              />
              <span class="currency-suffix">{{ toWallet?.currency ?? '' }}</span>
            </div>
          </div>
        </div>

        <!-- Balance check hint -->
        <p v-if="fromWallet && Number(fromAmount) > fromWallet.balance" class="balance-hint">
          <AlertCircleIcon :size="13" />
          Exceeds available balance of {{ formatCurrency(fromWallet.balance, fromWallet.currency) }}
        </p>
      </div>

      <!-- Summary -->
      <div v-if="fromWallet && toWallet && Number(fromAmount) > 0" class="summary-box">
        <div class="summary-row">
          <span class="summary-label">Deducting</span>
          <span class="summary-value">{{ formatCurrency(Number(fromAmount), fromWallet.currency) }}</span>
        </div>
        <div class="summary-row">
          <span class="summary-label">From</span>
          <span class="summary-value">{{ fromWallet.wallet_name }} ({{ formatCurrency(fromWallet.balance, fromWallet.currency) }})</span>
        </div>
        <div class="summary-row">
          <span class="summary-label">Crediting ~</span>
          <span class="summary-value accent">{{ formatCurrency(Number(toAmount), toWallet.currency) }}</span>
        </div>
        <div class="summary-row">
          <span class="summary-label">To</span>
          <span class="summary-value">{{ toWallet.wallet_name }}</span>
        </div>
        <div v-if="fromWallet.currency !== toWallet.currency" class="summary-note">
          <InfoIcon :size="13" />
          Exact received amount may vary slightly from the live rate at execution time.
        </div>
      </div>

      <p v-if="internalError" class="form-error"><AlertCircleIcon :size="14" />{{ internalError }}</p>
      <div v-if="internalSuccess" class="success-banner"><CheckCircleIcon :size="16" />Transfer completed successfully!</div>

      <button
        class="btn-primary send-btn"
        :disabled="!canInternalSubmit || internalLoading"
        @click="handleInternalTransfer"
      >
        <LoaderCircleIcon v-if="internalLoading" :size="16" class="spinner" />
        <ArrowLeftRightIcon v-else :size="16" />
        {{ internalLoading ? 'Processing…' : 'Move Money' }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useTransfer } from '@/composables/useTransfer'
import { useInternalTransfer } from '@/composables/useInternalTransfer'
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
  ArrowLeftRightIcon,
  ArrowRightIcon,
} from 'lucide-vue-next'

const tab = ref<'user' | 'internal'>('user')

// ── Send to User ─────────────────────────────────────────
const {
  walletStore: transferStore,
  recipientQuery, searchResults, selectedRecipient,
  selectedWalletName, selectedWallet,
  amount: sendAmount, error: sendError, success: sendSuccess, loading: sendLoading, canSubmit,
  searchUsers, selectRecipient, handleTransfer,
} = useTransfer()

// ── Between My Wallets ───────────────────────────────────
const {
  walletStore: internalStore,
  fromWallet, toWallet,
  fromAmount, toAmount,
  rate, rateLoading,
  loading: internalLoading, error: internalError, success: internalSuccess, canSubmit: canInternalSubmit,
  onFromInput, onToInput, swapWallets, handleInternalTransfer,
} = useInternalTransfer()

const { formatCurrency } = useFormatters()
const searchFocused = ref(false)
const amountFocused = ref(false)
const quickAmounts = [10, 50, 100, 500]

function currencySymbol(code: Currency) {
  return currencies.find((c) => c.code === code)?.symbol ?? code
}

onMounted(() => {
  transferStore.fetchWallets()
  internalStore.fetchWallets()
})
</script>

<style scoped>
.transfer-page { max-width: 640px; margin: 0 auto; }

.page-header { margin-bottom: 1.5rem; }
h2 { margin: 0 0 0.25rem; font-size: 1.75rem; font-weight: 700; color: var(--c-text); }
.subtitle { margin: 0; color: var(--c-text-muted); font-size: 0.95rem; }

/* ── Tabs ──────────────────────────────────────────────── */
.tabs {
  display: flex;
  gap: 0.25rem;
  background: var(--c-surface-soft);
  border: 1px solid var(--c-border-soft);
  border-radius: var(--radius-lg);
  padding: 0.25rem;
  margin-bottom: 1.5rem;
}
.tab {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.45rem;
  padding: 0.6rem 1rem;
  border: none;
  border-radius: calc(var(--radius-lg) - 2px);
  background: transparent;
  color: var(--c-text-muted);
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
}
.tab:hover { color: var(--c-text); background: var(--c-surface); }
.tab.active { background: var(--c-surface); color: var(--c-primary); box-shadow: var(--shadow-sm); }

/* ── Card ──────────────────────────────────────────────── */
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

/* ── Steps ─────────────────────────────────────────────── */
.step-section { display: flex; flex-direction: column; gap: 0.75rem; }
.step-label {
  display: flex; align-items: center; gap: 0.5rem;
  font-size: 0.8rem; font-weight: 600; color: var(--c-text-secondary);
  text-transform: uppercase; letter-spacing: 0.06em;
}
.step-number {
  display: inline-flex; align-items: center; justify-content: center;
  width: 20px; height: 20px; border-radius: 50%;
  background: var(--c-primary-light); color: var(--c-primary);
  font-size: 0.7rem; font-weight: 700;
}

/* ── Recipient search ──────────────────────────────────── */
.search-wrapper { position: relative; }
.dropdown {
  position: absolute; top: calc(100% + 6px); left: 0; right: 0;
  background: var(--c-surface); border: 1px solid var(--c-border);
  border-radius: var(--radius-lg); box-shadow: var(--shadow-lg); z-index: 20; overflow: hidden;
}
.dropdown-item {
  display: flex; align-items: center; gap: 0.75rem;
  width: 100%; padding: 0.75rem 1rem; background: none; border: none;
  text-align: left; cursor: pointer; transition: background 0.15s;
}
.dropdown-item:hover { background: var(--c-surface-mute); }
.user-avatar {
  width: 34px; height: 34px; border-radius: 50%;
  background: var(--c-primary-light); color: var(--c-primary);
  display: flex; align-items: center; justify-content: center;
  font-size: 0.85rem; font-weight: 700; flex-shrink: 0;
}
.user-info { display: flex; flex-direction: column; gap: 0.1rem; }
.user-name { font-size: 0.875rem; font-weight: 600; color: var(--c-text); }
.user-full { font-size: 0.75rem; color: var(--c-text-subtle); }

.selected-chip {
  display: flex; align-items: center; gap: 0.75rem;
  padding: 0.625rem 0.875rem;
  background: var(--c-primary-soft); border: 1px solid var(--c-primary-border);
  border-radius: var(--radius-lg);
}
.chip-avatar {
  width: 32px; height: 32px; border-radius: 50%;
  background: var(--c-primary); color: white;
  display: flex; align-items: center; justify-content: center;
  font-size: 0.8rem; font-weight: 700; flex-shrink: 0;
}
.chip-info { display: flex; flex-direction: column; gap: 0.05rem; flex: 1; }
.chip-username { font-size: 0.875rem; font-weight: 600; color: var(--c-primary-hover); }
.chip-name { font-size: 0.75rem; color: var(--c-text-muted); }
.chip-clear {
  background: none; border: none; color: var(--c-text-subtle); cursor: pointer;
  padding: 0.2rem; border-radius: 4px; display: flex; align-items: center; transition: color 0.15s, background 0.15s;
}
.chip-clear:hover { background: var(--c-primary-border); color: var(--c-primary-hover); }

/* ── Wallet options ────────────────────────────────────── */
.wallet-options { display: flex; flex-direction: column; gap: 0.5rem; }
.wallet-option {
  display: flex; align-items: center; gap: 0.875rem;
  padding: 0.875rem 1rem;
  border: 1.5px solid var(--c-border); border-radius: var(--radius-lg);
  background: var(--c-surface-soft); cursor: pointer; text-align: left;
  transition: border-color 0.15s, background 0.15s, box-shadow 0.15s; width: 100%;
}
.wallet-option:hover:not(:disabled) { border-color: var(--c-primary-border); background: var(--c-primary-soft); }
.wallet-option.selected {
  border-color: var(--c-primary); background: var(--c-primary-soft);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}
.wallet-option.disabled, .wallet-option:disabled { opacity: 0.4; cursor: not-allowed; }

.wallet-option-currency {
  font-size: 0.7rem; font-weight: 700; letter-spacing: 0.06em;
  padding: 0.2rem 0.55rem; border-radius: 20px; flex-shrink: 0;
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

/* ── Wallet pair (internal transfer) ───────────────────── */
.wallet-pair {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  gap: 1rem;
  align-items: start;
}
.wallet-side { display: flex; flex-direction: column; gap: 0.5rem; }
.side-label { font-size: 0.75rem; font-weight: 700; color: var(--c-text-muted); text-transform: uppercase; letter-spacing: 0.05em; margin: 0; }

.swap-btn {
  display: flex; align-items: center; justify-content: center;
  width: 40px; height: 40px; border-radius: 50%;
  background: var(--c-surface-soft); border: 1.5px solid var(--c-border);
  color: var(--c-text-secondary); cursor: pointer;
  transition: all 0.2s; margin-top: 1.6rem; flex-shrink: 0;
}
.swap-btn:hover:not(:disabled) {
  background: var(--c-primary-soft); border-color: var(--c-primary);
  color: var(--c-primary); transform: rotate(180deg);
}
.swap-btn:disabled { opacity: 0.4; cursor: not-allowed; }

/* ── Dual amount input ─────────────────────────────────── */
.dual-amount {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  gap: 0.75rem;
  align-items: center;
}
.dual-field { display: flex; flex-direction: column; gap: 0.4rem; }
.dual-label { font-size: 0.75rem; font-weight: 600; color: var(--c-text-muted); }

.conversion-arrow {
  display: flex; flex-direction: column; align-items: center; gap: 0.3rem;
  color: var(--c-text-subtle); padding-top: 1.2rem;
}
.rate-hint {
  font-size: 0.65rem; font-weight: 600; color: var(--c-text-subtle);
  white-space: nowrap; text-align: center;
}

.balance-hint {
  display: flex; align-items: center; gap: 0.4rem;
  font-size: 0.8rem; color: #dc2626; margin: 0;
}

/* ── Amount ────────────────────────────────────────────── */
.amount-wrap { gap: 0; }
.currency-prefix {
  padding: 0 0.75rem; font-size: 1rem; font-weight: 600;
  color: var(--c-text-muted); border-right: 1.5px solid var(--c-border);
  line-height: 1; flex-shrink: 0;
}
.amount-input { text-align: right; font-size: 1.1rem; font-weight: 600; padding-right: 0 !important; }
.currency-suffix {
  padding: 0 0.75rem; font-size: 0.75rem; font-weight: 700;
  color: var(--c-text-subtle); border-left: 1.5px solid var(--c-border);
  line-height: 1; flex-shrink: 0;
}
.amount-input::-webkit-inner-spin-button,
.amount-input::-webkit-outer-spin-button { -webkit-appearance: none; margin: 0; }
.amount-input[type=number] { -moz-appearance: textfield; }

.quick-amounts { display: flex; gap: 0.4rem; margin-top: 0.625rem; }
.quick-btn {
  flex: 1; padding: 0.45rem 0; border: 1.5px solid var(--c-border);
  border-radius: var(--radius-sm); background: var(--c-surface-soft);
  font-size: 0.8rem; font-weight: 600; color: var(--c-text-secondary);
  cursor: pointer; transition: all 0.15s;
}
.quick-btn:hover { border-color: var(--c-primary-border); color: var(--c-primary-hover); background: var(--c-primary-soft); }
.quick-btn.selected { border-color: var(--c-primary); background: var(--c-primary-light); color: var(--c-primary-hover); }

/* ── Summary ───────────────────────────────────────────── */
.summary-box {
  background: var(--c-surface-soft); border: 1px solid var(--c-border-soft);
  border-radius: var(--radius-lg); padding: 1rem 1.25rem;
  display: flex; flex-direction: column; gap: 0.6rem;
}
.summary-row {
  display: flex; justify-content: space-between; align-items: center; font-size: 0.875rem;
}
.summary-label { color: var(--c-text-muted); }
.summary-value { font-weight: 600; color: var(--c-text); }
.summary-value.accent { color: var(--c-primary); }
.summary-note {
  display: flex; align-items: flex-start; gap: 0.4rem;
  font-size: 0.775rem; color: var(--c-text-subtle);
  padding-top: 0.4rem; border-top: 1px solid var(--c-border-soft);
  margin-top: 0.1rem; line-height: 1.4;
}
.summary-note :deep(svg) { flex-shrink: 0; margin-top: 1px; }

/* ── Feedback ──────────────────────────────────────────── */
.success-banner {
  display: flex; align-items: center; gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: var(--c-success-soft); border: 1px solid var(--c-success-border);
  border-radius: var(--radius-md); color: var(--c-success);
  font-size: 0.875rem; font-weight: 600;
}

.send-btn { width: 100%; justify-content: center; font-size: 1rem; padding: 0.85rem; }

@media (max-width: 520px) {
  .wallet-pair { grid-template-columns: 1fr; }
  .swap-btn { margin: 0 auto; transform: rotate(90deg); }
  .swap-btn:hover:not(:disabled) { transform: rotate(270deg); }
  .dual-amount { grid-template-columns: 1fr; }
  .conversion-arrow { flex-direction: row; padding-top: 0; justify-content: center; }
}
</style>
