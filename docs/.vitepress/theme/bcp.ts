// @ts-ignore
import raw from '../data/data.json'

export interface Tier {
  id: string
  name: string
}

export interface Recipe {
  id: string
  type: string
  result: { id: string; count: number }
  pattern?: string[]
  key?: Record<string, string>
  ingredients?: string[]
  template?: string
  base?: string
  addition?: string
}

export const data = raw as unknown as {
  tiers: Tier[]
  colors: string[]
  names: Record<string, string>
  textures: Record<string, string>
  recipes: Recipe[]
  config: { speeds: Record<string, number>; min: number; max: number; autoOutput: boolean }
}

export const MOD = 'bettercampfirepot'

/** Tiers a pot can be upgraded to: everything above vanilla. */
export const betterTiers = data.tiers.filter((tier) => tier.id !== 'vanilla')

/** The pot item for a tier and colour; vanilla is Cobblemon's own pot. */
export function potId(tier: string, color: string): string {
  return tier === 'vanilla' ? `cobblemon:campfire_pot_${color}` : `${MOD}:${tier}_${color}_campfire_pot`
}

export function upgradeId(from: string, to: string): string {
  return `${MOD}:${from}_to_${to}_tier_upgrade`
}

/** Mod items use their in-game name; vanilla ids are turned into readable names. */
export function itemName(id: string): string {
  if (data.names[id]) return data.names[id]
  const path = id.replace(/^#/, '').split(':').pop() ?? id
  return path
    .split('_') // @ts-ignore
    .map((word) => (['of', 'the', 'to'].includes(word) ? word : word.charAt(0).toUpperCase() + word.slice(1)))
    .join(' ')
}

/** Hosted renders of vanilla items, one PNG per item id. Mojang's textures are not bundled here. */
const VANILLA_ICONS = 'https://storage.googleapis.com/coolerpromc/textures'

/** Where to load an item's icon from: the hosted mod and Cobblemon icons listed by the sync script, or the vanilla renders. */
export function itemIcon(id: string): string | null {
  if (data.textures[id]) return data.textures[id]
  // @ts-ignore
  const [namespace, path] = id.includes(':') ? id.split(':') : ['minecraft', id]
  if (namespace !== 'minecraft') return null
  return `${VANILLA_ICONS}/${namespace}/${path}.png`
}

export function findRecipe(id: string): Recipe | undefined {
  return data.recipes.find((r) => r.id === id || r.id === `${MOD}:${id}`)
}

/** The recipe that makes an item. */
export function recipeFor(itemId: string): Recipe | undefined {
  return data.recipes.find((r) => r.result.id === itemId)
}

/** Speed shown on the item tooltip and in Jade: progress per tick relative to a vanilla pot. */
export function multiplier(speed: number): string {
  const value = speed / 2
  // @ts-ignore
  return `${Number.isInteger(value) ? value : value.toFixed(1)}×`
}
