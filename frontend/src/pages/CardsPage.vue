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
import { onMounted } from 'vue'
import { useCards } from '@/composables/useCards'
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

const {
  cards, loading, showAdd, addLoading, addError, hoveredId, focusedField, currentYear, form,
  previewNumber, previewExpiry, previewHolder, previewBrand, previewBrandClass,
  brandClass, formatCardNumber,
  openAdd, fetchCards, confirmDelete, handleAdd,
} = useCards()

function networkIcon(_brand: string | null | undefined) {
  return CircleIcon
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
h2 { margin: 0 0 0.25rem; font-size: 1.75rem; font-weight: 700; color: var(--c-text); }
.subtitle { margin: 0; color: var(--c-text-muted); font-size: 0.9rem; }

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

/* ── Card modal: wider than default 420px ──────────────────── */
.modal { max-width: 460px; }

/* Card preview in modal */
.preview-wrap { padding: 1.25rem 1.5rem 0; }
.preview-card {
  min-height: 160px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.18);
  transition: none;
}
.preview-card:hover { transform: none; box-shadow: 0 8px 24px rgba(0,0,0,0.18); }

.row { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 0.75rem; }

/* Fade for remove button */
.fade-enter-active, .fade-leave-active { transition: opacity 0.15s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
