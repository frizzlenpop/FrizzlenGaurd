# FrizzlenGaurd Claiming System

This document provides detailed information about creating and managing claims in [FrizzlenGaurd](README.md).

## Table of Contents

- [Introduction](#introduction)
- [Claim Basics](#claim-basics)
- [Creating Claims](#creating-claims)
- [Managing Claim Size](#managing-claim-size)
- [Subclaims](#subclaims)
- [Claim Visualization](#claim-visualization)
- [Claim Commands](#claim-commands)
- [Claim Permissions](#claim-permissions)

## Introduction

Claiming land in FrizzlenGaurd allows you to protect your builds from griefing and unwanted modifications. The system uses a cuboid (3D box) selection method to define the boundaries of your protected area.

## Claim Basics

- **Claims are 3D**: Claims protect from bedrock to sky limit
- **Claim Blocks**: Players have a limited number of claim blocks that restrict how large their claims can be
- **Multiple Claims**: Players can create multiple claims up to a configured limit
- **Owner Control**: Claim owners can add members, set permissions, and customize protection flags

## Creating Claims

### Getting a Claim Stick

To start creating a claim, you need a selection tool:

```
/fg claim
```

This will either give you a selection tool or start a claim session.

### Setting Selection Points

With the selection tool in hand:

1. **Left-click** a block to mark the first corner (Position 1)
2. **Right-click** another block to mark the second corner (Position 2)

These two points will define the opposite corners of your claim's 3D box.

Alternatively, you can set points at your current location:

```
/fg claim pos1
/fg claim pos2
```

### Previewing Your Selection

Before finalizing your claim, you can preview the boundaries:

```
/fg claim preview
```

This will show particle effects around the borders of your selection.

### Creating the Claim

Once you're satisfied with your selection, create the claim:

```
/fg claim <name>
```

For example:
```
/fg claim MyBase
```

### Claim Requirements

Claims must meet several requirements:

- Must be larger than the minimum size (default: 25 blocks)
- Must not exceed the maximum size (default: 10,000 blocks)
- Must not overlap with other claims (unless you own them)
- You must have enough claim blocks
- You must not exceed the maximum claims per player

## Managing Claim Size

### Claim Blocks

Each player has a limited number of claim blocks that determine how large their claims can be:

- New players start with a default amount (configurable in config.yml)
- Players earn more claim blocks by playing on the server
- Admins can award additional claim blocks

### Checking Your Claim Blocks

To see how many claim blocks you have available:

```
/fg blocks
```

### Resizing Claims

To modify an existing claim's size:

```
/fg resize <region>
```

Follow the on-screen instructions to select new boundaries.

## Subclaims

Subclaims are regions within your main claim that can have separate permissions. They're useful for:

- Giving specific players access to certain areas
- Creating separate permission zones within a large claim
- Restricting access to valuable items even from trusted members

### Creating a Subclaim

1. Stand inside your main claim
2. Select two points as with a regular claim
3. Create the subclaim:
```
/fg subclaim <name>
```

## Claim Visualization

FrizzlenGaurd provides several ways to visualize claims:

### Boundary Visualization

To see the boundaries of a claim:

```
/fg visualize <region>
```

This will display particle effects around the claim's borders.

### Claim Information

To get detailed information about a claim:

```
/fg regioninfo <region>
```

This shows:
- Owner and members
- Dimensions and size
- Flags and settings
- Creation date

## Claim Commands

| Command | Description |
|---------|-------------|
| `/fg claim` | Get a selection tool |
| `/fg claim pos1` | Set first position at current location |
| `/fg claim pos2` | Set second position at current location |
| `/fg claim <name>` | Create a claim with selected points |
| `/fg claim preview` | Toggle selection preview |
| `/fg subclaim <name>` | Create a subclaim |
| `/fg resize <region>` | Resize a claim |
| `/fg visualize <region>` | Show claim boundaries |
| `/fg regioninfo <region>` | Display claim details |
| `/fg listregions` | List your claims |

## Claim Permissions

| Permission | Description |
|------------|-------------|
| `frizzlengaurd.claim` | Allow claiming regions |
| `frizzlengaurd.subclaim` | Allow creating subclaims |
| `frizzlengaurd.resize` | Allow resizing claims |
| `frizzlengaurd.visualize` | Allow visualizing claims |
| `frizzlengaurd.info` | Allow viewing claim information | 