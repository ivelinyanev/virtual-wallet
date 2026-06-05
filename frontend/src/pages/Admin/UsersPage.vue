<template>
  <div>
    <h2>Admin — Users</h2>
    <div class="search-bar">
      <input v-model="query" type="text" placeholder="Search users…" @input="search" />
    </div>
    <table class="table">
      <thead>
        <tr>
          <th>ID</th><th>Username</th><th>Name</th><th>Status</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="user in users" :key="user.id">
          <td>{{ user.id }}</td>
          <td>{{ user.username }}</td>
          <td>{{ user.first_name }} {{ user.last_name }}</td>
          <td>—</td>
        </tr>
        <tr v-if="!users.length"><td colspan="4" class="empty">No users found.</td></tr>
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
import { usersApi } from '@/api/users'
import type { PublicUserResponse } from '@/types'

const users = ref<PublicUserResponse[]>([])
const query = ref('')
const page = ref(0)
const totalPages = ref(1)

let searchTimeout: ReturnType<typeof setTimeout>

function search() {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => load(), 300)
}

async function load() {
  const { data } = query.value
    ? await usersApi.search(query.value, page.value)
    : await usersApi.getAll(page.value)
  users.value = data.content
  totalPages.value = data.total_pages
}

function goTo(p: number) { page.value = p; load() }

onMounted(load)
</script>

<style scoped>
h2 { margin: 0 0 1.5rem; color: #1a1a2e; }
.search-bar { margin-bottom: 1rem; }
.search-bar input {
  padding: 0.6rem 0.75rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 0.9rem;
  width: 280px;
  outline: none;
}

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
