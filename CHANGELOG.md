<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Changelog

## [Unreleased]

## [1.1.1]
### Fixed
- Fixed the K2 compiler compatibility configuration.

## [1.1.0]
### Added
- Added `.gitignore` to the list of files that can be compared with their intellij-platform-plugin-template versions.

### Changed
- New supported IDE version range: 2024.2.1 - 2024.3.*.
- Updated the project to use the IntelliJ Platform Gradle Plugin 2.0.
- Updated the project to use JDK 21.
- Updated project configuration to make sure the plugin works when the K2 Kotlin compiler is enabled.

### Fixed
- Fixed the **Compare with Platform Plugin Template Version** action to work properly when the opened project
is actually a workspace by the Multi-Project Workspace plugin.
- Fixed the **Update Gradle Version** line marker to work with workspaces.

## [1.0.0]
### Added
- [30](https://github.com/picimako/just-kitting/issues/30): Added a line marker that displays the referenced icons in the `actions.action@icon` and `actions.group.action@icon`
XML attributes in plugin descriptor files.
- [28](https://github.com/picimako/just-kitting/issues/28): Added a line marker that can signal mismatching gradle version in `gradle.properties` and `gradle-wrapper.properties`
and can update the wrapper based on the new version in `gradle.properties`.

## [0.9.0]
### Changed
- Supported IDE version range: 2023.3-2024.2-EAP.
- Plugin configuration and dependency updates.
- Deleted the inspection options panel generation action because there is a newer API for that that provides
an easier way to create the panel.

## [0.8.1]
### Fixed
- Added a missing `getActionUpdateThread()` method override to avoid throwing exception.

## [0.8.0]
### Changed
- Supported IDE version range: 2023.1.5-2024.1-EAP.
- Intention action sub-category names are now displayed in `<intentionAction>` tag code folding placeholder texts.
- Plugin configuration updates.

## [0.7.0]
### Changed
- Supported IDE version range: 2022.3-2023.3
- Plugin configuration updates.

## [0.6.0]
### Added
- [#29](https://github.com/picimako/just-kitting/issues/29): Added code folding for `extensions.intentionAction` tags in plugin descriptor files.

### Changed
- [#29](https://github.com/picimako/just-kitting/issues/29): Code folding of `extensions.localInspection` and `extensions.globalInspection` tags
is extended with resource bundle message resolution with fallback logic based on where bundle names are specified in the EP or in the plugin descriptor file.
- The light service class names are now sorted alphabetically in the **View all light services** inlay hint list popup.

### Fixed
- Fixed the issue that the **View all light services** inlay hint list popup didn't appear because it didn't handle rendering of KtClasses.

## [0.5.0]
### Added
- [#29](https://github.com/picimako/just-kitting/issues/29): Added code folding for `extensions.globalInspection` tags in plugin descriptor files.

### Changed
- The check for non-existent method for `CallMatcher`s now works also when the class name is specified as a constant.

### Fixed
- Fixed an NPE that occurred when looking up the `com.intellij.openapi.components.Service` class for showing the light services inlay hint.

## [0.4.0]
### Added
- [#22](https://github.com/picimako/just-kitting/issues/22): Added code folding for `extensions.localInspection` tags in plugin descriptor files.

### Changed
- Added `qodana.yml` to the list of diffable plugin configuration files.
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
