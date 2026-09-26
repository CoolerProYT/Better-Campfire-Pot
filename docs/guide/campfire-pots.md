# Campfire pots

Better Campfire Pot adds six tiers of campfire pot. Every colour of Cobblemon campfire pot can be taken to all six tiers, by crafting or with an [upgrade item](./upgrade-items). A better pot keeps its colour at every tier.

![Every tier in every colour, sitting on campfires](/pots.png)

## Tiers

Cooking speed is how much cooking progress the pot adds per tick. The multiplier is the speed compared to a Cobblemon pot, and is what the pot's tooltip and [Jade](./compat) show.

<TierTable />

::: info
These are the default speeds. Each tier's speed can be set from `1` to `200` in the [common config](/config/common). On a server, pot tooltips show the server's speeds.
:::

## Every pot

Hover a pot to see its name.

<PotGallery />

## Placing and using

A better pot works like Cobblemon's pot:

- Place it on a campfire or soul campfire to cook. It can also be placed on its own as a block.
- Open it to reach the nine input slots, three seasoning slots and the output slot.
- Close the lid to cook.

The difference is in how ingredients are matched: **the recipe's grid pattern is ignored**. Put the ingredients in any input slots, stacked however you like, and the pot finds the recipe. This is what makes pots easy to feed from hoppers and autocrafters. See [Menu & automation](./automation).

## Crafting

Each tier is crafted from the tier below it, surrounded by that tier's material. Copper starts from a Cobblemon campfire pot, and Netherite is a smithing upgrade from Emerald. Pick a colour to see its recipes.

<PotRecipes />
