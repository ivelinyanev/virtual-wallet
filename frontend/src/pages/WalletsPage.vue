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
                <button type="submit" class="btn-success" :disabled="topUpLoading || topUpAmount <= 0">
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useWalletStore } from '@/stores/wallet'
import { useDialog } from '@/composables/useDialog'
import type { PrivateWalletDto, Currency } from '@/types'
import {
  PlusIcon,
  PlusCircleIcon,
  Trash2Icon,
  LandmarkIcon,
  CpuIcon,
  XIcon,
  PencilIcon,
  ArrowDownCircleIcon,
  AlertCircleIcon,
  LoaderCircleIcon,
} from 'lucide-vue-next'

const walletStore = useWalletStore()
const dialog = useDialog()

const showCreate = ref(false)
const createForm = ref<{ name: string; currency: Currency }>({ name: '', currency: 'EUR' })
const createError = ref('')
const createLoading = ref(false)

const topUpWallet = ref<PrivateWalletDto | null>(null)
const topUpAmount = ref(0)
const topUpError = ref('')
const topUpLoading = ref(false)

const quickAmounts = [10, 50, 100, 500]

const currencies = [
  { code: 'EUR' as Currency, symbol: '€', name: 'Euro' },
  { code: 'USD' as Currency, symbol: '$', name: 'US Dollar' },
  { code: 'GBP' as Currency, symbol: '£', name: 'Pound Sterling' },
]

function currencySymbol(code: Currency) {
  return currencies.find((c) => c.code === code)?.symbol ?? code
}

function formatCurrency(amount: number, currency: Currency = 'EUR') {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency }).format(amount)
}

function cardAccent(currency: Currency) {
  return { eur: currency === 'EUR', usd: currency === 'USD', gbp: currency === 'GBP' }
}

function openCreate() {
  createForm.value = { name: '', currency: 'EUR' }
  createError.value = ''
  showCreate.value = true
}

function openTopUp(wallet: PrivateWalletDto) {
  topUpWallet.value = wallet
  topUpAmount.value = 0
  topUpError.value = ''
}

async function confirmDelete(id: number) {
  const { isConfirmed } = await dialog.confirm({
    title: 'Delete wallet?',
    text: 'This action cannot be undone.',
    confirmLabel: 'Delete',
  })
  if (!isConfirmed) return
  try {
    await walletStore.deleteWallet(id)
  } catch (e: any) {
    await dialog.error('Could not delete wallet', e.response?.data?.message)
  }
}

async function handleCreate() {
  createError.value = ''
  createLoading.value = true
  try {
    await walletStore.createWallet(createForm.value)
    showCreate.value = false
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
/* ── Page ──────────────────────────────────────────────────── */
.wallets-page { max-width: 900px; margin: 0 auto; }

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
}
h2 { margin: 0 0 0.2rem; font-size: 1.75rem; font-weight: 700; color: #0f172a; }
.subtitle { margin: 0; color: #64748b; font-size: 0.9rem; }

/* ── Buttons ───────────────────────────────────────────────── */
.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  background: #6366f1;
  color: white;
  border: none;
  padding: 0.6rem 1.1rem;
  border-radius: 10px;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 600;
  transition: background 0.15s, transform 0.1s;
}
.btn-primary:hover { background: #4f52e0; }
.btn-primary:active { transform: scale(0.98); }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }

.btn-ghost {
  background: transparent;
  border: 1px solid #e2e8f0;
  color: #64748b;
  padding: 0.6rem 1.1rem;
  border-radius: 10px;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
  transition: background 0.15s;
}
.btn-ghost:hover { background: #f8fafc; }

.btn-success {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  background: #16a34a;
  color: white;
  border: none;
  padding: 0.6rem 1.1rem;
  border-radius: 10px;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 600;
  transition: background 0.15s;
}
.btn-success:hover { background: #15803d; }
.btn-success:disabled { opacity: 0.6; cursor: not-allowed; }

/* ── States ────────────────────────────────────────────────── */
.loading-state {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  color: #94a3b8;
  padding: 3rem 0;
}
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  color: #94a3b8;
}
.empty-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 72px;
  height: 72px;
  border-radius: 20px;
  background: #f1f5f9;
  color: #cbd5e1;
  margin-bottom: 1rem;
}
.empty-title { font-size: 1.1rem; font-weight: 600; color: #475569; margin: 0 0 0.4rem; }
.empty-sub { font-size: 0.875rem; margin: 0 0 1.5rem; }

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
  min-height: 190px;
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
  justify-content: space-between;
  align-items: center;
  gap: 0.75rem;
}
.wallet-name { font-size: 0.85rem; font-weight: 500; color: rgba(255,255,255,0.75); }

.card-actions { display: flex; gap: 0.4rem; }
.card-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.78rem;
  font-weight: 600;
  padding: 0.4rem 0.75rem;
  transition: background 0.15s;
}
.topup-btn { background: rgba(255,255,255,0.2); color: white; }
.topup-btn:hover { background: rgba(255,255,255,0.3); }
.delete-btn { background: rgba(239,68,68,0.25); color: #fca5a5; padding: 0.4rem 0.6rem; }
.delete-btn:hover { background: rgba(239,68,68,0.4); }

/* ── Modal ─────────────────────────────────────────────────── */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15,23,42,0.55);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 200;
  padding: 1rem;
}

.modal {
  background: white;
  border-radius: 20px;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 24px 64px rgba(0,0,0,0.18);
  overflow: hidden;
}

.modal-header {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 1.5rem 1.5rem 0;
}
.modal-icon-wrap {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: #ede9fe;
  color: #6366f1;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.modal-icon-wrap.green { background: #dcfce7; color: #16a34a; }
.modal-header > div:nth-child(2) { flex: 1; min-width: 0; }
.modal-header h3 { margin: 0 0 0.15rem; font-size: 1.05rem; font-weight: 700; color: #0f172a; }
.modal-sub { margin: 0; font-size: 0.8rem; color: #94a3b8; }
.modal-close {
  background: transparent;
  border: none;
  color: #94a3b8;
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 6px;
  display: flex;
  align-items: center;
  transition: background 0.15s, color 0.15s;
}
.modal-close:hover { background: #f1f5f9; color: #475569; }

.modal-body { padding: 1.25rem 1.5rem 1.5rem; display: flex; flex-direction: column; gap: 1.1rem; }

/* Fields */
.field { display: flex; flex-direction: column; gap: 0.4rem; }
label { font-size: 0.8rem; font-weight: 600; color: #475569; }

.input-wrap {
  display: flex;
  align-items: center;
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
  overflow: hidden;
  transition: border-color 0.15s, box-shadow 0.15s;
  background: #f8fafc;
}
.input-wrap:focus-within {
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99,102,241,0.12);
  background: white;
}
.input-icon { color: #94a3b8; padding: 0 0.75rem; flex-shrink: 0; }
.input-wrap input {
  flex: 1;
  border: none;
  outline: none;
  padding: 0.65rem 0.75rem 0.65rem 0;
  font-size: 0.95rem;
  background: transparent;
  color: #0f172a;
}
.input-wrap input::placeholder { color: #cbd5e1; }

/* Currency selector */
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
.after-value { font-weight: 700; }

.form-error {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  color: #dc2626;
  font-size: 0.825rem;
  margin: 0;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.6rem;
  padding-top: 0.25rem;
}

/* Spinner */
@keyframes spin { to { transform: rotate(360deg); } }
.spinner { animation: spin 0.8s linear infinite; }

/* Modal transition */
.modal-enter-active, .modal-leave-active { transition: opacity 0.2s ease; }
.modal-enter-active .modal, .modal-leave-active .modal { transition: transform 0.2s ease, opacity 0.2s ease; }
.modal-enter-from, .modal-leave-to { opacity: 0; }
.modal-enter-from .modal, .modal-leave-to .modal { transform: scale(0.96) translateY(8px); opacity: 0; }
</style>
