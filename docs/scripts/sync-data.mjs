// Pulls wiki data straight from the mod so the docs never drift from the game:
// datagen output (recipes, item models), the lang file, the config defaults and the pot menu texture.
// Run `./gradlew :neoforge:runData` first when the mod's data changes.
import { copyFileSync, existsSync, mkdirSync, readdirSync, readFileSync, writeFileSync } from 'node:fs'
import { basename, dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

const docs = join(dirname(fileURLToPath(import.meta.url)), '..')
const root = join(docs, '..')
const generated = join(root, 'common/src/generated/resources')
const assets = join(root, 'common/src/main/resources/assets/bettercampfirepot')
const data = join(generated, 'data/bettercampfirepot')
const configSource = join(root, 'common/src/main/java/com/coolerpromc/bettercampfirepot/BetterCampfirePotConfig.java')

if (!existsSync(generated)) {
  console.error(`No datagen output at ${generated}. Run ./gradlew :neoforge:runData first.`)
  process.exit(1)
}

const readJson = (file) => JSON.parse(readFileSync(file, 'utf8'))
const jsonFiles = (dir) => (existsSync(dir) ? readdirSync(dir).filter((f) => f.endsWith('.json')).sort() : [])
const capitalize = (word) => word.charAt(0).toUpperCase() + word.slice(1)

const lang = readJson(join(assets, 'lang/en_us.json'))

// Same order as Tiers.TIERS in the mod.
const tiers = ['vanilla', 'copper', 'iron', 'gold', 'diamond', 'emerald', 'netherite'].map((id) => ({
  id,
  name: lang[`tier.bettercampfirepot.${id}`] ?? capitalize(id),
}))
const tierName = Object.fromEntries(tiers.map((tier) => [tier.id, tier.name]))

// Cobblemon's pot colours, in its creative tab order.
const colors = ['red', 'yellow', 'green', 'blue', 'pink', 'black', 'white'].filter((color) =>
  existsSync(join(assets, `textures/item/campfire_pot/campfire_pot_${color}.png`)),
)

// Pot and upgrade names are built in code from translation keys (see BetterCampfirePotItem and TierUpgradeItem),
// so they are assembled here the same way.
const names = {}
for (const color of colors) {
  names[`cobblemon:campfire_pot_${color}`] = `${capitalize(color)} Campfire Pot`
}
const itemModels = jsonFiles(join(generated, 'assets/bettercampfirepot/models/item')).map((file) => basename(file, '.json'))
for (const item of itemModels) {
  const pot = item.match(/^([a-z]+)_([a-z]+)_campfire_pot$/)
  const upgrade = item.match(/^([a-z]+)_to_([a-z]+)_tier_upgrade$/)
  if (pot) names[`bettercampfirepot:${item}`] = `${tierName[pot[1]]} ${names[`cobblemon:campfire_pot_${pot[2]}`]}`
  else if (upgrade) names[`bettercampfirepot:${item}`] = `${tierName[upgrade[1]]} to ${tierName[upgrade[2]]} Tier Upgrade`
}

// Item models are layered, so the icons are flattened and hosted at 1024x1024 alongside the vanilla renders.
// Upload new ones there before syncing (see README.md).
const HOSTED_TEXTURES = 'https://storage.googleapis.com/coolerpromc/textures'
const textures = {}
for (const item of itemModels) textures[`bettercampfirepot:${item}`] = `${HOSTED_TEXTURES}/bettercampfirepot/${item}.png`
for (const color of colors) textures[`cobblemon:campfire_pot_${color}`] = `${HOSTED_TEXTURES}/cobblemon/campfire_pot_${color}.png`

mkdirSync(join(docs, 'public/gui'), { recursive: true })
copyFileSync(join(assets, 'textures/gui/campfire_pot.png'), join(docs, 'public/gui/campfire_pot.png'))

const ingredient = (value) => {
  if (typeof value === 'string') return value
  if (Array.isArray(value)) return ingredient(value[0])
  if (value?.item) return value.item
  if (value?.tag) return `#${value.tag}`
  return '?'
}

const recipes = jsonFiles(join(data, 'recipe')).map((file) => {
  const json = readJson(join(data, 'recipe', file))
  const recipe = { id: `bettercampfirepot:${basename(file, '.json')}`, type: json.type, result: { id: json.result?.id, count: json.result?.count ?? 1 } }
  if (json.type === 'minecraft:crafting_shaped') {
    recipe.pattern = json.pattern
    recipe.key = Object.fromEntries(Object.entries(json.key).map(([symbol, value]) => [symbol, ingredient(value)]))
  } else if (json.type === 'minecraft:crafting_shapeless') {
    recipe.ingredients = json.ingredients.map(ingredient)
  } else if (json.type === 'minecraft:smithing_transform') {
    recipe.template = ingredient(json.template)
    recipe.base = ingredient(json.base)
    recipe.addition = ingredient(json.addition)
  }
  return recipe
})

// Config defaults, read from the ModConfigSpec builder calls.
const configJava = readFileSync(configSource, 'utf8')
const speeds = { vanilla: 2 }
for (const [, key, value, min, max] of configJava.matchAll(/defineInRange\("([a-z]+)",\s*(\d+),\s*(\d+),\s*(\d+)\)/g)) {
  speeds[key] = Number(value)
  speeds.min = Number(min)
  speeds.max = Number(max)
}
const autoOutput = /define\("autoOutput",\s*(true|false)\)/.exec(configJava)?.[1] === 'true'
const config = { speeds: Object.fromEntries(tiers.map((tier) => [tier.id, speeds[tier.id]])), min: speeds.min, max: speeds.max, autoOutput }

mkdirSync(join(docs, '.vitepress/data'), { recursive: true })
writeFileSync(
  join(docs, '.vitepress/data/data.json'),
  JSON.stringify({ tiers, colors, names, textures, recipes, config }, null, 2),
)
console.log(`Synced ${tiers.length} tiers, ${colors.length} colours, ${recipes.length} recipes, ${Object.keys(textures).length} textures.`)
