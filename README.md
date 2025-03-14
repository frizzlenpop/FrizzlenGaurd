# FrizzlenGaurd - Advanced Land Protection System

FrizzlenGaurd is a powerful and flexible land protection plugin for Minecraft servers, offering advanced region management with granular permissions, visual feedback, and extensive customization options.

## Table of Contents

- [Introduction](#introduction)
- [Key Features](#key-features)
- [Installation](#installation)
- [Basic Usage](#basic-usage)
- [Command Reference](#command-reference)
- [Permission System](#permission-system)
- [Flag System](#flag-system)
- [Configuration](#configuration)
- [Detailed Documentation](#detailed-documentation)

## Introduction

FrizzlenGaurd allows players to protect their builds by creating claims (regions) where other players' actions are restricted based on configurable permissions. With an extensive flag system and role-based permissions, you can fine-tune exactly what actions are allowed within each region.

## Key Features

### Core Protection
- **Advanced Land Claiming**: Create and manage protected regions using cuboid selections
- **Multi-tier Permissions**: Granular control with OWNER, MEMBER, and VISITOR roles
- **Subregions Support**: Create nested regions with inheritance or override settings
- **Flag System**: 40+ customizable region flags to control specific behaviors
- **Multi-world Support**: Protect regions across different worlds with world exclusion options

### Management Features
- **Visual Feedback**: Particle effects for region boundaries and selection preview
- **GUI Interface**: User-friendly menus for region management
- **Logging System**: Track changes and modifications within regions
- **Vault Integration**: Complete Vault support for land claiming costs and transactions

### Permission Features
- **Rank-Based Claim System**: Scale claim limits based on player ranks/permissions
- **Permission Controlled Blocks**: Define claim block limits based on permissions (250 for standard users)
- **Claim Limit Control**: Permission-based number of claims (1 claim for standard users)
- **Admin Bypass**: Special permissions for administrative control

### Admin Tools
- **Admin Claims**: Powerful tool for creating server-controlled regions without restrictions
- **Region Management**: Tools for merging, resizing and monitoring regions
- **Backup System**: Automated backup and restore functionality

## Installation

1. Download the latest release from the GitHub repository
2. Place the JAR file in your server's `plugins` folder
3. Make sure Vault is installed for economy features
4. Restart your server
5. Configure the plugin in `plugins/FrizzlenGaurd/config.yml`

## Basic Usage

### Creating a Claim

1. Get a selection tool: `/fg claim`
2. Left-click to set position 1
3. Right-click to set position 2
4. Create the claim: `/fg claim <name>`

### Managing Permissions

1. Add a member: `/fg addfriend <region> <player> [role]`
2. Remove a member: `/fg removefriend <region> <player>`
3. Set member role: `/fg setrole <region> <player> <role>`

### Setting Flags

Set region flags: `/fg setflag <region> <flag> <value>`

For example:
- `/fg setflag myregion pvp false` - Disable PvP in your region
- `/fg setflag myregion chest-access true` - Allow visitors to open chests

## Command Reference

### Player Commands
- `/fg claim` - Start a claim session / get a claim stick
- `/fg claim pos1` - Set first position at current location
- `/fg claim pos2` - Set second position at current location
- `/fg claim <name>` - Create a claim with selected points
- `/fg claim preview` - Toggle selection preview
- `/fg subclaim <name>` - Create a subregion
- `/fg addfriend <region> <player> [role]` - Add member to region
- `/fg removefriend <region> <player>` - Remove member from region
- `/fg setrole <region> <player> <role>` - Change member's role
- `/fg regioninfo [region]` - Display region details
- `/fg setflag <region> <flag> <value>` - Toggle region flag
- `/fg listregions` - List your accessible regions
- `/fg gui` - Open management GUI
- `/fg logs <region>` - View region logs
- `/fg blocks` - Check your available claim blocks

### Admin Commands
- `/fg adminclaim <name>` - Create an admin claim without restrictions
- `/fg delregion <region>` - Force delete region
- `/fg backup` - Create data backup
- `/fg reload` - Reload configuration
- `/fg scan` - Check for issues
- `/fg merge <target> <source>` - Merge regions
- `/fg resize <region>` - Modify region size

## Permission System

FrizzlenGaurd uses a role-based permission system within regions:

- **OWNER**: Full control over the region
- **MEMBER**: Can build and interact, but cannot modify flags or manage members
- **VISITOR**: Limited interaction based on region flags

### Core Permissions

- `frizzlengaurd.claim` - Allow claiming regions
- `frizzlengaurd.subclaim` - Allow creating subregions
- `frizzlengaurd.addfriend` - Allow adding members to regions
- `frizzlengaurd.setflag` - Allow setting region flags
- `frizzlengaurd.admin.claim` - Allow creating admin claims
- `frizzlengaurd.admin.bypass` - Bypass region restrictions
- `frizzlengaurd.admin.*` - All admin permissions

### Claim Limit Permissions

- `frizzlengaurd.blocks.basic` - Grant 250 claim blocks (standard)
- `frizzlengaurd.blocks.premium` - Grant 500 claim blocks
- `frizzlengaurd.blocks.vip` - Grant 1000 claim blocks
- `frizzlengaurd.blocks.elite` - Grant 2500 claim blocks

- `frizzlengaurd.claims.basic` - Allow 1 claim (standard)
- `frizzlengaurd.claims.premium` - Allow 2 claims
- `frizzlengaurd.claims.vip` - Allow 3 claims
- `frizzlengaurd.claims.elite` - Allow 5 claims

## Flag System

FrizzlenGaurd features an extensive flag system with 40+ flags to control nearly every aspect of gameplay within regions. [See detailed flag documentation](FLAGS.md).

Key flag categories:
- **Protection flags**: pvp, mob-spawning, explosions, fire-spread
- **Interaction flags**: chest-access, door-use, button-use, lever-use
- **Redstone flags**: repeater-use, comparator-use, redstone
- **Entity flags**: animal-breeding, animal-damage, villager-trade
- **Block-specific flags**: furnace-use, anvil-use, crafting-table-use

## Configuration

The plugin creates the following configuration files:
- `config.yml` - Main configuration
- `regions.yml` - Region data storage
- `messages.yml` - Customizable messages

See the [Configuration Guide](CONFIGURATION.md) for detailed information on all configuration options.

## Detailed Documentation

For more in-depth information about specific features, please refer to these detailed guides:

- [Claiming System](CLAIMING.md) - Detailed guide on creating and managing claims
- [Flag System](FLAGS.md) - Complete list of flags and their effects
- [Admin Tools](ADMIN.md) - Guide for server administrators
- [Role System](ROLES.md) - Details on the permission roles
- [Configuration Guide](CONFIGURATION.md) - Complete configuration reference 