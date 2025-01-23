import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

//See gradle/libs.versions.toml
plugins {
    id("java") // Java support
    alias(libs.plugins.kotlin) // Kotlin support
    alias(libs.plugins.intelliJPlatform) // IntelliJ Platform Gradle Plugin
    alias(libs.plugins.changelog) // Gradle Changelog Plugin
    alias(libs.plugins.lombok) // Gradle Lombok Plugin
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

// Set the JVM language level used to build the project.
kotlin {
    jvmToolchain(21)
}

// Configure project's dependencies
repositories {
    mavenCentral()

    // IntelliJ Platform Gradle Plugin Repositories Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-repositories-extension.html
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    //Testing

    //Required for 'junit.framework.TestCase' referenced in 'com.intellij.testFramework.UsefulTestCase'
    testImplementation(libs.junit)
    testImplementation("org.assertj:assertj-core:3.27.3")
    //See https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-faq.html#missing-opentest4j-dependency-in-test-framework
    testImplementation("org.opentest4j:opentest4j:1.3.0")

    // IntelliJ Platform Gradle Plugin Dependencies Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html
    intellijPlatform {
        create(providers.gradleProperty("platformType"), providers.gradleProperty("platformVersion"))

        // Plugin Dependencies. Uses `platformBundledPlugins` property from the gradle.properties file for bundled IntelliJ Platform plugins.
        bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })

        // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file for plugin from JetBrains Marketplace.
        plugins(providers.gradleProperty("platformPlugins").map { it.split(',') })

        instrumentationTools()
        pluginVerifier()
        zipSigner()
        testFramework(TestFrameworkType.Platform)
        //Required for 'LightJavaCodeInsightFixtureTestCase'
        testFramework(TestFrameworkType.Plugin.Java)
    }
}

// Configure IntelliJ Platform Gradle Plugin - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-extension.html
intellijPlatform {
    pluginConfiguration {
        version = providers.gradleProperty("pluginVersion")

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        description = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            with(it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
            }
        }

        val changelog = project.changelog // local variable for configuration cache compatibility
        // Get the latest available change notes from the changelog file
        changeNotes = providers.gradleProperty("pluginVersion").map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = providers.gradleProperty("pluginUntilBuild")
        }
    }

    pluginVerification {
        ides {
            recommended()
        }
    }
}

intellijPlatformTesting {
    val runTestsInIJCommunity by intellijPlatformTesting.testIde.registering {
        type = IntelliJPlatformType.IntellijIdeaCommunity
        version = "2024.3.2"
        task {
            useJUnit {
                isScanForTestClasses = false
                include("**/*Test.class")
                //Disabled due to haven't been able to make the tests resolve the bundle properties files. The functionality works in production environment.
                exclude("**/PluginDescriptorTagsFoldingBuilderResourceBundleTest.class")
            }
        }
    }

    val runTestsWithK2InIJCommunity by intellijPlatformTesting.testIde.registering {
        type = IntelliJPlatformType.IntellijIdeaCommunity
        version = "2024.3.2"
        task {
            //See https://kotlin.github.io/analysis-api/testing-in-k2-locally.html
            jvmArgumentProviders += CommandLineArgumentProvider {
                listOf("-Didea.kotlin.plugin.use.k2=true")
            }
            useJUnit {
                isScanForTestClasses = false
                include("**/*Test.class")
                //Disabled due to haven't been able to make the tests resolve the bundle properties files. The functionality works in production environment.
                exclude("**/PluginDescriptorTagsFoldingBuilderResourceBundleTest.class")
            }
        }
    }
}

//Uncomment this to start the IDE with the K2 Kotlin compiler enabled
//tasks.named<RunIdeTask>("runIde") {
//    jvmArgumentProviders += CommandLineArgumentProvider {
//        listOf("-Didea.kotlin.plugin.use.k2=true")
//    }
//}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    groups.empty()
    repositoryUrl = providers.gradleProperty("pluginRepositoryUrl")
}

tasks {
    wrapper {
        gradleVersion = providers.gradleProperty("gradleVersion").get()
    }
}
