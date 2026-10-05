//Copyright 2026 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.inlayhint.services

import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.utils.inlays.InlayHintsProviderTestCase
import com.picimako.justkitting.ThirdPartyLibraryLoader

/**
 * Integration test for [LightServicesInlayHintsProvider].
 */
@Suppress("UnstableApiUsage")
class LightServicesInlayHintsProviderTest : InlayHintsProviderTestCase() {

    override fun setUp() {
        super.setUp()
        ThirdPartyLibraryLoader.loadPlatform(myFixture)
    }

    override fun getTestDataPath(): String {
        return "src/test/testData/inlayproject"
    }

    override fun getProjectDescriptor(): LightProjectDescriptor {
        //When executing the entire test class without a project descriptor (the same project being used for each test),
        // something in the background is stuck, and some tests fail.
        //Thus, a new project descriptor (essentially a new project) is created for each test that solves the issue.
        return LightProjectDescriptor()
    }

    private fun loadLightServiceFiles() {
        //This file is included to test that references of the Service class are not taken into account when not used as part of an annotation.
        myFixture.copyFileToProject("ANonServiceClassWithServiceLevelParameter.kt")
        myFixture.copyFileToProject("AProjectService.java")
        myFixture.copyFileToProject("AnApplicationService.java")
        myFixture.copyFileToProject("AProjectAndApplicationService.kt")
    }

    //No hint

    fun `test no hint when there is no light service in the project`() {
        doTestProvider(
            "plugin.xml",
            """
                <idea-plugin>
                    <extensions defaultExtensionNs="com.intellij">
                    </extensions>
                </idea-plugin>
                """.trimIndent(),
            LightServicesInlayHintsProvider(),
            Settings(),
            false)
    }

    fun `test no hint in non-plugin descriptor file called plugin xml`() {
        doTestProvider(
            "plugin.xml",
            """
                <non-idea-plugin>
                     <extensions defaultExtensionNs="com.intellij">
                    </extensions>
                </non-idea-plugin>
                """.trimIndent(),
            LightServicesInlayHintsProvider(),
            Settings(lightServicesDisplayMode = InlayDisplayMode.ListOfLightServices, maxNumberOfServicesToDisplay = 3),
            false)
    }

    fun `test no hint on non-extension tag`() {
        doTestProvider(
            "plugin.xml",
            """
                <idea-plugin>
                    <actions>
                    </actions>
                </idea-plugin>
                """.trimIndent(),
            LightServicesInlayHintsProvider(),
            Settings(lightServicesDisplayMode = InlayDisplayMode.ListOfLightServices, maxNumberOfServicesToDisplay = 3),
            false)
    }

    fun `test no hint on extensions with non-comintellij namespace`() {
        doTestProvider(
            "plugin.xml",
            """
                <idea-plugin>
                    <extensions defaultExtensionNs="non.matching.namespace">
                    </extensions>
                </idea-plugin>
                """.trimIndent(),
            LightServicesInlayHintsProvider(),
            Settings(lightServicesDisplayMode = InlayDisplayMode.ListOfLightServices, maxNumberOfServicesToDisplay = 3),
            false)
    }

    //plugin.xml

    fun `test list of services without View All - plugin xml`() {
        loadLightServiceFiles()
        doTestProvider(
            "plugin.xml",
            """
                <idea-plugin>
                <# block -- Project light services --
                AProjectService
                -- Application light services --
                AnApplicationService
                -- Project and application light services --
                AProjectAndApplicationService #>
                /*<# block -- Project light services --
                AProjectService
                -- Application light services --
                AnApplicationService
                -- Project and application light services --
                AProjectAndApplicationService #>*/
                    <extensions defaultExtensionNs="com.intellij">
                    </extensions>
                </idea-plugin>
                """.trimIndent(),
            LightServicesInlayHintsProvider(),
            Settings(lightServicesDisplayMode = InlayDisplayMode.ListOfLightServices, maxNumberOfServicesToDisplay = 3),
            true)
    }

    fun `test list of services with View All - plugin xml`() {
        loadLightServiceFiles()
        doTestProvider(
            "plugin.xml",
            """
                <idea-plugin>
                <# block -- Project light services --
                AProjectService
                View all light services... #>
                /*<# block -- Project light services --
                AProjectService
                View all light services... #>*/
                    <extensions defaultExtensionNs="com.intellij">
                    </extensions>
                </idea-plugin>
                """.trimIndent(),
            LightServicesInlayHintsProvider(),
            Settings(lightServicesDisplayMode = InlayDisplayMode.ListOfLightServices, maxNumberOfServicesToDisplay = 1),
            true)
    }

    fun `test View All only - plugin xml`() {
        loadLightServiceFiles()
        doTestProvider(
            "plugin.xml",
            """
                <idea-plugin>
                <# block View all light services... #>
                /*<# block View all light services... #>*/
                    <extensions defaultExtensionNs="com.intellij">
                    </extensions>
                </idea-plugin>
                """.trimIndent(),
            LightServicesInlayHintsProvider(),
            Settings(lightServicesDisplayMode = InlayDisplayMode.ViewAllOnly),
            true)
    }

    //Modules descriptor

    fun `test list of services without View All - module descriptor`() {
        loadLightServiceFiles()
        doTestProvider(
            "module.descriptor.xml",
            """
                <idea-plugin>
                <# block -- Project light services --
                AProjectService
                -- Application light services --
                AnApplicationService
                -- Project and application light services --
                AProjectAndApplicationService #>
                /*<# block -- Project light services --
                AProjectService
                -- Application light services --
                AnApplicationService
                -- Project and application light services --
                AProjectAndApplicationService #>*/
                    <extensions defaultExtensionNs="com.intellij">
                    </extensions>
                </idea-plugin>
                """.trimIndent(),
            LightServicesInlayHintsProvider(),
            Settings(lightServicesDisplayMode = InlayDisplayMode.ListOfLightServices, maxNumberOfServicesToDisplay = 3),
            true)
    }

    fun `test list of services with View All - module descriptor`() {
        loadLightServiceFiles()
        doTestProvider(
            "module.descriptor.xml",
            """
                <idea-plugin>
                <# block -- Project light services --
                AProjectService
                View all light services... #>
                /*<# block -- Project light services --
                AProjectService
                View all light services... #>*/
                    <extensions defaultExtensionNs="com.intellij">
                    </extensions>
                </idea-plugin>
                """.trimIndent(),
            LightServicesInlayHintsProvider(),
            Settings(lightServicesDisplayMode = InlayDisplayMode.ListOfLightServices, maxNumberOfServicesToDisplay = 1),
            true)
    }

    fun `test View All only - module descriptor`() {
        loadLightServiceFiles()
        doTestProvider(
            "module.descriptor.xml",
            """
                <idea-plugin>
                <# block View all light services... #>
                /*<# block View all light services... #>*/
                    <extensions defaultExtensionNs="com.intellij">
                    </extensions>
                </idea-plugin>
                """.trimIndent(),
            LightServicesInlayHintsProvider(),
            Settings(lightServicesDisplayMode = InlayDisplayMode.ViewAllOnly),
            true)
    }
}
