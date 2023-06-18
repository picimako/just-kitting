# Plugin Configuration

## Configuration file diffs with the IntelliJ Platform Plugin Template

![](https://img.shields.io/badge/diffview-orange) ![](https://img.shields.io/badge/since-0.3.0-blue) [![](https://img.shields.io/badge/implementation-CompareConfigFileWithPluginTemplateAction-blue)](../src/main/java/com/picimako/justkitting/action/diff/CompareConfigFileWithPluginTemplateAction.java)

This action is registered in editor and Project View context menus of certain plugin configuration files,
and it opens a two-sided diff view comparing the local version of the file with its IntelliJ Platform Plugin Template version on GitHub.

| Project view context menu                                                                                                    | Editor context menu                                                                                              |
|------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------|
| ![compare_with_template_project_view_context_menu_action](assets/compare_with_template_project_view_context_menu_action.png) | ![compare_with_template_editor_context_menu_action](assets/compare_with_template_editor_context_menu_action.png) |


The action is available for the following configuration files:
- `build.gradle.kts`
- `gradle.properties`
- `.github/dependabot.yml`
- `.github/workflows/build.yml`
- `.github/workflows/release.yml`
- `.github/workflows/run-ui-tests.yml`
- `gradle/libs.versions.toml`

The contents from GitHub are downloaded from the `main` branch of the template repository, directly from *https://raw.githubusercontent.com/JetBrains/intellij-platform-plugin-template/main/...*

![compare_with_template_diff_view](assets/compare_with_template_diff_view.png)

If the remote contents cannot be downloaded, a balloon is displayed, and a log entry is logged with the reason of it.

![compare_with_template_error_balloon](assets/compare_with_template_error_balloon.png)
