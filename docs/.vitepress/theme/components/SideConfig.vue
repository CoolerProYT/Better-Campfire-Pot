<script setup lang="ts">
import { reactive } from 'vue'

// Mirrors CampfirePotSlot (colours included) and the defaults set in BetterCampfireBlockEntity.
const SLOTS = [
  { id: 'INPUT', name: 'Input', color: '#1fff26', detail: 'The 3×3 ingredient grid' },
  { id: 'SEASONING', name: 'Seasoning', color: '#dbbd0f', detail: 'The three seasoning slots' },
  { id: 'OUTPUT', name: 'Output', color: '#de5d07', detail: 'The finished dish, extract only' },
] as const
type Slot = (typeof SLOTS)[number]['id']

const DEFAULTS: Record<string, Slot> = {
  TOP: 'SEASONING',
  LEFT: 'INPUT',
  FRONT: 'INPUT',
  RIGHT: 'INPUT',
  BACK: 'INPUT',
  BOTTOM: 'OUTPUT',
}

// Same layout as the buttons in the pot's config panel.
const LAYOUT = [
  { side: 'TOP', col: 2, row: 1 },
  { side: 'LEFT', col: 1, row: 2 },
  { side: 'FRONT', col: 2, row: 2 },
  { side: 'RIGHT', col: 3, row: 2 },
  { side: 'BACK', col: 1, row: 3 },
  { side: 'BOTTOM', col: 2, row: 3 },
]

const sides = reactive({ ...DEFAULTS })
const slot = (id: Slot) => SLOTS.find((s) => s.id === id)!
const cycle = (side: string) => {
  const index = SLOTS.findIndex((s) => s.id === sides[side])
  sides[side] = SLOTS[(index + 1) % SLOTS.length].id
}
const reset = () => Object.assign(sides, DEFAULTS)
const label = (side: string) => side.charAt(0) + side.slice(1).toLowerCase()
// Back and bottom share a first letter, so both get two.
const SHORT: Record<string, string> = { TOP: 'T', LEFT: 'L', FRONT: 'F', RIGHT: 'R', BACK: 'Bk', BOTTOM: 'Bt' }
</script>

<template>
  <div class="bcp-sides">
    <div class="panel">
      <button
        v-for="cell in LAYOUT"
        :key="cell.side"
        type="button"
        class="side"
        :style="{ gridColumn: cell.col, gridRow: cell.row, '--slot-color': slot(sides[cell.side]).color }"
        :title="`${label(cell.side)}: ${slot(sides[cell.side]).name}`"
        :aria-label="`${label(cell.side)}: ${slot(sides[cell.side]).name}. Click to change slot.`"
        @click="cycle(cell.side)"
      >
        <span class="letter">{{ SHORT[cell.side] }}</span>
      </button>
    </div>
    <div class="info">
      <ul>
        <li v-for="cell in LAYOUT" :key="cell.side">
          <span class="dot" :style="{ background: slot(sides[cell.side]).color }" />
          <strong>{{ label(cell.side) }}</strong>
          <span class="arrow">→</span>
          {{ slot(sides[cell.side]).name }}
          <span v-if="sides[cell.side] !== DEFAULTS[cell.side]" class="changed">changed</span>
        </li>
      </ul>
      <p class="hint">Click a side to cycle it, like in game. <button type="button" class="reset" @click="reset">Reset to defaults</button></p>
      <dl class="legend">
        <template v-for="s in SLOTS" :key="s.id">
          <dt><span class="dot" :style="{ background: s.color }" />{{ s.name }}</dt>
          <dd>{{ s.detail }}</dd>
        </template>
      </dl>
    </div>
  </div>
</template>

<style scoped>
.bcp-sides {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  align-items: flex-start;
  margin: 16px 0;
  padding: 16px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 10px;
  background: var(--vp-c-bg-soft);
}

/* The in-game panel: grey GUI background with 8px buttons, scaled up 5x. */
.panel {
  display: grid;
  grid-template-columns: repeat(3, 40px);
  grid-template-rows: repeat(3, 40px);
  padding: 10px;
  background: #c6c6c6;
  border: 3px solid;
  border-color: #fff #555 #555 #fff;
  image-rendering: pixelated;
}

.side {
  position: relative;
  margin: 3px;
  background: var(--slot-color);
  border: 3px solid;
  border-color: rgba(255, 255, 255, 0.6) rgba(0, 0, 0, 0.35) rgba(0, 0, 0, 0.35) rgba(255, 255, 255, 0.6);
  cursor: pointer;
  transition: transform 0.08s;
}

.side:hover {
  outline: 2px solid #fff;
}

.side:active {
  transform: scale(0.94);
}

.letter {
  font: 700 12px/1 var(--vp-font-family-mono);
  color: rgba(0, 0, 0, 0.55);
}

.info {
  flex: 1;
  min-width: 220px;
}

.info ul {
  margin: 0;
  padding: 0;
  list-style: none;
}

.info li {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0;
  line-height: 1.9;
}

.arrow {
  color: var(--vp-c-text-3);
}

.dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  margin-right: 6px;
  border-radius: 2px;
  border: 1px solid rgba(0, 0, 0, 0.3);
}

.info li .dot {
  margin-right: 0;
}

.changed {
  padding: 0 6px;
  font-size: 11px;
  border-radius: 999px;
  color: var(--vp-c-brand-1);
  background: var(--vp-c-brand-soft);
}

.hint {
  margin: 10px 0;
  font-size: 13px;
  color: var(--vp-c-text-2);
}

.reset {
  margin-left: 4px;
  color: var(--vp-c-brand-1);
  text-decoration: underline;
}

.legend {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 2px 12px;
  margin: 0;
  font-size: 13px;
}

.legend dt {
  display: flex;
  align-items: center;
  font-weight: 600;
}

.legend dd {
  margin: 0;
  color: var(--vp-c-text-2);
}
</style>
