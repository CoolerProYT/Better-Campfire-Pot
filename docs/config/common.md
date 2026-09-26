# Common config

The common config sets each tier's cooking speed and turns on auto output. It is shared by the server and every client, and pot tooltips on a server show the server's values.

The file is `config/bettercampfirepot-common.toml` on both Fabric and NeoForge.

## Better Campfire Pot Speed

Cooking progress each tier's pot adds per tick. Every value can be set from `1` to `200`. A Cobblemon pot cooks at `2`.

| Field | Type | Default |
| --- | --- | --- |
| `copper` | Number | `5` |
| `iron` | Number | `10` |
| `gold` | Number | `20` |
| `diamond` | Number | `40` |
| `emerald` | Number | `60` |
| `netherite` | Number | `80` |

## Better Campfire Pot Automation

| Field | Type | Default | Description |
| --- | --- | --- | --- |
| `autoOutput` | Boolean | `false` | Push finished dishes into the container on the pot's item-facing side, the same side leftover items go to. See [Auto output](/guide/automation#auto-output). |

## Example

::: code-group
```toml [bettercampfirepot-common.toml]
["Better Campfire Pot Speed"]
	#Speed of copper tier campfire pot per tick
	#Range: 1 ~ 200
	copper = 5
	#Speed of iron tier campfire pot per tick
	#Range: 1 ~ 200
	iron = 10
	#Speed of gold tier campfire pot per tick
	#Range: 1 ~ 200
	gold = 20
	#Speed of diamond tier campfire pot per tick
	#Range: 1 ~ 200
	diamond = 40
	#Speed of emerald tier campfire pot per tick
	#Range: 1 ~ 200
	emerald = 60
	#Speed of netherite tier campfire pot per tick
	#Range: 1 ~ 200
	netherite = 80

["Better Campfire Pot Automation"]
	#Push finished dishes into the container on the pot's item-facing side, the same side leftover items go to
	autoOutput = false
```
:::
