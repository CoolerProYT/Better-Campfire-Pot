import { defineConfig } from 'vitepress'

// GitHub Pages serves a project site from /<repository>/. For a custom domain or a user site, build with DOCS_BASE=/.
const base = process.env.DOCS_BASE ?? '/Better-Campfire-Pot/'
const POT_ICON = 'https://storage.googleapis.com/coolerpromc/textures/bettercampfirepot/netherite_red_campfire_pot.png'

const HOME_ICON =
  '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path fill="currentColor" d="M4 19v-9q0-.475.213-.9t.587-.7l6-4.5q.525-.4 1.2-.4t1.2.4l6 4.5q.375.275.588.7T20 10v9q0 .825-.588 1.413T18 21h-3q-.425 0-.712-.288T14 20v-5q0-.425-.288-.712T13 14h-2q-.425 0-.712.288T10 15v5q0 .425-.288.713T9 21H6q-.825 0-1.412-.587T4 19"/></svg>'

export default defineConfig({
  title: 'Better Campfire Pot',
  description: 'A Cobblemon addon with six faster campfire pot tiers, tier upgrades and automation-friendly cooking.',
  base,
  cleanUrls: true,
  lastUpdated: true,
  srcExclude: ['README.md', 'scripts/**'],
  head: [['link', { rel: 'icon', type: 'image/png', href: POT_ICON }]],
  themeConfig: {
    logo: { src: POT_ICON, alt: '' },
    nav: [
      { text: 'Guide', link: '/guide/getting-started' },
      { text: 'Pots', link: '/guide/campfire-pots' },
      { text: 'Config', link: '/config/common' },
    ],
    sidebar: [
      {
        text: 'Guide',
        items: [
          { text: 'Getting started', link: '/guide/getting-started' },
          { text: 'Campfire pots', link: '/guide/campfire-pots' },
          { text: 'Upgrade items', link: '/guide/upgrade-items' },
          { text: 'Menu & automation', link: '/guide/automation' },
          { text: 'JEI & Jade', link: '/guide/compat' },
        ],
      },
      {
        text: 'Configuration',
        items: [{ text: 'Common config', link: '/config/common' }],
      },
    ],
    socialLinks: [
      { icon: { svg: HOME_ICON }, link: 'https://coolerpromc.com/', ariaLabel: 'CoolerProMC' },
      { icon: 'github', link: 'https://github.com/CoolerProYT/Better-Campfire-Pot' },
      { icon: 'discord', link: 'http://discord.gg/hvFfqsqQm8' },
    ],
    search: { provider: 'local' },
    outline: { level: [2, 3] },
    footer: { message: 'Released under the MIT License.' },
  },
})
