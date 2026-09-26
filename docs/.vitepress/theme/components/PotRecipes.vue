<script setup lang="ts">
import { ref } from 'vue'
import { betterTiers, potId } from '../bcp'
import ColorPicker from './ColorPicker.vue'
import RecipeCard from './RecipeCard.vue'

const color = ref('red')
</script>

<template>
  <div class="bcp-pot-recipes">
    <ColorPicker v-model="color" />
    <div v-for="tier in betterTiers" :key="tier.id" class="step">
      <h4>{{ tier.name }}</h4>
      <RecipeCard :result="potId(tier.id, color)" />
    </div>
  </div>
</template>

<style scoped>
.bcp-pot-recipes {
  margin: 16px 0;
}

.step h4 {
  margin: 18px 0 0;
  font-size: 14px;
  color: var(--vp-c-text-2);
}
</style>
