# RideOnHead
**A lightweight plugin that lets players sit on each other's heads, build player towers, and manage ride permissions.**
---

## 🎯 Core Mechanics
- **Right-click** a player with an empty hand to mount their head  
- **Sneak** to eject all passengers  
- **Stack-climb** – hop onto the top of any player stack (configurable)

## ⚙️ Features
- **Personal toggle** – `/ride toggle` lets any player instantly allow or forbid riding on themselves  
- **Blacklist system** – `/ride blacklist <player>` adds/removes someone from your blacklist; works with offline players  
- **Admin controls** – manage other players' settings, view their blacklists, and reload the plugin via `/ride reload`  
- **Storage flexibility** – NBT (online only), YAML files, or H2 database with Caffeine caching  
- **Optional sounds** – custom mount/dismount sounds configurable in `config.yml` (set to `"none"` to disable)  
- **MiniMessage support** – all messages use Adventure API; test your formatting at [https://webui.advntr.dev/](https://webui.advntr.dev/)  
- **Message disabling** – leave any message key empty in a language file to prevent it from appearing

## 🌍 Localization
- **Automatic player language detection** (uses the client's locale)  
- Each player sees messages in their own language if a translation exists  
- Fallback to the configured default language when no match is found  
- **Built-in translations** for:
  - English (`en`)
  - Russian (`ru`)
  - Ukrainian (`uk`)
  - German (`de`)
  - French (`fr`)
  - Spanish (`es`)
  - Chinese Simplified (`zh`)
- Easy to add or customise: just drop a `.yml` file into `plugins/RideOnHead/lang/`

## 📋 Commands & Permissions
| Command | Description | Permission |
|---------|-------------|------------|
| `/ride toggle` | Switch riding permission on yourself | `rideonhead.user` |
| `/ride blacklist` | Show your blacklist | `rideonhead.user` |
| `/ride blacklist <player>` | Add / remove a player from your blacklist | `rideonhead.user` |
| `/ride toggle <player>` | **(Admin)** Force toggle for another player | `rideonhead.admin` |
| `/ride blacklist <owner> <target>` | **(Admin)** Manage someone else's blacklist | `rideonhead.admin` |
| `/ride reload` | **(Admin)** Reload config + language files | `rideonhead.admin` |

## 🔧 Configuration
Detailed comments inside `config.yml` will guide you through all settings, including storage, sounds, language, and stack-climb behaviour.

## 👀 Preview
![Preview](https://i.imgur.com/w6yuMNu.gif)
---
**Issue tracker:** [GitHub Issues](https://github.com/DeelTer/RideOnHead/issues)  
