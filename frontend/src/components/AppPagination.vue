<template>
  <nav v-if="totalPages > 0" class="pg-nav" aria-label="Pagination">
    <!-- Prev -->
    <button class="pg-btn pg-arrow" :disabled="modelValue === 0" @click="emit('update:modelValue', modelValue - 1)" aria-label="Previous page">
      <ChevronLeftIcon :size="15" />
    </button>

    <!-- Page numbers with ellipsis -->
    <template v-for="(item, i) in pages" :key="i">
      <span v-if="item === ELLIPSIS" class="pg-ellipsis" aria-hidden="true">…</span>
      <button
        v-else
        class="pg-btn pg-num"
        :class="{ active: item === modelValue }"
        :aria-current="item === modelValue ? 'page' : undefined"
        @click="emit('update:modelValue', item as number)"
      >{{ (item as number) + 1 }}</button>
    </template>

    <!-- Next -->
    <button class="pg-btn pg-arrow" :disabled="modelValue >= totalPages - 1" @click="emit('update:modelValue', modelValue + 1)" aria-label="Next page">
      <ChevronRightIcon :size="15" />
    </button>
  </nav>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ChevronLeftIcon, ChevronRightIcon } from 'lucide-vue-next'

const ELLIPSIS = '...' as const

const props = defineProps<{
  modelValue: number   // 0-based current page
  totalPages: number
}>()

const emit = defineEmits<{
  'update:modelValue': [page: number]
}>()

const pages = computed(() => {
  const total = props.totalPages
  const current = props.modelValue

  if (total <= 7) {
    return Array.from({ length: total }, (_, i) => i)
  }

  const items: (number | typeof ELLIPSIS)[] = []

  // Always show first page
  items.push(0)

  const left = current - 1
  const right = current + 1

  if (left > 1) items.push(ELLIPSIS)

  for (let i = Math.max(1, left); i <= Math.min(total - 2, right); i++) {
    items.push(i)
  }

  if (right < total - 2) items.push(ELLIPSIS)

  // Always show last page
  items.push(total - 1)

  return items
})
</script>

<style scoped>
.pg-nav {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.25rem;
  margin-top: 1.5rem;
  flex-wrap: wrap;
}

.pg-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 36px;
  height: 36px;
  padding: 0 0.5rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: white;
  color: #475569;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s, color 0.15s, box-shadow 0.15s;
  line-height: 1;
}

.pg-btn:hover:not(:disabled) {
  background: #f5f3ff;
  border-color: #c7d2fe;
  color: #6366f1;
}

.pg-btn.active {
  background: #6366f1;
  border-color: #6366f1;
  color: white;
  font-weight: 600;
  box-shadow: 0 1px 4px rgba(99, 102, 241, 0.35);
}

.pg-btn:disabled {
  opacity: 0.35;
  cursor: not-allowed;
  pointer-events: none;
}

.pg-arrow {
  color: #64748b;
}

.pg-ellipsis {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 36px;
  height: 36px;
  color: #94a3b8;
  font-size: 0.875rem;
  letter-spacing: 0.05em;
  user-select: none;
}
</style>
