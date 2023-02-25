# Just Kitting IntelliJ plugin

![](https://img.shields.io/badge/since-IJ2022.1-blue) ![](https://img.shields.io/badge/until-IJ2022.3-blue)

<!-- Plugin description -->
A plugin for JetBrains IDE plugin developers to provide them with extra functionality during plugin development, besides the official DevKit plugin.
<!-- Plugin description end -->

## Disclaimer

The features in this plugin are based on the IntelliJ Platform SDK documentation, and personal experience, and what I personally find useful.
There are many subtleties to using DevKit and the IntelliJ Platform, that are not covered here, and I don't know of.

Please use your best judgement when using these features to make sure they actually suit your specific needs.

## Contributions

If you'd like to contribute, first please check whether the functionality you are implementing would be a better fit for the
official DevKit plugin, and whether there is already a [JetBrains YouTrack ticket](https://youtrack.jetbrains.com/issues?q=Subsystem:%20%7BPlugin%20Development%20(DevKit)%7D) for that.
This is to minimize the chance of clashing with the development of the DevKit plugin.

## Documentation

- [Light Services](docs/light_services.md)
- [Caching](docs/caching.md)
- [CallMatcher](docs/call_matcher.md)
- [PersistentStateComponents](docs/persistent_state_components.md)
- [Inspections](docs/inspections.md)
- [Miscellaneous](docs/misc.md)

- [Developer docs](/docs/dev_docs.md)

## Kotlin support

As of now, all functionality in this plugin is implemented only for Java code, but at least some Kotlin support is planned in the long run.

## License

This project is licensed under the terms of Apache Licence Version 2.0.

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
