# FrizzlenGaurd Configuration Guide

This document provides detailed information about configuring [FrizzlenGaurd](README.md), explaining all available settings and options.

## Table of Contents

- [Introduction](#introduction)
- [Configuration Files](#configuration-files)
- [General Settings](#general-settings)
- [Economy Settings](#economy-settings)
- [Claim Settings](#claim-settings)
- [Permission-Based Claim System](#permission-based-claim-system)
- [World Settings](#world-settings)
- [Visual Settings](#visual-settings)
- [Protection Settings](#protection-settings)
- [Role Settings](#role-settings)
- [Logging Settings](#logging-settings)
- [GUI Settings](#gui-settings)
- [Advanced Configuration](#advanced-configuration)

## Introduction

FrizzlenGaurd is highly configurable, allowing server administrators to customize nearly every aspect of the plugin's behavior. Configuration is done through YAML files located in the `plugins/FrizzlenGaurd` directory.

## Configuration Files

FrizzlenGaurd uses the following configuration files:

- **config.yml** - Main configuration file with all plugin settings
- **regions.yml** - Data storage for all regions (not meant to be manually edited)
- **messages.yml** - Customizable messages for all plugin communications

## General Settings

These settings control the plugin's core behavior:

```yaml
# General Settings
debug-mode: false                # Enable for detailed console logging
auto-save-interval: 5            # Minutes between auto-saves
auto-backup-interval: 60         # Minutes between auto-backups (0 to disable)
max-backups: 10                  # Maximum number of backups to keep
```

### Debug Mode

When `debug-mode` is enabled, the plugin will output detailed information to the console, which can be useful for troubleshooting issues.

### Auto-Save & Backup

- `auto-save-interval`: Controls how frequently region data is saved (in minutes)
- `auto-backup-interval`: Controls how frequently automatic backups are created (in minutes)
- `max-backups`: Limits the number of backup files to prevent excessive disk usage

## Economy Settings

These settings control the integration with Vault economy:

```yaml
# Economy Settings
economy:
  enabled: true                  # Will only work if Vault is installed
  claim-cost-per-block: 0.5      # Cost per block when claiming
  tax-enabled: false             # Whether to tax claims
  tax-interval: 1440             # Minutes between tax collections (24 hours)
  tax-cost-per-block: 0.01       # Cost per block for taxes
  use-vault: true                # Use Vault for economy integration
  currency-name: "Coins"         # Name of currency to display in messages
  currency-name-plural: "Coins"  # Plural name of currency
```

### Economy Integration

- `economy.enabled`: Enables or disables economy features
- `use-vault`: Set to true to use Vault for economy integration (recommended)
- `claim-cost-per-block`: Sets the cost per block when creating a claim
- `tax-enabled`: Enables a recurring tax on claims
- `tax-interval`: How often taxes are collected (in minutes)
- `tax-cost-per-block`: Cost per block for each tax collection
- `currency-name`: Name of the currency to use in messages
- `currency-name-plural`: Plural form of the currency name

## Claim Settings

These settings control claim creation and limitations:

```yaml
# Claim Settings
claims:
  use-permission-based-limits: true  # Use permission system for claim limits
  default-max-claims: 1              # Default number of claims a player can have
  default-claim-blocks: 250          # Default number of claim blocks for new players
  max-claim-size: 10000              # Maximum size of a claim in blocks
  min-claim-size: 25                 # Minimum size of a claim in blocks
  max-subregions-per-claim: 5        # Maximum number of subregions per claim
  blocks-accrued-per-hour: 0         # Claim blocks earned per hour of playtime (0 to disable)
```

### Claim Limits

- `use-permission-based-limits`: Set to true to use the permission system for claim limits
- `default-max-claims`: Default number of claims a player can have (1 by default)
- `default-claim-blocks`: Starting claim blocks for new players (250 by default)
- `max-claim-size`: Maximum volume (in blocks) of a single claim
- `min-claim-size`: Minimum volume (in blocks) of a single claim
- `max-subregions-per-claim`: Limits subregions within a main claim
- `blocks-accrued-per-hour`: Rate at which players earn additional claim blocks (0 to disable)

## Permission-Based Claim System

FrizzlenGaurd uses a permission-based system to control claim blocks and limits. This allows you to scale claim capabilities with player ranks:

```yaml
# Permission-Based Claim System
permission-claim-system:
  enabled: true                      # Enable permission-based claim limits
  override-default-values: true      # Override default values with permission-based values
  
  # Permission for maximum number of claims
  max-claims-permissions:
    "frizzlengaurd.claims.basic": 1
    "frizzlengaurd.claims.premium": 2
    "frizzlengaurd.claims.vip": 3
    "frizzlengaurd.claims.elite": 5
  
  # Permission for maximum claim blocks
  claim-blocks-permissions:
    "frizzlengaurd.blocks.basic": 250
    "frizzlengaurd.blocks.premium": 500
    "frizzlengaurd.blocks.vip": 1000
    "frizzlengaurd.blocks.elite": 2500
```

### Permission System Configuration

- `enabled`: Enables or disables the permission-based claim system
- `override-default-values`: If true, permission values override default values

### Permission-Based Claim Limits

These permissions control how many claims a player can have:

- `frizzlengaurd.claims.basic`: 1 claim (standard)
- `frizzlengaurd.claims.premium`: 2 claims
- `frizzlengaurd.claims.vip`: 3 claims
- `frizzlengaurd.claims.elite`: 5 claims

### Permission-Based Claim Blocks

These permissions control how many claim blocks a player has:

- `frizzlengaurd.blocks.basic`: 250 claim blocks (standard)
- `frizzlengaurd.blocks.premium`: 500 claim blocks
- `frizzlengaurd.blocks.vip`: 1000 claim blocks
- `frizzlengaurd.blocks.elite`: 2500 claim blocks

The plugin will automatically use the highest permission value a player has. For example, if a player has both `frizzlengaurd.blocks.basic` and `frizzlengaurd.blocks.premium`, they will receive 500 claim blocks.

## World Settings

These settings control which worlds the plugin operates in:

```yaml
# World Settings
worlds:
  excluded: []                   # Worlds where claiming is disabled
```

### World Exclusion

- `worlds.excluded`: List of world names where claiming is disabled
- Example: `excluded: ["creative", "minigames"]`

## Visual Settings

These settings control the visual effects for claims:

```yaml
# Visual Settings
visuals:
  enabled: true                  # Enable visual effects
  particle-type: "REDSTONE"      # Particle type for claim visualization
  particle-color: "RED"          # Color for claim particles
  particle-density: 2            # Particles per block (higher = more particles)
  show-on-entry: true            # Show particles when entering a claim
  show-on-stick-right-click: true # Show particles when right-clicking with a stick
```

### Particle Effects

- `enabled`: Turn all visual effects on/off
- `particle-type`: Type of particle to use (e.g., REDSTONE, FLAME, HEART)
- `particle-color`: Color for particles that support coloring
- `particle-density`: Controls how many particles are spawned per block

### Trigger Settings

- `show-on-entry`: Show claim boundaries when entering a claim
- `show-on-stick-right-click`: Show claim boundaries when right-clicking with a stick

## Protection Settings

These settings control the default protection flags for new claims:

```yaml
# Protection Settings
protection:
  default-flags:
    pvp: false
    mob-spawning: true
    mob-damage: false
    explosions: false
    fire-spread: false
    leaf-decay: true
    crop-trample: false
    piston-protection: true
    redstone: true
    # ... additional detailed flags
```

See the [Flag System](FLAGS.md) documentation for a complete list of all available flags.

## Role Settings

These settings control the permissions for each role:

```yaml
# Role Settings
roles:
  owner:
    color: "&c"
    can-build: true
    can-interact: true
    can-container: true
    can-teleport: true
    can-modify-flags: true
    can-invite: true
    can-kick: true
    can-create-subregion: true
  
  # ... additional roles
```

See the [Role System](ROLES.md) documentation for details on role configuration.

## Logging Settings

These settings control the logging features:

```yaml
# Logging Settings
logging:
  enabled: true
  log-block-changes: true
  log-player-interactions: true
  max-log-age-days: 30          # How long to keep logs (in days)
```

### Logging Controls

- `enabled`: Turn logging on/off
- `log-block-changes`: Record block placement and breaking
- `log-player-interactions`: Record player interactions with blocks
- `max-log-age-days`: Automatically remove old logs

## GUI Settings

These settings control the GUI menus:

```yaml
# GUI Settings
gui:
  enabled: true
  main-menu-rows: 4
  region-menu-rows: 5
```

### GUI Controls

- `enabled`: Turn the GUI interface on/off
- `main-menu-rows`: Number of rows in the main menu
- `region-menu-rows`: Number of rows in the region menu

## Advanced Configuration

### Custom Messages

You can customize all plugin messages in the `messages.yml` file:

```yaml
claim:
  claim-created: "&aSuccessfully created claim &f%name%&a."
  claim-deleted: "&aSuccessfully deleted claim &f%name%&a."
  claim-too-small: "&cClaim too small. Minimum size is %min_size% blocks."
  claim-too-large: "&cClaim too large. Maximum size is %max_size% blocks."
  claim-overlaps: "&cThis area overlaps with an existing claim."
  not-enough-blocks: "&cYou don't have enough claim blocks. Need %needed%, have %available%."
  too-many-claims: "&cYou have reached the maximum number of claims allowed (%max_claims%)."
```

### Performance Tuning

For large servers, you may want to adjust these settings:

```yaml
# Performance Settings
performance:
  async-chunk-loading: true      # Use async chunk loading for large operations
  cache-timeout: 300             # Seconds to cache region data in memory
  max-regions-per-chunk: 10      # Limit regions per chunk for performance
  scan-batch-size: 100           # Number of chunks to scan in a batch
```

These advanced settings help optimize performance on busy servers. 