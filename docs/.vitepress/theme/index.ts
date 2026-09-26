import DefaultTheme from 'vitepress/theme'
import type { Theme } from 'vitepress'
import ItemSlot from './components/ItemSlot.vue'
import PotGallery from './components/PotGallery.vue'
import PotRecipes from './components/PotRecipes.vue'
import RecipeCard from './components/RecipeCard.vue'
import SideConfig from './components/SideConfig.vue'
import TierTable from './components/TierTable.vue'
import UpgradeExplorer from './components/UpgradeExplorer.vue'
import './style.css'

export default {
  extends: DefaultTheme,
  enhanceApp({ app }) {
    app.component('ItemSlot', ItemSlot)
    app.component('PotGallery', PotGallery)
    app.component('PotRecipes', PotRecipes)
    app.component('RecipeCard', RecipeCard)
    app.component('SideConfig', SideConfig)
    app.component('TierTable', TierTable)
    app.component('UpgradeExplorer', UpgradeExplorer)
  },
} satisfies Theme
