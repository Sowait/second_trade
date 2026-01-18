<template>
  <div class="flex items-center justify-between bg-white/55 backdrop-blur-xl border border-light-2/80 rounded-2xl px-4 py-3 shadow-card">
    <div class="text-sm text-dark-2">
      共 <span class="font-medium text-dark">{{ total }}</span> 条
    </div>

    <div class="flex items-center space-x-2">
      <button
        class="px-3 py-2 rounded-xl border border-light-2/80 bg-white/70 backdrop-blur text-dark-2 hover:bg-white transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
        :disabled="current <= 1"
        @click="go(current - 1)"
      >
        <i class="fas fa-chevron-left"></i>
      </button>

      <button
        v-for="p in pages"
        :key="p"
        class="w-10 h-10 rounded-xl border transition-colors"
        :class="p === current ? 'bg-primary text-white border-primary shadow-sm' : 'bg-white/70 backdrop-blur text-dark-2 border-light-2/80 hover:bg-white'"
        @click="go(p)"
      >
        {{ p }}
      </button>

      <button
        class="px-3 py-2 rounded-xl border border-light-2/80 bg-white/70 backdrop-blur text-dark-2 hover:bg-white transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
        :disabled="current >= pageCount"
        @click="go(current + 1)"
      >
        <i class="fas fa-chevron-right"></i>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  current?: number
  pageSize?: number
  total?: number
}>(), {
  current: 1,
  pageSize: 12,
  total: 0,
})

const emit = defineEmits<{
  (e: 'update:current', v: number): void
  (e: 'change', v: number): void
}>()

const pageCount = computed(() => {
  if (props.total === 0) return 0
  return Math.max(1, Math.ceil(props.total / props.pageSize))
})

const pages = computed(() => {
  if (pageCount.value === 0) return []
  const c = props.current
  const n = pageCount.value
  // 简单展示：最多 5 个页码
  const start = Math.max(1, c - 2)
  const end = Math.min(n, start + 4)
  const realStart = Math.max(1, end - 4)
  const arr: number[] = []
  for (let i = realStart; i <= end; i++) arr.push(i)
  return arr
})

function go(p: number) {
  if (pageCount.value === 0) return
  const next = Math.min(pageCount.value, Math.max(1, p))
  emit('update:current', next)
  emit('change', next)
}
</script>
