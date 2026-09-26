# Better Campfire Pot wiki

VitePress site for the mod. Recipes, item names, tier speeds and config defaults are read from the mod itself, so regenerate the mod's data before building when it changes.

```bash
./gradlew :neoforge:runData   # from the repository root, when mod data changed
cd docs
npm install
npm run dev                   # syncs data, then serves http://localhost:5173
npm run build                 # syncs data, then builds to .vitepress/dist
```

`npm run sync` (run automatically by `dev` and `build`) writes `.vitepress/data/data.json` and copies the pot menu texture to `public/gui/`. Both are git-ignored.

Item icons are not bundled. Every icon loads from `https://storage.googleapis.com/coolerpromc/textures/<namespace>/<name>.png` (set in `.vitepress/theme/bcp.ts`):

- `minecraft/` for vanilla items,
- `cobblemon/campfire_pot_<color>` for Cobblemon's campfire pots,
- `bettercampfirepot/` for this mod's pots and upgrade items.

The mod's item models are layered (pot colour + tier band + upgrade arrow), so the hosted icons are those layers flattened into one PNG and scaled to 1024x1024. When a new item is added, flatten and upload its icon before building, or the page falls back to the item's initials.
