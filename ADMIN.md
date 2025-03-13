# FrizzlenGaurd Admin Guide

This document provides detailed information for server administrators about the administrative features in [FrizzlenGaurd](README.md).

## Table of Contents

- [Admin Claims](#admin-claims)
- [Region Management](#region-management)
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
  max-claims-per-player: 3
  max-claim-size: 10000
  min-claim-size: 25
  max-subregions-per-claim: 5
  default-claim-blocks: 1000
  blocks-accrued-per-hour: 100
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