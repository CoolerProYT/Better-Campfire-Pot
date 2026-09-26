<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { itemIcon, itemName } from '../bcp'

const props = withDefaults(
  defineProps<{ id?: string | null; count?: number; label?: boolean; size?: 'normal' | 'large' }>(),
  { id: null, count: 1, label: false, size: 'normal' },
)

const name = computed(() => (props.id ? itemName(props.id) : ''))
const src = computed(() => (props.id ? itemIcon(props.id) : null))

// Falls back to initials when an item has no icon or the hosted icon fails to load.
const failed = ref(false)
watch(src, () => (failed.value = false))
const initials = computed(() =>
  name.value
    .split(' ')
    .filter((word) => /^[A-Z]/.test(word))
    .slice(0, 2)
    .map((word) => word[0])
    .join(''),
)
</script>

<template>
  <span class="bcp-item" :class="{ 'with-label': label }">
    <span class="bcp-slot" :class="size" :aria-label="name || undefined" :role="id ? 'img' : undefined">
      <img v-if="src && !failed" class="pixelated" :src="src" alt="" loading="lazy" @error="failed = true" />
      <span v-else-if="id" class="bcp-initials">{{ initials }}</span>
      <span v-if="count > 1" class="bcp-count">{{ count }}</span>
      <span v-if="id && !label" class="bcp-tooltip" aria-hidden="true">{{ name }}</span>
    </span>
    <span v-if="label && id" class="bcp-label">{{ name }}</span>
  </span>
</template>

<style scoped>
.bcp-item {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  vertical-align: middle;
}

.bcp-slot {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  flex: none;
  background: var(--bcp-slot-bg);
  border: 2px solid;
  border-color: var(--bcp-slot-dark) var(--bcp-slot-light) var(--bcp-slot-light) var(--bcp-slot-dark);
}

.bcp-slot.large {
  width: 52px;
  height: 52px;
}

.bcp-slot img {
  width: 32px;
  height: 32px;
}

.bcp-slot.large img {
  width: 48px;
  height: 48px;
}

.bcp-slot:hover {
  background: var(--bcp-slot-hover);
}

.bcp-initials {
  font: 600 12px/1 var(--vp-font-family-mono);
  color: #fff;
  text-shadow: 1px 1px 0 #3f3f3f;
}

.bcp-count {
  position: absolute;
  right: 1px;
  bottom: -1px;
  font: 700 12px/1 var(--vp-font-family-mono);
  color: #fff;
  text-shadow: 1px 1px 0 #3f3f3f;
}

/* Minecraft-style item tooltip. */
.bcp-tooltip {
  position: absolute;
  bottom: calc(100% + 6px);
  left: 50%;
  z-index: 20;
  transform: translateX(-50%);
  padding: 3px 7px;
  white-space: nowrap;
  font-size: 13px;
  line-height: 1.4;
  color: #fff;
  background: rgba(16, 0, 16, 0.94);
  border: 2px solid #2a0a5e;
  border-radius: 3px;
  box-shadow: inset 0 0 0 1px #5000ff55;
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.1s;
}

.bcp-slot:hover .bcp-tooltip {
  opacity: 1;
}

.bcp-label {
  font-weight: 500;
}
</style>
