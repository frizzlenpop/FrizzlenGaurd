# FrizzlenGaurd

FrizzlenGaurd is a powerful and flexible land protection plugin for Paper Minecraft servers, offering advanced region management with granular permissions, visual feedback, and extensive customization options.

## Features

### Core Protection
- **Advanced Land Claiming**: Create and manage protected regions using cuboid selections
- **Multi-tier Permissions**: Granular control with OWNER, CO_OWNER, TRUSTED, and VISITOR roles
- **Subregions Support**: Create nested regions with inheritance or override settings
- **Flag System**: Customize region behavior (PvP, mob spawning, explosions, etc.)
- **Multi-world Support**: Protect regions across different worlds with world exclusion options

### Management Features
- **Visual Feedback**: Particle effects for region boundaries and selection preview
- **GUI Interface**: User-friendly menus for region management
- **Logging System**: Track changes and modifications within regions
- **Economy Integration**: Ready for integration with economy plugins

## Commands

### Player Commands
/fg claim  - Start a claim session /fg subclaim
- Create a subregion /fg addfriend   
  [role] - Add member to region /fg removefriend
- Remove member /fg setrole
- Change member's role /fg regioninfo  - Display region details /fg setflag    - Toggle region flag /fg listregions - List accessible regions /fg gui - Open management GUI /fg logs  - View region logs
### Admin Commands
/fg delregion  - Force delete region 
/fg backup - Create data backup 
/fg reload - Reload configuration 
/fg scan - Check for issues 
/fg merge   - Merge regions 
/fg resize  - Modify region size 
/fg setexclude  - Exclude world from claims 
/fg removeexclude  - Remove world exclusion 
/fg listexclude - List excluded worlds
## Permissions

### Basic Permissions
- `frizzlengaurd.claim` - Allow claiming regions
- `frizzlengaurd.manage` - Manage owned regions
- `frizzlengaurd.friend` - Manage region members
- `frizzlengaurd.flags` - Set region flags

### Admin Permissions
- `frizzlengaurd.admin.delete` - Delete any region
- `frizzlengaurd.admin.modify` - Modify any region
- `frizzlengaurd.admin.reload` - Reload plugin
- `frizzlengaurd.admin.backup` - Create backups
- `frizzlengaurd.admin.world` - Manage world exclusions

## Installation

1. Download the latest release from the releases page
2. Place the JAR file in your server's `plugins` folder
3. Restart your server
4. Configure the plugin in `plugins/FrizzlenGaurd/config.yml`

## Configuration

The plugin creates the following configuration files:
- `config.yml` - Main configuration
- `regions.yml` - Region data storage
- `messages.yml` - Customizable messages

### Example Configuration
```yaml
# Default values for new regions
defaults:
  max-members: 10
  default-flags:
    pvp: false
    mob-spawning: true
    explosions: false

# Visual settings
visualization:
  enabled: true
  particle-type: "BARRIER"
  update-interval: 20

# World settings
worlds:
  excluded:
    - "excluded_world"
```

## Requirements

- Paper 1.21.x
- Java 17 or higher
- Recommended: Vault (for economy features)

## Support

If you encounter any issues or need assistance:
1. Check the [Wiki](https://github.com/your-username/FrizzlenGaurd/wiki)
2. Open an [Issue](https://github.com/your-username/FrizzlenGaurd/issues)
3. Join our Discord community (link)

## Building from Source

```bash
git clone https://github.com/your-username/FrizzlenGaurd.git
cd FrizzlenGaurd
mvn clean package
```

The built jar will be in the `target` directory.

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Thanks to the Paper team for their amazing server software
- Community members who provided feedback and testing
- All contributors who helped improve the plugin

---
Made with ❤️ for the Minecraft community
