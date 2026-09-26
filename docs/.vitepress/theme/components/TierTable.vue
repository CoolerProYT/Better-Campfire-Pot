<script setup lang="ts">
import { ref } from 'vue'
import { data, multiplier, potId } from '../bcp'
import ColorPicker from './ColorPicker.vue'
import ItemSlot from './ItemSlot.vue'

const color = ref('red')
const fastest = Math.max(...Object.values(data.config.speeds))
</script>

<template>
  <div class="bcp-tiers">
    <ColorPicker v-model="color" />
    <div class="rows">
      <div v-for="tier in data.tiers" :key="tier.id" class="row" :class="{ vanilla: tier.id === 'vanilla' }">
        <ItemSlot :id="potId(tier.id, color)" />
        <span class="name">{{ tier.name }}</span>
        <span class="bar" aria-hidden="true">
          <span :style="{ width: `${(data.config.speeds[tier.id] / fastest) * 100}%` }" :class="`tier-${tier.id}`" />
        </span>
        <span class="speed">
          <strong>{{ data.config.speeds[tier.id] }}</strong>
          <span class="unit">/ tick</span>
        </span>
        <span class="mult">{{ multiplier(data.config.speeds[tier.id]) }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.bcp-tiers {
  margin: 16px 0;
  padding: 14px 16px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 10px;
  background: var(--vp-c-bg-soft);
}

.rows {
  display: grid;
  gap: 6px;
  margin-top: 14px;
}

.row {
  display: grid;
  grid-template-columns: 36px 90px 1fr 76px 52px;
  align-items: center;
  gap: 12px;
}

.row.vanilla .name {
  color: var(--vp-c-text-2);
}

.name {
  font-weight: 600;
}

.bar {
  height: 10px;
  border-radius: 999px;
  background: var(--vp-c-default-soft);
  overflow: hidden;
}

.bar span {
  display: block;
  height: 100%;
  min-width: 4px;
  border-radius: 999px;
  background: var(--vp-c-text-3);
}

.bar .tier-copper { background: #d9794f; }
.bar .tier-iron { background: #b8b8b8; }
.bar .tier-gold { background: #f2c230; }
.bar .tier-diamond { background: #4ee0d6; }
.bar .tier-emerald { background: #2fcf6a; }
.bar .tier-netherite { background: #5a4a4f; }

.speed {
  text-align: right;
  font-variant-numeric: tabular-nums;
}

.unit {
  margin-left: 3px;
  font-size: 12px;
  color: var(--vp-c-text-2);
}

.mult {
  text-align: right;
  font: 600 13px var(--vp-font-family-mono);
  color: var(--vp-c-brand-1);
}

@media (max-width: 520px) {
  .row {
    grid-template-columns: 36px 1fr 70px 46px;
  }

  .bar {
    display: none;
  }
}
</style>
