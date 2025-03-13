# FrizzlenGaurd Role System

This document provides detailed information about the role system in [FrizzlenGaurd](README.md), explaining how permissions work within regions.

## Table of Contents

- [Introduction](#introduction)
- [Available Roles](#available-roles)
- [Role Permissions](#role-permissions)
- [Managing Roles](#managing-roles)
- [Role Commands](#role-commands)
- [Role Configuration](#role-configuration)

## Introduction

The role system in FrizzlenGaurd allows region owners to grant different levels of access to other players. Each role has specific permissions that determine what actions a player can perform within a region.

## Available Roles

FrizzlenGaurd comes with four pre-defined roles:

### Owner

The owner has full control over the region, including all build and management permissions. The player who creates a region is automatically assigned the owner role.

### Co-Owner

Co-owners have nearly all the same permissions as owners, including building and inviting others, but cannot delete the region.

### Trusted

Trusted players can build and interact with most blocks but cannot manage the region's settings or other members.

### Visitor

Visitors have limited access, primarily based on the region's flag settings. By default, they can only interact with certain blocks but not build or break blocks.

## Role Permissions

Each role has specific permissions that can be configured in the config.yml file:

| Permission | Description | Owner | Co-Owner | Trusted | Visitor |
|------------|-------------|:-----:|:--------:|:-------:|:-------:|
| `can-build` | Allow building and breaking blocks | ✓ | ✓ | ✓ | ✗ |
| `can-interact` | Allow interacting with blocks | ✓ | ✓ | ✓ | ✓ |
| `can-container` | Allow accessing containers | ✓ | ✓ | ✓ | ✗ |
| `can-teleport` | Allow teleporting to the region | ✓ | ✓ | ✓ | ✓ |
| `can-modify-flags` | Allow changing region flags | ✓ | ✓ | ✗ | ✗ |
| `can-invite` | Allow adding members | ✓ | ✓ | ✗ | ✗ |
| `can-kick` | Allow removing members | ✓ | ✓ | ✗ | ✗ |
| `can-create-subregion` | Allow creating subregions | ✓ | ✗ | ✗ | ✗ |

## Managing Roles

### Adding a Member with a Role

To add a player to your region with a specific role:

```
/fg addfriend <region> <player> <role>
```

For example:
```
/fg addfriend MyBase Alex trusted
```

If no role is specified, the player will be added with the "visitor" role by default.

### Changing a Member's Role

To change an existing member's role:

```
/fg setrole <region> <player> <role>
```

For example:
```
/fg setrole MyBase Alex co-owner
```

### Removing a Member

To remove a player from your region:

```
/fg removefriend <region> <player>
```

## Role Commands

| Command | Description |
|---------|-------------|
| `/fg addfriend <region> <player> [role]` | Add a player to your region with a specific role |
| `/fg removefriend <region> <player>` | Remove a player from your region |
| `/fg setrole <region> <player> <role>` | Change a player's role in your region |
| `/fg listmembers <region>` | List all members of a region with their roles |

## Role Configuration

Administrators can customize the permissions for each role in the `config.yml` file:

```yaml
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
  
  co-owner:
    color: "&6"
    can-build: true
    can-interact: true
    can-container: true
    can-teleport: true
    can-modify-flags: true
    can-invite: true
    can-kick: true
    can-create-subregion: false
  
  trusted:
    color: "&a"
    can-build: true
    can-interact: true
    can-container: true
    can-teleport: true
    can-modify-flags: false
    can-invite: false
    can-kick: false
    can-create-subregion: false
  
  visitor:
    color: "&7"
    can-build: false
    can-interact: true
    can-container: false
    can-teleport: true
    can-modify-flags: false
    can-invite: false
    can-kick: false
    can-create-subregion: false
```

### Custom Roles

Administrators can also create custom roles by adding new sections to the roles configuration. For example:

```yaml
roles:
  # ... existing roles ...
  
  builder:
    color: "&b"
    can-build: true
    can-interact: true
    can-container: true
    can-teleport: true
    can-modify-flags: false
    can-invite: false
    can-kick: false
    can-create-subregion: false
```

This would create a new "builder" role that can build but not manage members or flags. 