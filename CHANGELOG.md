<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Changelog

## [Unreleased]

## [0.3.0]
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
