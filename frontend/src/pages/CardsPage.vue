<template>
  <div>
    <div class="page-header">
      <h2>Cards</h2>
      <button class="btn-primary" @click="showAdd = true">+ Add Card</button>
    </div>

    <div v-if="loading" class="loading">Loading…</div>

    <div v-else class="cards-grid">
      <div v-for="card in cards" :key="card.id" class="card-visual">
        <div class="card-brand">{{ card.brand }}</div>
        <div class="card-number">•••• •••• •••• {{ card.last4 }}</div>
        <div class="card-footer">
          <span>{{ card.expiration_month }}/{{ card.expiration_year }}</span>
          <button class="btn-delete" @click="confirmDelete(card.id)">Remove</button>
        </div>
      </div>

      <p v-if="!cards.length" class="empty">No cards linked yet.</p>
    </div>

    <!-- Add Card Modal -->
    <div v-if="showAdd" class="modal-overlay" @click.self="showAdd = false">
      <div class="modal">
        <h3>Add Card</h3>
        <form @submit.prevent="handleAdd">
          <div class="field">
            <label>Card Number</label>
            <input v-model="form.card_number" maxlength="19" placeholder="1234 5678 9012 3456" required />
          </div>
          <div class="row">
            <div class="field">
              <label>Exp. Month</label>
              <input v-model.number="form.expiration_month" type="number" min="1" max="12" required />
            </div>
            <div class="field">
              <label>Exp. Year</label>
              <input v-model.number="form.expiration_year" type="number" min="2024" required />
            </div>
          </div>
          <div class="field">
            <label>CVV</label>
            <input v-model="form.cvv" maxlength="4" required />
          </div>
          <div class="field">
            <label>Cardholder Name</label>
            <input v-model="form.card_holder_name" required />
          </div>
          <p v-if="addError" class="error">{{ addError }}</p>
          <div class="modal-actions">
            <button type="button" @click="showAdd = false">Cancel</button>
            <button type="submit" class="btn-primary" :disabled="addLoading">Add Card</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { cardsApi } from '@/api/cards'
import type { PrivateCardDto } from '@/types'

const cards = ref<PrivateCardDto[]>([])
const loading = ref(false)
const showAdd = ref(false)
const addLoading = ref(false)
const addError = ref('')

const form = ref({
  card_number: '',
  expiration_month: new Date().getMonth() + 1,
  expiration_year: new Date().getFullYear(),
  cvv: '',
  card_holder_name: '',
})

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
  if (!confirm('Remove this card?')) return
  await cardsApi.delete(id)
  cards.value = cards.value.filter((c) => c.id !== id)
}

async function handleAdd() {
  addError.value = ''
  addLoading.value = true
  try {
    const { data } = await cardsApi.create(form.value)
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
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
h2 { margin: 0; color: #1a1a2e; }

.cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1rem;
}

.card-visual {
  background: linear-gradient(135deg, #1a1a2e, #7c83fd);
  color: white;
  border-radius: 16px;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  min-height: 160px;
  box-shadow: 0 4px 16px rgba(124, 131, 253, 0.3);
}

.card-brand { font-size: 0.875rem; font-weight: 600; opacity: 0.8; text-transform: uppercase; }
.card-number { font-size: 1.1rem; letter-spacing: 0.15em; font-family: monospace; flex: 1; }
.card-footer { display: flex; justify-content: space-between; align-items: center; }

.btn-delete {
  background: rgba(255,255,255,0.15);
  border: 1px solid rgba(255,255,255,0.3);
  color: white;
  padding: 0.3rem 0.75rem;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.8rem;
}

.btn-primary {
  background: #7c83fd;
  color: white;
  border: none;
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
  max-width: 380px;
}

.modal h3 { margin: 0 0 1.25rem; }

.row { display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem; }
.field { display: flex; flex-direction: column; gap: 0.25rem; margin-bottom: 1rem; }
label { font-size: 0.875rem; color: #555; }

input {
  padding: 0.6rem 0.75rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 1rem;
  outline: none;
}

input:focus { border-color: #7c83fd; }
.modal-actions { display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 0.5rem; }
.error { color: #e53e3e; font-size: 0.875rem; }
</style>
