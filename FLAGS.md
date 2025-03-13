# FrizzlenGaurd Flag System

This document provides detailed information about the flag system in [FrizzlenGaurd](README.md), explaining each available flag and its effect on region behavior.

## Table of Contents

- [Introduction](#introduction)
- [Setting Flags](#setting-flags)
- [Flag Categories](#flag-categories)
  - [Protection Flags](#protection-flags)
  - [Block Interaction Flags](#block-interaction-flags)
  - [Entity Interaction Flags](#entity-interaction-flags)
  - [Redstone Flags](#redstone-flags)
  - [Container Access Flags](#container-access-flags)
  - [Crafting Station Flags](#crafting-station-flags)
  - [Item Flags](#item-flags)

## Introduction

Flags in FrizzlenGaurd allow you to control specific behaviors within your regions. Each flag can be set to `true` (allowed) or `false` (denied), providing granular control over what actions players can perform in your region, even if they aren't members.

## Setting Flags

Flags can be set using the command:
```
/fg setflag <region> <flag> <value>
```

Where:
- `<region>` is the name of your region
- `<flag>` is one of the flags listed below
- `<value>` can be: true/false, on/off, allow/deny, or yes/no

Example:
```
/fg setflag myregion pvp false
/fg setflag myregion door-use true
```

## Flag Categories

### Protection Flags

| Flag | Description | Default |
|------|-------------|---------|
| `pvp` | Allow player vs player combat | false |
| `mob-spawning` | Allow mobs to spawn | true |
| `mob-damage` | Allow mobs to damage players | false |
| `explosions` | Allow explosions (TNT, creepers) | false |
| `fire-spread` | Allow fire to spread | false |
| `leaf-decay` | Allow leaves to decay | true |
| `crop-trample` | Allow crops to be trampled | false |
| `piston-protection` | Allow pistons to move blocks | true |

### Block Interaction Flags

| Flag | Description | Default |
|------|-------------|---------|
| `door-use` | Allow using doors | true |
| `trapdoor-use` | Allow using trapdoors | true |
| `fence-gate-use` | Allow using fence gates | true |
| `button-use` | Allow pressing buttons | true |
| `lever-use` | Allow toggling levers | true |
| `pressure-plate` | Allow stepping on pressure plates | true |
| `noteblock-use` | Allow playing note blocks | true |
| `jukebox-use` | Allow using jukeboxes | true |

### Entity Interaction Flags

| Flag | Description | Default |
|------|-------------|---------|
| `animal-breeding` | Allow breeding animals | true |
| `animal-damage` | Allow damaging animals | false |
| `villager-trade` | Allow trading with villagers | true |
| `armor-stands` | Allow modifying armor stands | false |
| `item-frames` | Allow modifying item frames | false |
| `paintings` | Allow modifying paintings | false |

### Redstone Flags

| Flag | Description | Default |
|------|-------------|---------|
| `redstone` | Allow redstone circuits to function | true |
| `repeater-use` | Allow modifying repeaters | true |
| `comparator-use` | Allow modifying comparators | true |

### Container Access Flags

| Flag | Description | Default |
|------|-------------|---------|
| `chest-access` | Allow accessing chests and other containers | false |
| `hopper-use` | Allow accessing hoppers | false |
| `dispenser-use` | Allow accessing dispensers | false |
| `dropper-use` | Allow accessing droppers | false |

### Crafting Station Flags

| Flag | Description | Default |
|------|-------------|---------|
| `furnace-use` | Allow using furnaces, blast furnaces, and smokers | false |
| `crafting-table-use` | Allow using crafting tables | true |
| `enchanting-table-use` | Allow using enchanting tables | true |
| `anvil-use` | Allow using anvils | true |
| `grindstone-use` | Allow using grindstones | true |
| `smithing-table-use` | Allow using smithing tables | true |
| `loom-use` | Allow using looms | true |
| `campfire-use` | Allow using campfires | true |
| `cauldron-use` | Allow using cauldrons | true |

### Item Flags

| Flag | Description | Default |
|------|-------------|---------|
| `item-pickup` | Allow item pickup | true |
| `item-drop` | Allow dropping items | true |
| `exp-pickup` | Allow experience orb pickup | true | 