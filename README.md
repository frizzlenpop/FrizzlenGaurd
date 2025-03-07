# FrizzlenGaurd

FrizzlenGaurd is a powerful and feature-rich land protection plugin for Minecraft servers. It allows players to claim and protect their land, manage permissions, and customize their protected regions.

## Features

- **Advanced Region Protection**
  - Cuboid region selection and claiming
  - Visual boundary markers and preview mode
  - Comprehensive permission system
  - Customizable flags for each region

- **Region Management**
  - Merge and resize regions
  - Member management with roles (Owner, Member)
  - Flag configuration through GUI
  - Region teleportation

- **Economy Integration**
  - Land rental system
  - Configurable prices and durations
  - Automatic rent collection and expiration

- **Administrative Tools**
  - Region scanning for issues
  - Backup and restore system
  - Permission-based claim block limits
  - Comprehensive logging system

- **Notification System**
  - Customizable alerts for region events
  - Multiple notification types (chat, title, sound)
  - Per-player notification settings

## Commands

### Player Commands

- `/fg claim [preview]` - Claim a new region
  - Use wooden axe to select corners
  - Optional preview mode to visualize selection

- `/fg merge <region1> <region2> [newName]` - Merge two adjacent regions
  - Regions must be owned by the same player or require admin permission
  - All permissions and settings are preserved

- `/fg resize <region> <direction> <amount>` - Resize a region
  - Directions: north, south, east, west, up, down
  - Amount in blocks to expand/shrink

- `/fg manage <region> <flags|members>` - Open management GUI
  - Flags: Configure region protection settings
  - Members: Add/remove members and set roles

- `/fg tp <region>` - Teleport to a region
  - Requires appropriate permissions
  - Finds safe location automatically

- `/fg rent <set|cancel|info> [region] [price] [duration]` - Manage region rentals
  - Set regions for rent with custom prices
  - View rental information
  - Requires Vault economy

- `/fg notify <toggle|settings> [type] [value]` - Configure notifications
  - Toggle all notifications
  - Customize notification types

### Admin Commands

- `/fg scan [type] [radius]` - Scan for problematic regions
  - Types: overlap, orphaned, empty, all
  - Configurable scan radius

- `/fg backup <create|restore|list> [name] [description]` - Manage backups
  - Create manual backups
  - Restore from previous backups
  - List available backups

- `/fg limit <set|info> <player> [limit]` - Manage claim limits
  - Set custom limits per player
  - View player's claim usage

## Permissions

- `frizzlengaurd.claim` - Allow claiming regions
- `frizzlengaurd.merge` - Allow merging regions
- `frizzlengaurd.resize` - Allow resizing regions
- `frizzlengaurd.manage` - Allow using management GUI
- `frizzlengaurd.teleport` - Allow teleporting to regions
- `frizzlengaurd.rent` - Allow using rental system
- `frizzlengaurd.notify` - Allow notification configuration
- `frizzlengaurd.admin` - Grant all administrative permissions
- `frizzlengaurd.admin.scan` - Allow scanning regions
- `frizzlengaurd.admin.backup` - Allow backup management
- `frizzlengaurd.admin.limit` - Allow managing claim limits

## Configuration

The plugin uses several configuration files:

- `config.yml` - Main configuration file
  - Economy settings
  - Claim limits
  - Notification settings
  - Backup configuration

- `regions.yml` - Region data storage
  - Region boundaries
  - Permissions
  - Flags
  - Member lists

## Dependencies

- Vault (optional) - Required for economy features
- WorldGuard (compatible) - Regions can coexist

## Installation

1. Place the plugin JAR in your server's `plugins` folder
2. Restart the server
3. Configure the plugin in `config.yml`
4. Set up permissions for your players

## Support

For support, please:
1. Check the documentation
2. Search existing issues
3. Create a new issue if needed

## License

This plugin is released under the MIT License. See the LICENSE file for details. 