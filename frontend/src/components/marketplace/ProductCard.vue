<template>
  <div class="bg-white/70 backdrop-blur-xl border border-light-2/80 rounded-2xl shadow-card overflow-hidden hover:shadow-lg transition-all duration-300 group hover:-translate-y-0.5" @click="onCardClick">
    <div class="relative">
      <img :src="item.cover" :alt="item.title" class="w-full h-48 object-cover" />
      <div class="absolute top-3 left-3 text-white text-xs font-medium px-2.5 py-1.5 rounded-full shadow-sm" :class="item.conditionColorClass">
        {{ item.conditionTag }}
      </div>
      <div class="absolute top-3 right-3 bg-white/80 backdrop-blur text-primary text-xs font-medium px-2.5 py-1.5 rounded-full border border-light-2/70">
        <i class="fas fa-heart mr-1"></i>{{ item.favCount }}
      </div>
    </div>

    <div class="p-4">
      <div class="flex items-center mb-2">
        <span class="text-xs bg-light-3 text-dark-2 px-2.5 py-1 rounded-full border border-light-2/70">{{ item.brandText }}</span>
        <div class="ml-auto flex items-center">
          <i class="fas fa-star text-warning text-xs"></i>
          <span class="text-xs text-dark-2 ml-1">{{ item.rating }}</span>
        </div>
      </div>

      <h3 class="font-medium text-dark mb-1 group-hover:text-primary transition-colors">
        {{ item.title }}
      </h3>

      <p class="text-sm text-dark-2 mb-3 line-clamp-2">{{ item.desc }}</p>

      <div class="flex items-center justify-between">
        <div>
          <span class="text-lg font-bold text-danger">{{ item.price }}</span>
          <span class="text-xs text-light-1 line-through ml-2">{{ item.oldPrice }}</span>
        </div>

        <button
          class="bg-primary/10 text-primary p-2.5 rounded-full hover:bg-primary/20 transition-colors border border-primary/10"
          @click.stop="addToCart"
        >
          <i class="fas fa-shopping-cart"></i>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{ item: any }>();
const emit = defineEmits<{ (e: "open"): void }>();

function onCardClick() {
  emit("open");
}
function addToCart() {
  // TODO：接后端购物车/下单
}
</script>
