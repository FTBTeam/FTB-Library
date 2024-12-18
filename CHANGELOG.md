# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
