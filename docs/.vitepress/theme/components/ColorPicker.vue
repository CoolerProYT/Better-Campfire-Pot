<script setup lang="ts">
import { data, itemIcon } from '../bcp'

const model = defineModel<string>({ required: true })
</script>

<template>
  <div class="bcp-colors" role="radiogroup" aria-label="Pot colour">
    <button
      v-for="color in data.colors"
      :key="color"
      type="button"
      role="radio"
      :aria-checked="model === color"
      :class="{ active: model === color }"
      :title="color"
      @click="model = color"
    >
      <img class="pixelated" :src="itemIcon(`cobblemon:campfire_pot_${color}`) ?? ''" alt="" />
      <span>{{ color }}</span>
    </button>
  </div>
</template>

<style scoped>
.bcp-colors {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

button {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px 3px 4px;
  font-size: 13px;
  text-transform: capitalize;
  color: var(--vp-c-text-2);
  border: 1px solid var(--vp-c-divider);
  border-radius: 999px;
  background: var(--vp-c-bg);
  transition: border-color 0.2s, color 0.2s, background 0.2s;
}

button:hover {
  color: var(--vp-c-text-1);
  border-color: var(--vp-c-brand-2);
}

button.active {
  color: var(--vp-c-brand-1);
  border-color: var(--vp-c-brand-1);
  background: var(--vp-c-brand-soft);
}

img {
  width: 20px;
  height: 20px;
}
</style>
