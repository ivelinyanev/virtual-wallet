<template>
  <div class="wallets-page">
    <!-- Header -->
    <div class="page-header">
      <div>
        <h2>My Wallets</h2>
        <p class="subtitle">Manage your accounts and balances</p>
      </div>
      <button class="btn-primary" @click="openCreate">
        <PlusIcon :size="16" />
        New Wallet
      </button>
    </div>

    <!-- Loading -->
    <div v-if="walletStore.loading" class="loading-state">
      <LoaderCircleIcon :size="24" class="spinner" />
      <span>Loading wallets…</span>
    </div>

    <!-- Empty -->
    <div v-else-if="!walletStore.wallets.length" class="empty-state">
      <div class="empty-icon"><LandmarkIcon :size="40" /></div>
      <p class="empty-title">No wallets yet</p>
      <p class="empty-sub">Create your first wallet to start managing your money</p>
      <button class="btn-primary" @click="openCreate">
        <PlusIcon :size="16" />
        Create Wallet
      </button>
    </div>

    <!-- Wallet cards -->
    <div v-else class="wallet-grid">
      <div
        v-for="wallet in walletStore.wallets"
        :key="wallet.id"
        class="wallet-card"
        :class="cardAccent(wallet.currency)"
      >
        <!-- Top row -->
        <div class="card-top">
          <div class="card-chip">
            <CpuIcon :size="28" />
          </div>
          <span class="currency-badge">{{ wallet.currency }}</span>
        </div>

        <!-- Balance -->
        <div class="card-balance">
          <span class="balance-label">Available balance</span>
          <span class="balance-amount">{{ formatCurrency(wallet.balance, wallet.currency) }}</span>
        </div>

        <!-- Footer -->
        <div class="card-footer">
          <span class="wallet-name">{{ wallet.wallet_name }}</span>
          <div class="card-actions">
            <button class="card-btn topup-btn" title="Top Up" @click="openTopUp(wallet)">
              <PlusCircleIcon :size="16" />
              Top Up
            </button>
            <button class="card-btn withdraw-btn" title="Withdraw" @click="openWithdraw(wallet)">
              <ArrowUpCircleIcon :size="16" />
              Withdraw
            </button>
            <button class="card-btn delete-btn" title="Delete wallet" @click="confirmDelete(wallet.id)">
              <Trash2Icon :size="16" />
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ── Create Wallet Modal ─────────────────────────────── -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="showCreate" class="modal-overlay" @click.self="showCreate = false">
          <div class="modal" role="dialog" aria-modal="true" aria-labelledby="create-title">
            <div class="modal-header">
              <div class="modal-icon-wrap">
                <LandmarkIcon :size="20" />
              </div>
              <div>
                <h3 id="create-title">New Wallet</h3>
                <p class="modal-sub">Choose a name and currency for your wallet</p>
              </div>
              <button class="modal-close" @click="showCreate = false">
                <XIcon :size="18" />
              </button>
            </div>

            <form @submit.prevent="handleCreate" class="modal-body">
              <!-- Wallet name -->
              <div class="field">
                <label for="wallet-name">Wallet name</label>
                <div class="input-wrap">
                  <PencilIcon :size="16" class="input-icon" />
                  <input
                    id="wallet-name"
                    v-model="createForm.name"
                    placeholder="e.g. Savings, Travel…"
                    maxlength="20"
                    required
                  />
                </div>
              </div>

              <!-- Currency selector -->
              <div class="field">
                <label>Currency</label>
                <div class="currency-options">
                  <button
                    v-for="c in currencies"
                    :key="c.code"
                    type="button"
                    class="currency-option"
                    :class="{ selected: createForm.currency === c.code }"
                    @click="createForm.currency = c.code"
                  >
                    <span class="currency-symbol">{{ c.symbol }}</span>
                    <span class="currency-code">{{ c.code }}</span>
                    <span class="currency-name">{{ c.name }}</span>
                  </button>
                </div>
              </div>

              <p v-if="createError" class="form-error">
                <AlertCircleIcon :size="14" />
                {{ createError }}
              </p>

              <div class="modal-actions">
                <button type="button" class="btn-ghost" @click="showCreate = false">Cancel</button>
                <button type="submit" class="btn-primary" :disabled="createLoading">
                  <LoaderCircleIcon v-if="createLoading" :size="16" class="spinner" />
                  <span>{{ createLoading ? 'Creating…' : 'Create Wallet' }}</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- ── Top-Up Modal ───────────────────────────────────── -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="topUpWallet" class="modal-overlay" @click.self="topUpWallet = null">
          <div class="modal" role="dialog" aria-modal="true" aria-labelledby="topup-title">
            <div class="modal-header">
              <div class="modal-icon-wrap green">
                <ArrowDownCircleIcon :size="20" />
              </div>
              <div>
                <h3 id="topup-title">Top Up</h3>
                <p class="modal-sub">{{ topUpWallet.wallet_name }} · {{ topUpWallet.currency }}</p>
              </div>
              <button class="modal-close" @click="topUpWallet = null">
                <XIcon :size="18" />
              </button>
            </div>

            <form @submit.prevent="handleTopUp" class="modal-body">
              <!-- Current balance -->
              <div class="balance-preview">
                <span class="balance-preview-label">Current balance</span>
                <span class="balance-preview-value">{{ formatCurrency(topUpWallet.balance, topUpWallet.currency) }}</span>
              </div>

              <!-- Card selector -->
              <div class="field">
                <label>Pay with card</label>
                <div v-if="!cards.length" class="no-cards">
                  <CreditCardIcon :size="14" />
                  No cards linked. <RouterLink to="/cards">Add one first</RouterLink>.
                </div>
                <div v-else class="card-options">
                  <button
                    v-for="card in cards"
                    :key="card.id"
                    type="button"
                    class="card-option"
                    :class="{ selected: topUpCardId === card.id }"
                    @click="topUpCardId = card.id"
                  >
                    <CreditCardIcon :size="16" />
                    <span class="card-option-brand">{{ card.card_brand }}</span>
                    <span class="card-option-num">•••• {{ card.last4 }}</span>
                    <span class="card-option-exp">{{ String(card.expiration_month).padStart(2,'0') }}/{{ card.expiration_year }}</span>
                  </button>
                </div>
              </div>

              <!-- Amount input -->
              <div class="field">
                <label for="topup-amount">Amount to add</label>
                <div class="input-wrap amount-wrap">
                  <span class="currency-prefix">{{ currencySymbol(topUpWallet.currency) }}</span>
                  <input
                    id="topup-amount"
                    v-model.number="topUpAmount"
                    type="number"
                    min="0.01"
                    step="0.01"
                    placeholder="0.00"
                    required
                    class="amount-input"
                  />
                  <span class="currency-suffix">{{ topUpWallet.currency }}</span>
                </div>
              </div>

              <!-- Quick-add amounts -->
              <div class="quick-amounts">
                <button
                  v-for="q in quickAmounts"
                  :key="q"
                  type="button"
                  class="quick-btn"
                  :class="{ selected: topUpAmount === q }"
                  @click="topUpAmount = q"
                >
                  +{{ currencySymbol(topUpWallet.currency) }}{{ q }}
                </button>
              </div>

              <!-- After top-up preview -->
              <div v-if="topUpAmount > 0" class="after-preview">
                <span>New balance after top-up</span>
                <span class="after-value">{{ formatCurrency(topUpWallet.balance + topUpAmount, topUpWallet.currency) }}</span>
              </div>

              <p v-if="topUpError" class="form-error">
                <AlertCircleIcon :size="14" />
                {{ topUpError }}
              </p>

              <div class="modal-actions">
                <button type="button" class="btn-ghost" @click="topUpWallet = null">Cancel</button>
                <button type="submit" class="btn-success" :disabled="topUpLoading || topUpAmount <= 0 || !topUpCardId">
                  <LoaderCircleIcon v-if="topUpLoading" :size="16" class="spinner" />
                  <ArrowDownCircleIcon v-else :size="16" />
                  <span>{{ topUpLoading ? 'Processing…' : 'Confirm Top Up' }}</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      </Transition>
    </Teleport>
    <!-- ── Withdraw Modal ───────────────────────────────────── -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="withdrawWallet" class="modal-overlay" @click.self="withdrawWallet = null">
          <div class="modal" role="dialog" aria-modal="true" aria-labelledby="withdraw-title">
            <div class="modal-header">
              <div class="modal-icon-wrap orange">
                <ArrowUpCircleIcon :size="20" />
              </div>
              <div>
                <h3 id="withdraw-title">Withdraw</h3>
                <p class="modal-sub">{{ withdrawWallet.wallet_name }} · {{ withdrawWallet.currency }}</p>
              </div>
              <button class="modal-close" @click="withdrawWallet = null">
                <XIcon :size="18" />
              </button>
            </div>

            <form @submit.prevent="handleWithdraw" class="modal-body">
              <!-- Current balance -->
              <div class="balance-preview">
                <span class="balance-preview-label">Available balance</span>
                <span class="balance-preview-value">{{ formatCurrency(withdrawWallet.balance, withdrawWallet.currency) }}</span>
              </div>

              <!-- Card selector -->
              <div class="field">
                <label>Withdraw to card</label>
                <div v-if="!cards.length" class="no-cards">
                  <CreditCardIcon :size="14" />
                  No cards linked. <RouterLink to="/cards">Add one first</RouterLink>.
                </div>
                <div v-else class="card-options">
                  <button
                    v-for="card in cards"
                    :key="card.id"
                    type="button"
                    class="card-option"
                    :class="{ selected: withdrawCardId === card.id }"
                    @click="withdrawCardId = card.id"
                  >
                    <CreditCardIcon :size="16" />
                    <span class="card-option-brand">{{ card.card_brand }}</span>
                    <span class="card-option-num">•••• {{ card.last4 }}</span>
                    <span class="card-option-exp">{{ String(card.expiration_month).padStart(2,'0') }}/{{ card.expiration_year }}</span>
                  </button>
                </div>
              </div>

              <!-- Amount input -->
              <div class="field">
                <label for="withdraw-amount">Amount to withdraw</label>
                <div class="input-wrap amount-wrap">
                  <span class="currency-prefix">{{ currencySymbol(withdrawWallet.currency) }}</span>
                  <input
                    id="withdraw-amount"
                    v-model.number="withdrawAmount"
                    type="number"
                    min="0.01"
                    :max="withdrawWallet.balance"
                    step="0.01"
                    placeholder="0.00"
                    required
                    class="amount-input"
                  />
                  <span class="currency-suffix">{{ withdrawWallet.currency }}</span>
                </div>
              </div>

              <!-- Quick amounts -->
              <div class="quick-amounts">
                <button
                  v-for="q in quickAmounts"
                  :key="q"
                  type="button"
                  class="quick-btn"
                  :class="{ selected: withdrawAmount === q }"
                  :disabled="q > withdrawWallet.balance"
                  @click="withdrawAmount = q"
                >
                  {{ currencySymbol(withdrawWallet.currency) }}{{ q }}
                </button>
              </div>

              <!-- After-withdraw preview -->
              <div v-if="withdrawAmount > 0" class="after-preview withdraw-preview">
                <span>Remaining balance</span>
                <span class="after-value">{{ formatCurrency(withdrawWallet.balance - withdrawAmount, withdrawWallet.currency) }}</span>
              </div>

              <p v-if="withdrawError" class="form-error">
                <AlertCircleIcon :size="14" />
                {{ withdrawError }}
              </p>

              <div class="modal-actions">
                <button type="button" class="btn-ghost" @click="withdrawWallet = null">Cancel</button>
                <button
                  type="submit"
                  class="btn-warning"
                  :disabled="withdrawLoading || withdrawAmount <= 0 || !withdrawCardId || withdrawAmount > withdrawWallet.balance"
                >
                  <LoaderCircleIcon v-if="withdrawLoading" :size="16" class="spinner" />
                  <ArrowUpCircleIcon v-else :size="16" />
                  <span>{{ withdrawLoading ? 'Processing…' : 'Confirm Withdrawal' }}</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useWallets } from '@/composables/useWallets'
import { useFormatters } from '@/composables/useFormatters'
import {
  PlusIcon,
  PlusCircleIcon,
  Trash2Icon,
  LandmarkIcon,
  CpuIcon,
  XIcon,
  PencilIcon,
  ArrowDownCircleIcon,
  ArrowUpCircleIcon,
  AlertCircleIcon,
  LoaderCircleIcon,
  CreditCardIcon,
} from 'lucide-vue-next'

const {
  walletStore,
  showCreate, createForm, createError, createLoading,
  topUpWallet, topUpAmount, topUpCardId, topUpError, topUpLoading, cards,
  withdrawWallet, withdrawAmount, withdrawCardId, withdrawError, withdrawLoading,
  quickAmounts, currencies,
  currencySymbol, cardAccent,
  openCreate, openTopUp, openWithdraw, confirmDelete, handleCreate, handleTopUp, handleWithdraw,
} = useWallets()

const { formatCurrency } = useFormatters()

onMounted(() => walletStore.fetchWallets())
</script>

<style scoped>
/* ── Page ──────────────────────────────────────────────────── */
.wallets-page { max-width: 900px; margin: 0 auto; }

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
}
h2 { margin: 0 0 0.25rem; font-size: 1.75rem; font-weight: 700; color: var(--c-text); }
.subtitle { margin: 0; color: var(--c-text-muted); font-size: 0.9rem; }

/* ── Wallet grid ───────────────────────────────────────────── */
.wallet-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.25rem;
}

.wallet-card {
  border-radius: 20px;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  color: white;
  position: relative;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(0,0,0,0.18);
  transition: transform 0.2s, box-shadow 0.2s;
}
.wallet-card::before {
  content: '';
  position: absolute;
  top: -40px;
  right: -40px;
  width: 160px;
  height: 160px;
  border-radius: 50%;
  background: rgba(255,255,255,0.08);
  pointer-events: none;
}
.wallet-card:hover { transform: translateY(-3px); box-shadow: 0 14px 32px rgba(0,0,0,0.22); }

/* Per-currency gradients */
.wallet-card.eur { background: linear-gradient(135deg, #1e3a5f, #2563eb); }
.wallet-card.usd { background: linear-gradient(135deg, #14532d, #16a34a); }
.wallet-card.gbp { background: linear-gradient(135deg, #4a1942, #9333ea); }

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.card-chip { color: rgba(255,255,255,0.7); }
.currency-badge {
  font-size: 0.7rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  padding: 0.25rem 0.6rem;
  background: rgba(255,255,255,0.15);
  border-radius: 20px;
  backdrop-filter: blur(4px);
}

.card-balance { display: flex; flex-direction: column; gap: 0.2rem; flex: 1; }
.balance-label { font-size: 0.72rem; color: rgba(255,255,255,0.6); text-transform: uppercase; letter-spacing: 0.06em; }
.balance-amount { font-size: 1.75rem; font-weight: 700; letter-spacing: -0.02em; }

.card-footer {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
  margin-top: auto;
}
.wallet-name { font-size: 0.85rem; font-weight: 500; color: rgba(255,255,255,0.75); }

.card-actions { display: flex; gap: 0.4rem; width: 100%; }
.card-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.35rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.78rem;
  font-weight: 600;
  padding: 0.45rem 0;
  transition: background 0.15s;
  flex: 1;
}
.topup-btn { background: rgba(255,255,255,0.2); color: white; }
.topup-btn:hover { background: rgba(255,255,255,0.3); }
.withdraw-btn { background: rgba(251,191,36,0.25); color: #fef08a; }
.withdraw-btn:hover { background: rgba(251,191,36,0.4); }
.delete-btn { background: rgba(239,68,68,0.25); color: #fca5a5; flex: 0 0 auto; padding: 0.45rem 0.75rem; }
.delete-btn:hover { background: rgba(239,68,68,0.4); }

/* ── Currency selector ─────────────────────────────────────── */
.currency-options {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0.5rem;
}
.currency-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.15rem;
  padding: 0.7rem 0.5rem;
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
  background: #f8fafc;
  cursor: pointer;
  transition: all 0.15s;
}
.currency-option:hover { border-color: #a5b4fc; background: #f5f3ff; }
.currency-option.selected { border-color: #6366f1; background: #ede9fe; }
.currency-symbol { font-size: 1.2rem; font-weight: 700; color: #0f172a; }
.currency-code { font-size: 0.75rem; font-weight: 700; color: #475569; }
.currency-name { font-size: 0.65rem; color: #94a3b8; }
.currency-option.selected .currency-symbol,
.currency-option.selected .currency-code { color: #4f46e5; }

/* Card selector */
.no-cards {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.825rem;
  color: #94a3b8;
  padding: 0.5rem 0;
}
.no-cards a { color: #6366f1; text-decoration: none; }

.card-options { display: flex; flex-direction: column; gap: 0.4rem; }

.card-option {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  padding: 0.65rem 0.875rem;
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
  background: #f8fafc;
  cursor: pointer;
  text-align: left;
  transition: border-color 0.15s, background 0.15s;
  width: 100%;
}
.card-option:hover { border-color: #a5b4fc; background: #f5f3ff; }
.card-option.selected { border-color: #6366f1; background: #ede9fe; }
.card-option :deep(svg) { color: #94a3b8; flex-shrink: 0; }
.card-option.selected :deep(svg) { color: #6366f1; }
.card-option-brand { font-size: 0.78rem; font-weight: 700; color: #475569; text-transform: uppercase; }
.card-option.selected .card-option-brand { color: #4f46e5; }
.card-option-num { font-size: 0.85rem; font-weight: 600; color: #0f172a; flex: 1; }
.card-option-exp { font-size: 0.75rem; color: #94a3b8; }

/* Top-up specifics */
.balance-preview {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.875rem 1rem;
  background: #f8fafc;
  border-radius: 10px;
  border: 1px solid #f1f5f9;
}
.balance-preview-label { font-size: 0.8rem; color: #64748b; }
.balance-preview-value { font-size: 1rem; font-weight: 700; color: #0f172a; }

.amount-wrap { gap: 0; }
.currency-prefix {
  padding: 0 0.75rem;
  font-size: 1rem;
  font-weight: 600;
  color: #64748b;
  border-right: 1.5px solid #e2e8f0;
  line-height: 1;
}
.amount-input { text-align: right; font-size: 1.1rem; font-weight: 600; padding-right: 0 !important; }
.currency-suffix {
  padding: 0 0.75rem;
  font-size: 0.75rem;
  font-weight: 700;
  color: #94a3b8;
  border-left: 1.5px solid #e2e8f0;
  line-height: 1;
}

.quick-amounts {
  display: flex;
  gap: 0.4rem;
}
.quick-btn {
  flex: 1;
  padding: 0.45rem 0;
  border: 1.5px solid #e2e8f0;
  border-radius: 8px;
  background: #f8fafc;
  font-size: 0.78rem;
  font-weight: 600;
  color: #475569;
  cursor: pointer;
  transition: all 0.15s;
}
.quick-btn:hover { border-color: #a5b4fc; color: #4f46e5; background: #f5f3ff; }
.quick-btn.selected { border-color: #6366f1; background: #ede9fe; color: #4f46e5; }

.after-preview {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 10px;
  font-size: 0.85rem;
  color: #166534;
}
.withdraw-preview { background: #fffbeb; border-color: #fde68a; color: #92400e; }
.after-value { font-weight: 700; }

/* ── Modal icon variants ────────────────────────────────── */
.modal-icon-wrap.orange { background: #fef3c7; color: #d97706; }

/* ── Warning button ─────────────────────────────────────── */
.btn-warning {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.6rem 1.25rem;
  border: none;
  border-radius: 10px;
  background: #d97706;
  color: white;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
}
.btn-warning:hover:not(:disabled) { background: #b45309; }
.btn-warning:disabled { opacity: 0.5; cursor: not-allowed; }

</style>
