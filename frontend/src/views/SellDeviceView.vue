<template>
  <div>
    <div v-if="sellerId == null" class="p-6 text-sm text-neutral-600">
      加载中...
    </div>

    <SellProductList
      v-else
      :key="listKey"
      :sellerId="sellerId"
      @open-wizard="openCreateWizard"
      @view="onView"
      @edit="onEdit"
      @unlist="onUnlist"
    />

    <ProductModal v-model:open="detailOpen" :product="activeProduct" />

    <Teleport to="body">
      <SellWizard
        v-if="wizardOpen"
        v-model:open="wizardOpen"
        :mode="wizardMode"
        :productId="editingProductId"
        @close="wizardOpen = false"
        @success="onPublishSuccess"
      />
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import http from "@/api/http";
import SellProductList from "@/components/common/SellProductList.vue";
import SellWizard from "@/components/common/SellWizard.vue";
import ProductModal from "@/components/common/ProductModal.vue";

const wizardOpen = ref(false);
const wizardMode = ref<"create" | "edit">("create");
const editingProductId = ref<number | null>(null);
const sellerId = ref<number | null>(null);
const listKey = ref(0);

const detailOpen = ref(false);
const activeProduct = ref<any | null>(null);

async function fetchMe() {
  const { data } = await http.get("/api/auth/me");
  const id = data?.id ?? data?.user?.id;
  sellerId.value = id != null ? Number(id) : null;
}

function onPublishSuccess() {
  wizardOpen.value = false;
  wizardMode.value = "create";
  editingProductId.value = null;
  // 强制刷新列表（最稳妥，避免依赖子组件内部 watch 逻辑）
  listKey.value += 1;
}

function openCreateWizard() {
  wizardMode.value = "create";
  editingProductId.value = null;
  wizardOpen.value = true;
}

function onView(id: any) {
  activeProduct.value = { id };
  detailOpen.value = true;
}
function onEdit(id: any) {
  const pid = Number(id);
  wizardMode.value = "edit";
  editingProductId.value = Number.isFinite(pid) ? pid : null;
  wizardOpen.value = true;
}
function onUnlist(id: any) {
  console.log("unlist", id);
}

onMounted(() => {
  fetchMe();
});
</script>
