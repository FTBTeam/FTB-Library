# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2101.1.20]

### Added
* Added abstraction layer API for currency mods
  * Does nothing on its own; intended to be set up by FTB XMod Compat

## [2101.1.19]

### Added
* Added filtering to `ResourceConfigValue`, allowing extra custom filtering in resource selection screens
  * E.g. have the itemstack selection screen only allow blocks to be selected

### Fixed
* Make the `/ftblibrary day` and `night` commands act more like their vanilla counterparts
  * Better cross-mod compat

## [2101.1.18]

### Added
* Added `TextField#reflow` method (recalculate width/height/linecount after changing the text)

### Fixed
* Fixed pressing the "E" (inventory) key in some popup textfields causing the current screen to close
* The `Theme.drawHorizontalTab()` method now works again

## [2101.1.17]

### Added
* Added `pt_br` translation (thanks @Xlr11)

### Fixed
* Fix a client crash triggered by certain empty textures

## [2101.1.16]

### Added
* Added `SidebarButtonVisibility` packet to allow dynamic server-side control of sidebar button visibility

## [2101.1.15]

### Added
* Added support for an "environment_condition" field to sidebar JSON files
  * If non-empty, allows for control of sidebar button visibility via presence or absence of an environment variable
  * Use a "!" prefix to require the environment variable to _not_ be present

## [2101.1.14]

### Added
* Better search in resource selection GUIs
  * Can now use a space-separated list of terms, all of which must match
  * e.g. `@minecraft log` matches only vanilla logs

## [2101.1.13]

### Added
* Added new `EditConfigChoicePacket` network packet to support mods offering client & server config editing buttons/commands

## [2101.1.12]

### Added
* Config screens (including FTB Quests property editing) now have cleaner tooltip handling
  * Instead of every line showing tooltip info, each line now has a "i" info button on the left to show the tooltip
  * Reduces the distracting of big tooltips popping up and down when the mouse moves over a config screen

### Changed
* Added `ja_jp` translations (thanks @twister716)
* Updated `ru_ru` translations (thanks @BazZziliuS)

## [2101.1.11]

### Added
* Added the concept of startup configs to the new `ConfigManager` system
  * Startup configs are loaded at mod construction time, not sync'd and not intended for in-game editing

### Fixed
* Load client config a little earlier to avoid possible resource reloading accessing not-yet-loaded configs

## [2101.1.10]

### Added
* Added a new config system implementation. **IMPORTANT CHANGE** for modpack developers and server admins!
  * Existing system still works but is considered deprecated. So far, only FTB Library client config uses the new system
    * Other FTB mods will be migrated to the new system soon and similar notes will be added to their changelogs
  * Default configs are no longer loaded from `defaultconfigs/` - this folder is now **ignored** by FTB Library
  * Configs should be distributed by modpacks in the `config/` folder
  * Config overrides are also checked for in `local/` (client configs) and `<world>/serverconfig/` (server configs) and loaded from there in preference; server admins can use this to have custom local configuration if desired

### Fixed
* Fixed several atlas texture sizes which weren't 16x16

## [2101.1.9]

* Added a `Icon#aspectRatio()` method, which the image width / height
  * Returns 1 for most icon types, but image and atlas sprite icons may have a different aspect ratio
  * Logical image size is used, so animated textures will return the correct ratio

## [2101.1.8]

### Added
* Added a `Icon#getPixelBufferFrameCount()` method
  * Returns the number of animation frames in an atlas sprite icon, as controlled by .mcmeta file
  * Always returns 1 for non-atlas-sprite icons (including `AnimationIcon`, which is just a list of `Icon` !)

## [2101.1.7]

## Fixed
* Fixed some icons not rendering correctly 
  * Specifically, "empty" icons were rendering as white squares instead of falling back to an appropriate default in FTB Quests

## [2101.1.6]

### Fixed
* Fixed cross-mod compat issue causing crashes under some situations when players join

## [2101.1.5]

### Changed
* Overhauled and cleaned up many icon textures

### Fixed
* Fixed a client crash with certain inputs to IntTextBox (as used in FTB Chunks waypoint editing)

## [2101.1.4]

### Added
* Added a dropdown menu widget
* Exposed some more methods in `EditStringConfigOverlay`

### Changed
* Replaced `ContextMenu.CButton` with `ContextButton`

### Fixed
* Fixed `allowEditMode` in `TextBox` not applying in all situations
* Fixed items/fluids/images in respective resource search screens being jammed together too closely

## [2101.1.3]

### Fixed
* Fixed an issue in config loading/saving of certain data types (affects FTB Chunks minimap info settings)
* Added tr_tr translations (thanks @RuyaSavascisi)

## [2101.1.2]

### Fixed
* Fixed `/ftblibrary clientconfig` command not being usable without op perms

## [2101.1.1]

### Fixed
* Fixed a couple of minor GUI drawing artifacts in some screens

## [2101.1.0]

### Changed
* Updated to MC 1.21.1

### Added
* Sidebar buttons (from FTB Library and all mods which add buttons) are now all repositionable and toggleable
  * New sidebar button to open client config for FTB Library (can be used to hide sidebar entirely)
  * Client config can also be opened via `/ftblibrary clientconfig` command
