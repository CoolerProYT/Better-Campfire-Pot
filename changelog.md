# 1.8.0
## Changes
- Fabric and NeoForge versions are now built from a single MultiLoader project
- Jade shows a campfire pot's tier, speed, current dish and cooking progress
- JEI shows how to use each tier upgrade item
- New `autoOutput` config option (off by default) that pushes finished dishes into the container on the pot's item-facing side
- Pot tooltips now show the server's configured speed when playing on a server

## Fixes
- Fixed items being duplicated when a pot was broken or removed while its menu was open
- The cooking pot menu now closes when the player moves away or the pot is removed
- Fixed players being able to change a pot's side config, slot lock or saved layout without having its menu open
- Fixed pots continuing to show as cooking while their output slot was blocked
- Campfire pot recipes now update after `/reload`
- Fixed pots on the client sometimes still rendering items that had been taken out
- The "Better Campfire Pot v0" pack is no longer also listed as a data pack on NeoForge

## Performance
- Pots only re-match recipes when their ingredients change
- Pot inventory changes are sent to clients at most once per tick
