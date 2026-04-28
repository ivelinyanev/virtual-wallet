<template>
  <div class="cards-page">
    <!-- Header -->
    <div class="page-header">
      <div>
        <h2>My Cards</h2>
        <p class="subtitle">Manage your linked payment cards</p>
      </div>
      <button class="btn-primary" @click="openAdd">
        <PlusIcon :size="16" />
        Add Card
      </button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-state">
      <LoaderCircleIcon :size="24" class="spinner" />
      <span>Loading cards…</span>
    </div>

    <!-- Empty -->
    <div v-else-if="!cards.length" class="empty-state">
      <div class="empty-icon"><CreditCardIcon :size="40" /></div>
      <p class="empty-title">No cards linked yet</p>
      <p class="empty-sub">Add a debit or credit card to top up your wallets</p>
      <button class="btn-primary" @click="openAdd">
        <PlusIcon :size="16" />
        Add Card
      </button>
    </div>

    <!-- Cards grid -->
    <div v-else class="cards-grid">
      <div
        v-for="card in cards"
        :key="card.id"
        class="card-visual"
        :class="brandClass(card.card_brand)"
        @mouseenter="hoveredId = card.id"
        @mouseleave="hoveredId = null"
      >
        <!-- Decorative circles -->
        <span class="circle circle-1" />
        <span class="circle circle-2" />

        <!-- Top row: brand + network logo -->
        <div class="card-top">
          <span class="card-brand">{{ card.card_brand }}</span>
          <component :is="networkIcon(card.card_brand)" :size="36" class="network-icon" />
        </div>

        <!-- Chip + number -->
        <div class="card-mid">
          <CpuIcon :size="32" class="chip-icon" />
          <span class="card-number">•••• •••• •••• {{ card.last4 }}</span>
        </div>

        <!-- Footer: expiry + holder + remove -->
        <div class="card-bottom">
          <div class="card-meta">
            <span class="meta-label">Expires</span>
            <span class="meta-value">{{ String(card.expiration_month).padStart(2, '0') }}/{{ card.expiration_year }}</span>
          </div>
          <Transition name="fade">
            <button
              v-if="hoveredId === card.id"
              class="remove-btn"
              title="Remove card"
              @click="confirmDelete(card.id)"
            >
              <Trash2Icon :size="14" />
              Remove
            </button>
          </Transition>
        </div>
      </div>
    </div>

    <!-- ── Add Card Modal ──────────────────────────────────── -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="showAdd" class="modal-overlay" @click.self="showAdd = false">
          <div class="modal" role="dialog" aria-modal="true" aria-labelledby="add-card-title">

            <!-- Modal header -->
            <div class="modal-header">
              <div class="modal-icon-wrap">
                <CreditCardIcon :size="20" />
              </div>
              <div>
                <h3 id="add-card-title">Add New Card</h3>
                <p class="modal-sub">Enter your card details securely</p>
              </div>
              <button class="modal-close" @click="showAdd = false">
                <XIcon :size="18" />
              </button>
            </div>

            <!-- Live card preview -->
            <div class="preview-wrap">
              <div class="preview-card" :class="previewBrandClass">
                <span class="circle circle-1" />
                <span class="circle circle-2" />
                <div class="card-top">
                  <span class="card-brand">{{ previewBrand }}</span>
                </div>
                <div class="card-mid">
                  <CpuIcon :size="28" class="chip-icon" />
                  <span class="card-number">{{ previewNumber }}</span>
                </div>
                <div class="card-bottom">
                  <div class="card-meta">
                    <span class="meta-label">Expires</span>
                    <span class="meta-value">{{ previewExpiry }}</span>
                  </div>
                  <span class="preview-holder">{{ previewHolder }}</span>
                </div>
              </div>
            </div>

            <!-- Form -->
            <form @submit.prevent="handleAdd" class="modal-body">
              <!-- Card number -->
              <div class="field">
                <label for="card-number">Card Number</label>
                <div class="input-wrap" :class="{ focused: focusedField === 'number' }">
                  <CreditCardIcon :size="16" class="input-icon" />
                  <input
                    id="card-number"
                    v-model="form.card_number"
                    placeholder="1234 5678 9012 3456"
                    maxlength="19"
                    required
                    @input="formatCardNumber"
                    @focus="focusedField = 'number'"
                    @blur="focusedField = null"
                  />
                </div>
              </div>

              <!-- Cardholder name -->
              <div class="field">
                <label for="holder">Cardholder Name</label>
                <div class="input-wrap" :class="{ focused: focusedField === 'holder' }">
                  <UserIcon :size="16" class="input-icon" />
                  <input
                    id="holder"
                    v-model="form.card_holder_name"
                    placeholder="John Doe"
                    style="text-transform: uppercase"
                    required
                    @focus="focusedField = 'holder'"
                    @blur="focusedField = null"
                  />
                </div>
              </div>

              <!-- Expiry + CVV -->
              <div class="row">
                <div class="field">
                  <label for="exp-month">Month</label>
                  <div class="input-wrap" :class="{ focused: focusedField === 'month' }">
                    <input
                      id="exp-month"
                      v-model.number="form.exp_month"
                      type="number"
                      min="1"
                      max="12"
                      placeholder="MM"
                      required
                      @focus="focusedField = 'month'"
                      @blur="focusedField = null"
                    />
                  </div>
                </div>
                <div class="field">
                  <label for="exp-year">Year</label>
                  <div class="input-wrap" :class="{ focused: focusedField === 'year' }">
                    <input
                      id="exp-year"
                      v-model.number="form.exp_year"
                      type="number"
                      :min="currentYear"
                      placeholder="YYYY"
                      required
                      @focus="focusedField = 'year'"
                      @blur="focusedField = null"
                    />
                  </div>
                </div>
                <div class="field">
                  <label for="cvv">CVV</label>
                  <div class="input-wrap" :class="{ focused: focusedField === 'cvv' }">
                    <ShieldIcon :size="16" class="input-icon" />
                    <input
                      id="cvv"
                      v-model="form.cvv"
                      type="password"
                      maxlength="4"
                      placeholder="•••"
                      required
                      @focus="focusedField = 'cvv'"
                      @blur="focusedField = null"
                    />
                  </div>
                </div>
              </div>

              <p v-if="addError" class="form-error">
                <AlertCircleIcon :size="14" />
                {{ addError }}
              </p>

              <div class="modal-actions">
                <button type="button" class="btn-ghost" @click="showAdd = false">Cancel</button>
                <button type="submit" class="btn-primary" :disabled="addLoading">
                  <LoaderCircleIcon v-if="addLoading" :size="16" class="spinner" />
                  <LockIcon v-else :size="16" />
                  {{ addLoading ? 'Adding…' : 'Add Card Securely' }}
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
import { ref, computed, onMounted } from 'vue'
import { cardsApi } from '@/api/cards'
import { useDialog } from '@/composables/useDialog'
import type { PrivateCardDto } from '@/types'
import {
  PlusIcon,
  CreditCardIcon,
  CpuIcon,
  Trash2Icon,
  XIcon,
  ShieldIcon,
  UserIcon,
  AlertCircleIcon,
  LoaderCircleIcon,
  LockIcon,
  CircleIcon,
} from 'lucide-vue-next'

const dialog = useDialog()
const cards = ref<PrivateCardDto[]>([])
const loading = ref(false)
const showAdd = ref(false)
const addLoading = ref(false)
const addError = ref('')
const hoveredId = ref<number | null>(null)
const focusedField = ref<string | null>(null)
const currentYear = new Date().getFullYear()

const form = ref({
  card_number: '',
  card_holder_name: '',
  exp_month: new Date().getMonth() + 1,
  exp_year: currentYear,
  cvv: '',
})

// ── Computed preview values ───────────────────────────────────
const previewNumber = computed(() => {
  const raw = form.value.card_number.replace(/\s/g, '')
  if (!raw) return '•••• •••• •••• ••••'
  const padded = raw.padEnd(16, '•')
  return padded.match(/.{1,4}/g)!.join(' ')
})

const previewExpiry = computed(() => {
  const m = String(form.value.exp_month).padStart(2, '0')
  const y = String(form.value.exp_year)
  return `${m}/${y}`
})

const previewHolder = computed(() =>
  form.value.card_holder_name.toUpperCase() || 'YOUR NAME',
)

const previewBrand = computed(() => detectBrand(form.value.card_number ?? ''))
const previewBrandClass = computed(() => brandClass(previewBrand.value))

// ── Helpers ───────────────────────────────────────────────────
function detectBrand(number: string): string {
  const n = (number ?? '').replace(/\s/g, '')
  if (/^4/.test(n)) return 'VISA'
  if (/^5[1-5]/.test(n) || /^2[2-7]/.test(n)) return 'MASTERCARD'
  if (/^3[47]/.test(n)) return 'AMEX'
  return 'CARD'
}

function brandClass(brand: string | null | undefined) {
  const b = (brand ?? '').toLowerCase()
  if (b === 'visa') return 'brand-visa'
  if (b === 'mastercard') return 'brand-mastercard'
  if (b === 'amex') return 'brand-amex'
  return 'brand-default'
}

function networkIcon(_brand: string | null | undefined) {
  return CircleIcon
}

function formatCardNumber(e: Event) {
  const input = e.target as HTMLInputElement
  let val = input.value.replace(/\D/g, '').substring(0, 16)
  val = val.match(/.{1,4}/g)?.join(' ') ?? val
  form.value.card_number = val
}

function openAdd() {
  form.value = {
    card_number: '',
    card_holder_name: '',
    exp_month: new Date().getMonth() + 1,
    exp_year: currentYear,
    cvv: '',
  }
  addError.value = ''
  showAdd.value = true
}

async function fetchCards() {
  loading.value = true
  try {
    const { data } = await cardsApi.getMyCards()
    cards.value = data
  } finally {
    loading.value = false
  }
}

async function confirmDelete(id: number) {
  const { isConfirmed } = await dialog.confirm({
    title: 'Remove card?',
    text: 'This card will be unlinked from your account.',
    confirmLabel: 'Remove',
  })
  if (!isConfirmed) return
  await cardsApi.delete(id)
  cards.value = cards.value.filter((c) => c.id !== id)
}

async function handleAdd() {
  addError.value = ''
  addLoading.value = true
  try {
    const parts = form.value.card_holder_name.trim().split(/\s+/)
    const payload = {
      card_number: form.value.card_number.replace(/\s/g, ''),
      first_name: parts[0] ?? '',
      last_name: parts.slice(1).join(' ') || (parts[0] ?? ''),
      exp_month: form.value.exp_month,
      exp_year: form.value.exp_year,
      cvv: form.value.cvv,
    }
    const { data } = await cardsApi.create(payload)
    cards.value.push(data)
    showAdd.value = false
  } catch (e: any) {
    addError.value = e.response?.data?.message ?? 'Failed to add card.'
  } finally {
    addLoading.value = false
  }
}

onMounted(fetchCards)
</script>

<style scoped>
/* ── Page ──────────────────────────────────────────────────── */
.cards-page { max-width: 900px; margin: 0 auto; }

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

/* ── Cards grid ────────────────────────────────────────────── */
.cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
}

/* ── Card visual (shared between grid + preview) ───────────── */
.card-visual,
.preview-card {
  position: relative;
  border-radius: 20px;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  min-height: 185px;
  color: white;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0,0,0,0.2);
  transition: transform 0.25s, box-shadow 0.25s;
}
.card-visual:hover {
  transform: translateY(-4px) rotate(-0.5deg);
  box-shadow: 0 18px 40px rgba(0,0,0,0.26);
}

/* Brand gradients */
.brand-visa    { background: linear-gradient(135deg, #1a1f6e, #2563eb); }
.brand-mastercard { background: linear-gradient(135deg, #7c1c1c, #d97706); }
.brand-amex    { background: linear-gradient(135deg, #064e3b, #059669); }
.brand-default { background: linear-gradient(135deg, #1e293b, #475569); }

/* Decorative circles */
.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255,255,255,0.07);
  pointer-events: none;
}
.circle-1 { width: 180px; height: 180px; top: -60px; right: -60px; }
.circle-2 { width: 120px; height: 120px; bottom: -40px; left: -30px; }

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
}
.card-brand {
  font-size: 0.8rem;
  font-weight: 800;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  opacity: 0.9;
}
.network-icon { opacity: 0.7; }

.card-mid {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex: 1;
  position: relative;
}
.chip-icon { opacity: 0.75; flex-shrink: 0; }
.card-number {
  font-size: 1.05rem;
  letter-spacing: 0.18em;
  font-family: 'Courier New', monospace;
  font-weight: 600;
}

.card-bottom {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  position: relative;
}
.card-meta { display: flex; flex-direction: column; gap: 0.1rem; }
.meta-label { font-size: 0.6rem; text-transform: uppercase; letter-spacing: 0.08em; opacity: 0.6; }
.meta-value { font-size: 0.9rem; font-weight: 600; letter-spacing: 0.05em; }
.preview-holder { font-size: 0.8rem; font-weight: 600; letter-spacing: 0.06em; opacity: 0.85; max-width: 140px; text-align: right; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

.remove-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  background: rgba(239,68,68,0.25);
  border: 1px solid rgba(239,68,68,0.4);
  color: #fca5a5;
  padding: 0.35rem 0.7rem;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.75rem;
  font-weight: 600;
  transition: background 0.15s;
}
.remove-btn:hover { background: rgba(239,68,68,0.45); }

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
  max-width: 460px;
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
.modal-header > div:nth-child(2) { flex: 1; }
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

/* Card preview in modal */
.preview-wrap {
  padding: 1.25rem 1.5rem 0;
}
.preview-card {
  min-height: 160px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.18);
  transition: none;
}
.preview-card:hover { transform: none; box-shadow: 0 8px 24px rgba(0,0,0,0.18); }

/* Form */
.modal-body { padding: 1.25rem 1.5rem 1.5rem; display: flex; flex-direction: column; gap: 0.9rem; }

.field { display: flex; flex-direction: column; gap: 0.4rem; }
label { font-size: 0.8rem; font-weight: 600; color: #475569; }

.input-wrap {
  display: flex;
  align-items: center;
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
  background: #f8fafc;
  transition: border-color 0.15s, box-shadow 0.15s, background 0.15s;
  overflow: hidden;
}
.input-wrap.focused {
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
  width: 100%;
}
.input-wrap:not(:has(.input-icon)) input { padding-left: 0.75rem; }
.input-wrap input::placeholder { color: #cbd5e1; }

.row { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 0.75rem; }

.form-error {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  color: #dc2626;
  font-size: 0.825rem;
  margin: 0;
}

.modal-actions { display: flex; justify-content: flex-end; gap: 0.6rem; padding-top: 0.25rem; }

/* Spinner */
@keyframes spin { to { transform: rotate(360deg); } }
.spinner { animation: spin 0.8s linear infinite; }

/* Fade for remove button */
.fade-enter-active, .fade-leave-active { transition: opacity 0.15s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

/* Modal transition */
.modal-enter-active, .modal-leave-active { transition: opacity 0.2s ease; }
.modal-enter-active .modal, .modal-leave-active .modal { transition: transform 0.2s ease, opacity 0.2s ease; }
.modal-enter-from, .modal-leave-to { opacity: 0; }
.modal-enter-from .modal, .modal-leave-to .modal { transform: scale(0.96) translateY(8px); opacity: 0; }
</style>
