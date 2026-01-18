<template>
  <div class="app-root">
    <div class="app-shell">
      <Sidebar :collapsed="collapsed" @toggle="collapsed = !collapsed" />

      <div class="app-main app-surface p-4">
        <Topbar @toggle-sidebar="collapsed = !collapsed" />
        <main class="app-content mt-4 p-0">
          <slot />
        </main>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import Sidebar from "@/components/common/Sidebar.vue";
import Topbar from "@/components/common/Topbar.vue";

const collapsed = ref(false);
</script>
