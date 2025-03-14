# FrizzlenGaurd Admin Guide

This document provides detailed information for server administrators about the administrative features in [FrizzlenGaurd](README.md).

## Table of Contents

- [Admin Claims](#admin-claims)
- [Region Management](#region-management)
- [Managing Claim Permissions](#managing-claim-permissions)
- [Backup System](#backup-system)
- [Administrative Commands](#administrative-commands)
- [Permissions](#permissions)
- [Configuration](#configuration)

## Admin Claims

Admin claims are special regions that bypass normal restrictions like claim block limits, size restrictions, and overlapping checks. They're perfect for server spawns, event areas, and other server-controlled regions.

### Creating an Admin Claim

1. Select two positions using the standard selection method
   - Get a selection tool: `/fg claim`
   - Left-click to set position 1
   - Right-click to set position 2
2. Create the admin claim with: `/fg adminclaim <name>`

### Examples

```
/fg adminclaim spawn
/fg adminclaim event-arena
/fg adminclaim shopping-district
```

### Benefits over Regular Claims

- No claim block requirements
- No size restrictions (min/max)
- Can overlap with existing claims
- No economy costs
- Automatically saved and loaded with server restarts

## Region Management

### Deleting Regions

Admins can force-delete any region with:
```
/fg delregion <region>
```

### Merging Regions

Combine two regions into one:
```
/fg merge <target> <source>
```
The source region will be removed, and its area added to the target region.

### Resizing Regions

Modify a region's boundaries:
```
/fg resize <region>
```
Follow the on-screen instructions to select new boundaries.

### Scanning for Issues

Check for potential issues in the region database:
```
/fg scan
```
This will report problems like overlapping regions, invalid data, or other inconsistencies.

## Managing Claim Permissions

FrizzlenGaurd uses a permission-based system for controlling claim limits and blocks. This allows you to scale players' claiming capabilities based on their rank or status on your server.

### Default Claim Limits

By default, players with basic permissions receive:
- 250 claim blocks (`frizzlengaurd.blocks.basic`)
- 1 maximum claim (`frizzlengaurd.claims.basic`)

### Permission Structure

The permission system is structured in two parts:

#### 1. Claim Block Permissions
- `frizzlengaurd.blocks.basic` - 250 blocks
- `frizzlengaurd.blocks.premium` - 500 blocks
- `frizzlengaurd.blocks.vip` - 1000 blocks
- `frizzlengaurd.blocks.elite` - 2500 blocks

#### 2. Claim Count Permissions
- `frizzlengaurd.claims.basic` - 1 claim
- `frizzlengaurd.claims.premium` - 2 claims
- `frizzlengaurd.claims.vip` - 3 claims
- `frizzlengaurd.claims.elite` - 5 claims

### Setting Up Permissions

To implement the permission-based claim system:

1. Make sure `use-permission-based-limits` is set to `true` in the configuration
2. Configure your permission plugin to assign the appropriate permissions to different ranks
3. The plugin will automatically apply the highest permission a player has for each category

Example permission configuration for LuckPerms:

```
/lp group default permission set frizzlengaurd.blocks.basic true
/lp group premium permission set frizzlengaurd.blocks.premium true
/lp group vip permission set frizzlengaurd.blocks.vip true
/lp group elite permission set frizzlengaurd.blocks.elite true

/lp group default permission set frizzlengaurd.claims.basic true
/lp group premium permission set frizzlengaurd.claims.premium true
/lp group vip permission set frizzlengaurd.claims.vip true
/lp group elite permission set frizzlengaurd.claims.elite true
```

### Checking Player Claim Blocks

As an administrator, you can check how many claim blocks a player has:

```
/fg blocks <player>
```

### Vault Integration

FrizzlenGaurd is fully integrated with Vault for economy features. If you have Vault and an economy plugin installed, players can:

- Pay for claims based on size
- Pay taxes on claims (if enabled)
- Buy additional claim blocks (if configured)

To set up the Vault integration:

1. Install Vault and a compatible economy plugin
2. Set `economy.enabled` and `economy.use-vault` to `true` in the configuration
3. Configure the economy settings to match your server's economy

## Backup System

FrizzlenGaurd includes an automatic backup system that periodically saves region data.

### Creating Backups

Create a manual backup:
```
/fg backup create
```

### Viewing Backups

List available backups:
```
/fg backup list
```

### Restoring Backups

Restore a specific backup:
```
/fg backup restore <timestamp>
```

## Administrative Commands

| Command | Description |
|---------|-------------|
| `/fg adminclaim <name>` | Create an admin claim |
| `/fg delregion <region>` | Force delete a region |
| `/fg backup create` | Create a backup |
| `/fg backup list` | List available backups |
| `/fg backup restore <timestamp>` | Restore a backup |
| `/fg scan` | Scan for region issues |
| `/fg reload` | Reload plugin configuration |
| `/fg merge <target> <source>` | Merge two regions |
| `/fg resize <region>` | Resize a region |
| `/fg blocks <player>` | Check a player's claim blocks |

## Permissions

| Permission | Description |
|------------|-------------|
| `frizzlengaurd.admin.claim` | Allow creating admin claims |
| `frizzlengaurd.admin.delete` | Allow deleting any region |
| `frizzlengaurd.admin.backup` | Allow using the backup system |
| `frizzlengaurd.admin.scan` | Allow scanning for region issues |
| `frizzlengaurd.admin.reload` | Allow reloading the plugin |
| `frizzlengaurd.admin.bypass` | Bypass all region restrictions |
| `frizzlengaurd.admin.merge` | Allow merging any regions |
| `frizzlengaurd.admin.resize` | Allow resizing any regions |
| `frizzlengaurd.admin.blocks` | Allow checking other players' blocks |
| `frizzlengaurd.admin.*` | Grant all admin permissions |

## Configuration

Admins can customize various plugin settings in the `config.yml` file. Here are some important admin-specific options:

### Debug Mode

```yaml
debug-mode: false
```
When enabled, additional debugging information will be logged to the console.

### Auto-Save and Backup

```yaml
auto-save-interval: 5 # Minutes
auto-backup-interval: 60 # Minutes
max-backups: 10 # Number of backups to keep
```

### Claim Restrictions

```yaml
claims:
  use-permission-based-limits: true
  default-max-claims: 1
  default-claim-blocks: 250
  max-claim-size: 10000
  min-claim-size: 25
  max-subregions-per-claim: 5
  blocks-accrued-per-hour: 0
```

### Permission-Based Claim System

```yaml
permission-claim-system:
  enabled: true
  override-default-values: true
  
  max-claims-permissions:
    "frizzlengaurd.claims.basic": 1
    "frizzlengaurd.claims.premium": 2
    "frizzlengaurd.claims.vip": 3
    "frizzlengaurd.claims.elite": 5
  
  claim-blocks-permissions:
    "frizzlengaurd.blocks.basic": 250
    "frizzlengaurd.blocks.premium": 500
    "frizzlengaurd.blocks.vip": 1000
    "frizzlengaurd.blocks.elite": 2500
```

### World Settings

```yaml
worlds:
  excluded: [] # Worlds where claiming is disabled
```

### Default Flag Settings

```yaml
protection:
  default-flags:
    pvp: false
    mob-spawning: true
    # ... other flags
```

### Role Permissions

```yaml
roles:
  owner:
    can-build: true
    # ... other permissions
  co-owner:
    can-build: true
    # ... other permissions
  trusted:
    can-build: true
    # ... other permissions
  visitor:
    can-build: false
    # ... other permissions
``` 