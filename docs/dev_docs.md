# Developer Documentation

## Environment setup

### JDK

Install Java JDK 11 and configure the project to use that JDK if not automatically configured.

Also configure the **JAVA_HOME** environment variable on your system, this is needed for executing integration tests.

## Functional tests

Integrations tests build mostly on JUnit3-based platform test classes. For assertions, either the IntelliJ platform's underlying logic is used, or AssertJ
where applicable.

DevKit Plus base test classes:
- [WireMochaTestBase](../src/test/java/com/picimako/devkitplus/DevKitPlusTestBase.java) as the main base test class
- [IntentionTestBase](../src/test/java/com/picimako/devkitplus/intention/DevKitPlusIntentionTestBase.java) for Intention Actions
- [InspectionTestBase](../src/test/java/com/picimako/devkitplus/inspection/DevKitPlusInspectionTestBase.java) for Inspections
- [CodeCompletionTestBase](../src/test/java/com/picimako/devkitplus/action/DevKitPlusActionTestBase.java) for Actions

### Load 3rd-party libs

#### JDK

Java file based tests require either a mock or a real JDK to be available. This project is configured to always use a real JDK via `com.picimako.devkitplus.DevKitPlusTestBase.getJdkHome()`,
so that no modification of the [`idea.home.path` system property](https://plugins.jetbrains.com/docs/intellij/code-inspections.html#inspection-unit-test) is necessary for running tests locally.
And, using the JAVA_HOME based JDK also works on GitHub Actions.

#### Other libs

In order for tests to recognize code from other libraries, those libraries have to be added to the classpath too. You can use the various
`load*()` methods of `com.picimako.devkitplus.ThirdPartyLibraryLoader`.

These libraries are located in the `lib` directory under the project root.

**NOTE:** Since at least some jars coming from the IntelliJ platform, they are allowed to be added to this repository, only if they come from the community sources and aren't related to Fleet nor Marketplace.  

There is an alternative called `MavenDependencyUtil` that can simplify this logic but based on previous experience it has some issues on CI which may need to be investigated.

Other resources: [IntelliJ Platform Plugin SDK - How to test a JVM language?](https://plugins.jetbrains.com/docs/intellij/testing-faq.html#how-to-test-a-jvm-language) for MavenDependencyUtil

### testData

The `src/test/testData` folder is used to store test data. They can be configured as project roots when setting the test data path in functional tests.

## CI/CD

Qodana and changelog patching is disabled in GitHub Actions to cut build time, and unnecessary rounds of patching the changelog,
since the CI build runs on a personal GitHub account without subscription to Actions.  
