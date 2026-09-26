<script setup lang="ts">
import { computed } from 'vue'
import { findRecipe, recipeFor } from '../bcp'
import ItemSlot from './ItemSlot.vue'

/** Pass a recipe `id`, or the `result` item to show the recipe that makes it. */
const props = defineProps<{ id?: string; result?: string }>()

const recipe = computed(() => (props.id ? findRecipe(props.id) : props.result ? recipeFor(props.result) : undefined))

/** Nine cells for the crafting grid, left to right, top to bottom. */
const grid = computed<(string | null)[]>(() => {
  const r = recipe.value
  if (!r) return []
  if (r.pattern && r.key) {
    const cells: (string | null)[] = []
    for (let row = 0; row < 3; row++) {
      for (let col = 0; col < 3; col++) {
        const symbol = r.pattern[row]?.[col] ?? ' '
        cells.push(symbol === ' ' ? null : r.key[symbol] ?? null)
      }
    }
    return cells
  }
  if (r.ingredients) {
    return Array.from({ length: 9 }, (_, i) => r.ingredients![i] ?? null)
  }
  return []
})
</script>

<template>
  <div v-if="recipe" class="bcp-recipe">
    <template v-if="recipe.template">
      <div class="row">
        <ItemSlot :id="recipe.template" />
        <ItemSlot :id="recipe.base" />
        <ItemSlot :id="recipe.addition" />
      </div>
      <span class="arrow">
        <span class="station">Smithing</span>
        ➜
      </span>
    </template>
    <template v-else>
      <div class="grid">
        <ItemSlot v-for="(cell, i) in grid" :id="cell" :key="i" />
      </div>
      <span class="arrow">
        <span class="station">Crafting</span>
        ➜
      </span>
    </template>
    <ItemSlot :id="recipe.result.id" :count="recipe.result.count" label />
  </div>
  <p v-else class="bcp-muted">Recipe {{ id ?? result }} not found.</p>
</template>

<style scoped>
.bcp-recipe {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 14px;
  margin: 12px 0;
  padding: 12px 16px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 10px;
  background: var(--vp-c-bg-soft);
}

.grid {
  display: grid;
  grid-template-columns: repeat(3, 36px);
}

.row {
  display: flex;
}

.arrow {
  display: flex;
  flex-direction: column;
  align-items: center;
  font-size: 22px;
  line-height: 1;
  color: var(--vp-c-text-2);
}

.station {
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}
</style>
