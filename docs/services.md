# Services

<!-- TOC -->
* [Generate service static getInstance() method](#generate-service-static-getinstance---method)
* [Light Services visualized in plugin.xml](#light-services-visualized-in-pluginxml)
<!-- TOC -->

Official Plugin SDK documentation: [Light Services](https://plugins.jetbrains.com/docs/intellij/plugin-services.html#light-services)

Related JetBrains YT tickets:
- [Devkit: Light Services inspection](https://youtrack.jetbrains.com/issue/IDEA-236032/Devkit-Light-Services-inspection)
- [Class, that registered as extension, must be not registered as service or component](https://youtrack.jetbrains.com/issue/IDEA-294395/Class-that-registered-as-extension-must-be-not-registered-as-service-or-component)

## Generate service static getInstance() method

![](https://img.shields.io/badge/action-orange) ![](https://img.shields.io/badge/since-0.1.0-blue) [![](https://img.shields.io/badge/implementation-GenerateLightServiceStaticGetInstanceAction-blue)](../src/main/java/com/picimako/justkitting/action/GenerateLightServiceStaticGetInstanceAction.java)

It is common practice, and is mentioned by the [Retrieving a Service](https://plugins.jetbrains.com/docs/intellij/plugin-services.html#retrieving-a-service)
section as well, that a static `getInstance()` convenience method may be added to (light) service classes to wrap service retrieval logic.

This action is available from the Alt+Insert **Generate** menu when the following conditions are met:
- the class is not anonymous
- the class is not an enum
- the class doesn't have any static `getInstance()` method defined

The generation logic happens as follows:
- If `Service.Level#PROJECT` is specified, then project-level getter is generated.
- If `Service.Level#APP` is specified, then application-level getter is generated.
- If no `Service.Level` is specified but the class name ends with
  - _ProjectService_, _ProjectSettings_, or _ProjectState_, then project-level getter is generated,
  - _ApplicationService_, _ApplicationSettings_ or _ApplicationState_, then application-level getter is generated.

In every other case, users can choose from a popup list the service level to generated the method for.
    
  <details>
      <summary><strong>Project-level example...</strong></summary>

  **From:**
  ```java
  @Service(Service.Level.PROJECT)
  public final class SomeService {
  }
  ```
  
  **To:**
  ```java
  @Service(Service.Level.PROJECT)
  public final class SomeService {
    public static SomeService getInstance(Project project) {
        return project.getService(SomeService.class);
    }
  }
  ```
  </details>

  <details>
      <summary><strong>Application-level example...</strong></summary>

  **From:**
  ```java
  @Service(Service.Level.APP)
  public final class SomeService {
  }
  ```
  
  **To:**
  ```java
  @Service(Service.Level.APP)
  public final class SomeService {
    public static SomeService getInstance() {
        return ApplicationManager.getApplication().getService(SomeService.class);
    }
  }
  ```
  </details>

![application_level_get_instance](assets/application_level_get_instance.gif)

## Light Services visualized in plugin.xml

![](https://img.shields.io/badge/inlayhint-orange) ![](https://img.shields.io/badge/since-0.1.0-blue) [![](https://img.shields.io/badge/implementation-LightServicesInlayHintsProvider-blue)](../src/main/java/com/picimako/justkitting/inlayhint/LightServicesInlayHintsProvider.kt)

According to the [Light Services](https://plugins.jetbrains.com/docs/intellij/plugin-services.html#light-services) Plugin SDK docs:
> A service not going to be overridden does not need to be registered in plugin.xml... Instead, annotate service class with @Service.

Thus, light services may or may not be visible in the plugin.xml unlike the rest of the plugin components.
This inlay hint aims to improve this situation by adding inlay hints to the starting tag of the `<extensions>` tag.

There are the following two display modes:

### List of light services

In this mode, it shows the names of light service classes in the project - grouped by the service level, and limited to a user-defined max number of items.

Upon clicking on a class name, it navigates to the corresponding class definition.

![light_services_inlay_hint_list_without_view_all](assets/light_services_inlay_hint_list_without_view_all.PNG)

The number of displayed class items can be configured in <kbd>Settings</kbd> > <kbd>Editor</kbd> > <kbd>Inlay Hints</kbd> > <kbd>XML</kbd> > <kbd>Plugin light services</kbd>,
and allows a value between 1 and 50. Although displaying too many hints can be counter-productive, 50 as the max threshold provides enough flexibility for those who might need it
(and who might wanna feel like a badass).

Note: the settings values are stored on application-level based on IntelliJ's default behaviour.

If the number of light services in the project is greater than the max number of items to display, an additional *View all light services...* hint is also added.
Clicking on it, a popup list of all light service classes is displayed.

![light_services_inlay_hint_list_with_view_all](assets/light_services_inlay_hint_list_with_view_all.PNG)

In this mode, hints are displayed for light service classes regardless of they are registered in `plugin.xml` or not.

(The examples are from one of my other plugins called *Gherkin Overview*).

### View all

This mode shows only the *View all light services...* hint, which behaves the same as the hint with the same name in the previous display mode.

![light_services_inlay_hint_view_all_only](assets/light_services_inlay_hint_view_all_only.PNG)

### Notes

As for the popup list (see [LightServicesHintPresentationAware](../src/main/java/com/picimako/justkitting/inlayhint/LightServicesHintPresentationAware.java)):
- there is no speed search implemented yet,
- the popup height is not yet limited to make the list scrollable
