# Just Kitting IntelliJ plugin

[![Version](https://img.shields.io/jetbrains/plugin/v/21139-just-kitting.svg)](https://plugins.jetbrains.com/plugin/21139-just-kitting)
![Build](https://github.com/picimako/mockitools/workflows/Build/badge.svg)

<!-- Plugin description -->
A plugin for JetBrains IDE plugin developers to provide them with extra functionality during plugin development, besides the official DevKit plugin.

It provides functionality that wouldn't/couldn't necessarily be a part of the original DevKit plugin,
but can still be useful to improve the plugin development process. They are in the following feature areas:

- [Light Services](docs/services.md)
- [Caching](docs/caching.md)
- [CallMatcher](docs/call_matcher.md)
- [PersistentStateComponents](docs/persistent_state_components.md)
- [Inspections](docs/inspections.md)
- [Plugin Configuration](docs/plugin_configuration.md)
- [Miscellaneous](docs/misc.md)
<!-- Plugin description end -->

## Disclaimer

The features in this plugin are based on the IntelliJ Platform SDK documentation, and personal experience, and what I personally find useful.
There are many subtleties to using DevKit and the IntelliJ Platform that are not covered here, and I don't know of.

Please use your best judgement when using these features to make sure they actually suit your specific needs.

## Contributions

If you'd like to contribute, first please check whether the functionality you are implementing would be a better fit for the
official DevKit plugin, and whether there is already a [JetBrains YouTrack ticket](https://youtrack.jetbrains.com/issues?q=Subsystem:%20%7BPlugin%20Development%20(DevKit)%7D) for that.
This is to minimize the chance of clashing with the roadmap and development of the DevKit plugin.

You can find the developer documentation [here](/docs/dev_docs.md).

## Kotlin support

Most of the functionality in this plugin is implemented for Java code, with some also supporting Kotlin as well.

## License

This project is licensed under the terms of Apache Licence Version 2.0.

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
