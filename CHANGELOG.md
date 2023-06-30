<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Changelog

## [Unreleased]

## [0.4.0]
### Added
- [#22](https://github.com/picimako/just-kitting/issues/22): Added code folding for `extensions.localInspection` tags in plugin descriptor files.

### Changed
- Added `qodana.yml` to the list diffable plugin configuration files.
- Improved the inlay hint logic to find and classify light services in Kotlin classes, including nested classes.
- [#2](https://github.com/picimako/just-kitting/issues/2): Added Kotlin file support for generating a `getInstance()` function in companion objects for services, components, etc.
- [#2](https://github.com/picimako/just-kitting/issues/2): Added support for converting a Kotlin class to a `PersistentStateComponent`.
- **CONF:** Updated plugin configuration to match version 1.7.0 of the platform plugin template.
- **CONF:** The plugin is now built with JDK 17.

### Removed
- Removed support for IJ 2022.1.
- Removed some deprecated and internal API usage.

## [0.3.0]
### Added
- Added support for IJ 2023.2.
- Kotlin class light services are now listed too in the light services inlay hint in `plugin.xml`.
- Added an action that opens a diff view of certain plugin configuration files, and compares them with their versions
in the IntelliJ Platform Plugin template on GitHub. It opens a two-sided diff view with the local and remote versions of the file.

### Changed
- Updated the inspection description code snippets to enable syntax highlight in them starting from IJ 2023.2.

## [0.2.0]
### Changed
- Added missing `@Override` annotation to `getState()` and `loadState()` methods when converting a class to `PersistentStateComponent`.
- Relaxed the conditions in what the service `getInstance()` generation action is available.
It is now available in interfaces, abstract classes and non-light-service classes as well, and users can also choose the service level.
- [#8](https://github.com/picimako/just-kitting/issues/8): `CallMatcher` initializers can now be generated when invoked on method calls as well.

## [0.1.0]
### Added
- Added project files.
- [#7](https://github.com/picimako/just-kitting/issues/7): Added an intention on Java class methods to generate `CallMatcher`
    initializer calls from them.
- Initial scaffold created from [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)
