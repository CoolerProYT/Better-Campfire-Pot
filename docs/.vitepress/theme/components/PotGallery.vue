<script setup lang="ts">
import { data, potId } from '../bcp'
import ItemSlot from './ItemSlot.vue'
</script>

<template>
  <div class="bcp-gallery">
    <table>
      <thead>
        <tr>
          <th />
          <th v-for="color in data.colors" :key="color">{{ color }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="tier in data.tiers" :key="tier.id">
          <th scope="row">{{ tier.name }}</th>
          <td v-for="color in data.colors" :key="color">
            <ItemSlot :id="potId(tier.id, color)" />
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
/* Scrolls sideways on phones; tooltips have room because the table sits below its caption row. */
.bcp-gallery {
  margin: 16px 0;
  padding: 36px 16px 12px;
  overflow-x: auto;
  border: 1px solid var(--vp-c-divider);
  border-radius: 10px;
  background: var(--vp-c-bg-soft);
}

.bcp-gallery table {
  display: table;
  margin: 0;
  border-collapse: collapse;
}

.bcp-gallery tr,
.bcp-gallery th,
.bcp-gallery td {
  border: none;
  background: transparent;
}

.bcp-gallery th,
.bcp-gallery td {
  padding: 0;
}

.bcp-gallery thead th {
  padding: 0 3px 6px;
  font-size: 12px;
  font-weight: 500;
  text-transform: capitalize;
  text-align: center;
  color: var(--vp-c-text-2);
}

.bcp-gallery tbody th {
  padding-right: 12px;
  font-size: 13px;
  text-align: right;
  white-space: nowrap;
}

.bcp-gallery td {
  width: 40px;
  text-align: center;
}
</style>
