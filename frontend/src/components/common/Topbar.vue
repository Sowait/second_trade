<template>
  <header class="h-16 bg-white border-2 border-slate-900/10 rounded-3xl shadow-card flex items-center justify-between px-5 z-10">
    <div class="flex items-center">
      <button
        class="mr-3 h-10 w-10 inline-flex items-center justify-center rounded-2xl border-2 border-slate-900/10 bg-light-3 text-dark-2 hover:bg-white active:translate-x-[1px] active:translate-y-[1px] transition"
        @click="$emit('toggle-sidebar')"
      >
        <i class="fas fa-bars text-xl"></i>
      </button>

      <div class="relative">
        <input
          v-model="keyword"
          type="text"
          placeholder="搜索..."
          class="pl-10 pr-4 py-2 rounded-2xl border-2 border-slate-900/10 bg-light-3 focus:outline-none focus:ring-2 focus:ring-primary/30 focus:border-primary w-64 md:w-80 transition-all duration-200"
        />
        <i class="fas fa-search absolute left-3 top-1/2 transform -translate-y-1/2 text-light-1"></i>
      </div>
    </div>

    <div class="flex items-center space-x-6">
      <button
        class="relative h-10 w-10 inline-flex items-center justify-center rounded-2xl border-2 border-slate-900/10 bg-light-3 text-dark-2 hover:bg-white active:translate-x-[1px] active:translate-y-[1px] transition"
      >
        <i class="fas fa-bell text-xl"></i>
        <span class="absolute top-0 right-0 w-2 h-2 bg-danger rounded-full"></span>
      </button>

      <UserDropdown />
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref } from "vue";
import UserDropdown from "@/components/common/UserDropdown.vue";

defineEmits<{ (e: "toggle-sidebar"): void }>();
const keyword = ref("");
</script>
