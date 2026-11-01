# JEI & Jade

Both are optional. Install either one to get the extras below.

## JEI

- Every better pot and upgrade item recipe shows up like any other crafting or smithing recipe.
- Each [upgrade item](./upgrade-items) has an info page explaining which tier it upgrades from and to, and that it is used by sneaking on a placed pot.

## Jade

Looking at a better pot on a campfire shows:

- the pot's **tier**,
- its **speed**, as the same multiplier the item tooltip uses,
- the dish it is **cooking**, and
- the cooking **progress** as a percentage.

The tooltip can be turned off under **Better Campfire Pot** in Jade's plugin settings.

## Stoves

Better pots can cook on Farmer's Delight and Kaleidoscope Cookery stoves, and all 16 colored CobbleFurnies stoves. None of these mods is required.

Right-click the **top** of a stove with a better pot, leaving the space above it empty. Remove any pot already attached to a CobbleFurnies stove first. The pot keeps its normal cooking screen, tier speed, upgrades, redstone control, and item automation. The stove and its inventory stay in place; interact with the stove's side to use its controls.

Farmer's Delight and Kaleidoscope Cookery stoves must be lit. Extinguishing the stove resets cooking progress without consuming ingredients. CobbleFurnies stoves cook without fuel; waterlogged stoves provide no heat.

Sneak and right-click the pot with an empty hand to retrieve it. Breaking the pot or removing the stove drops the pot and its contents.

Modpacks can add other heat sources to the `bettercampfirepot:stoves` block tag. A tagged block heats the pot when its `lit` property is true, or continuously if it has no `lit` property. Waterlogged blocks provide no heat. Optional block entries use `"required": false`.
