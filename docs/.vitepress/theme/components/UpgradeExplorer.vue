<script setup lang="ts">
import { ref } from 'vue'
import { betterTiers, data, itemIcon, itemName, upgradeId } from '../bcp'
import RecipeCard from './RecipeCard.vue'

// Every tier but netherite can be upgraded from; every tier but vanilla can be upgraded to.
const fromTiers = data.tiers.slice(0, -1)
const toTiers = betterTiers
const order = data.tiers.map((tier) => tier.id)

const selected = ref(upgradeId('vanilla', 'copper'))
const exists = (from: string, to: string) => order.indexOf(to) > order.indexOf(from) && !!data.names[upgradeId(from, to)]
</script>

<template>
  <div class="bcp-upgrades">
    <div class="matrix">
      <table>
        <thead>
          <tr>
            <th class="corner">
              <span>from ↓</span>
              <span>to →</span>
            </th>
            <th v-for="to in toTiers" :key="to.id">{{ to.name }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="from in fromTiers" :key="from.id">
            <th scope="row">{{ from.name }}</th>
            <td v-for="to in toTiers" :key="to.id">
              <button
                v-if="exists(from.id, to.id)"
                type="button"
                class="cell"
                :class="{ active: selected === upgradeId(from.id, to.id) }"
                :aria-pressed="selected === upgradeId(from.id, to.id)"
                :title="itemName(upgradeId(from.id, to.id))"
                @click="selected = upgradeId(from.id, to.id)"
              >
                <img class="pixelated" :src="itemIcon(upgradeId(from.id, to.id)) ?? ''" alt="" loading="lazy" />
              </button>
              <span v-else class="empty" aria-hidden="true" />
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <div class="detail">
      <RecipeCard :result="selected" />
    </div>
  </div>
</template>

<style scoped>
.bcp-upgrades {
  margin: 16px 0;
  padding: 14px 16px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 10px;
  background: var(--vp-c-bg-soft);
}

.matrix {
  overflow-x: auto;
}

.matrix table {
  display: table;
  margin: 0;
  border-collapse: separate;
  border-spacing: 4px;
}

.matrix tr,
.matrix th,
.matrix td {
  border: none;
  background: transparent;
  padding: 0;
}

.matrix thead th {
  padding-bottom: 2px;
  font-size: 12px;
  font-weight: 500;
  text-align: center;
  color: var(--vp-c-text-2);
}

.matrix tbody th {
  padding-right: 8px;
  font-size: 13px;
  text-align: right;
  white-space: nowrap;
}

.corner {
  font-size: 11px !important;
  line-height: 1.2;
  text-align: right !important;
  padding-right: 8px !important;
}

.corner span {
  display: block;
}

.cell,
.empty {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  margin: 0 auto;
}

.cell {
  background: var(--bcp-slot-bg);
  border: 2px solid;
  border-color: var(--bcp-slot-dark) var(--bcp-slot-light) var(--bcp-slot-light) var(--bcp-slot-dark);
  cursor: pointer;
}

.cell:hover {
  background: var(--bcp-slot-hover);
}

.cell.active {
  box-shadow: 0 0 0 2px var(--vp-c-bg-soft), 0 0 0 5px var(--vp-c-brand-1);
}

.cell img {
  width: 36px;
  height: 36px;
}

.empty {
  border-radius: 4px;
  background: var(--vp-c-default-soft);
  opacity: 0.5;
}

.detail {
  margin-top: 10px;
}
</style>
