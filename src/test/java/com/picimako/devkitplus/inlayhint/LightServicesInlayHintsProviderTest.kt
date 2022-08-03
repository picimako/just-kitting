//Copyright 2021 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.devkitplus.inlayhint

import com.picimako.devkitplus.DevKitPlusTestBase
import com.intellij.codeInsight.hints.CollectorWithSettings
import com.intellij.codeInsight.hints.InlayHintsSinkImpl
import org.assertj.core.api.Assertions.assertThat

/**
 * Functional test for [LightServicesInlayHintsProvider].
 */
@Suppress("UnstableApiUsage")
class LightServicesInlayHintsProviderTest : DevKitPlusTestBase() {

    override fun getTestDataPath(): String {
        return "src/test/testData/inlayproject"
    }

    private fun loadLightServiceFiles() {
        myFixture.copyFileToProject("AProjectService.java")
        myFixture.copyFileToProject("AnApplicationService.java")
    }

    fun testNoHint() {
        doTest()
    }

    fun testListOfServicesWithoutViewAll() {
        loadLightServiceFiles()
        doTest(Settings(lightServicesDisplayMode = InlayDisplayMode.ListOfLightServices, maxNumberOfServicesToDisplay = 2), 1,
            """-- Project light services --
AProjectService
-- Application light services --
AnApplicationService
""".trimIndent())
    }

    fun testListOfServicesWithViewAll() {
        loadLightServiceFiles()
        doTest(Settings(lightServicesDisplayMode = InlayDisplayMode.ListOfLightServices, maxNumberOfServicesToDisplay = 1), 1,
            """-- Project light services --
AProjectService
View all light services...
""".trimIndent())
    }

    fun testViewAllOnly() {
        loadLightServiceFiles()
        doTest(Settings(lightServicesDisplayMode = InlayDisplayMode.ViewAllOnly), 1,
            """View all light services...
""".trimIndent())
    }

    /**
     * This method is an attempt to marry the logic in [com.intellij.testFramework.utils.inlays.InlayHintsProviderTestCase]
     * (in which I couldn't add platform-api.jar to the test project) and have the ability via [com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase]
     * to add platform-api.jar, so that the [com.intellij.openapi.components.Service] annotation becomes available.
     */
    private fun doTest(settings: Settings = Settings(), expectedInlayBlockCount: Int = 0, expectedInlayBlockText: String = "") {
        myFixture.configureByText("plugin.xml", """
            <idea-plugin>
                <id>a.plugin.id</id>
                <extensions defaultExtensionNs="com.intellij">
                </extensions>
            </idea-plugin>
        """.trimIndent())
        val sink = InlayHintsSinkImpl(myFixture.editor)
        val provider = LightServicesInlayHintsProvider()
        val collector = provider.getCollectorFor(myFixture.file, myFixture.editor, settings, sink)
        CollectorWithSettings(collector, provider.key, myFixture.file.language, sink).collectTraversingAndApply(myFixture.editor, myFixture.file, true)
        val lightServicesHints = myFixture.editor.inlayModel.getBlockElementsInRange(39, 39)
        assertThat(lightServicesHints).hasSize(expectedInlayBlockCount)
        if (expectedInlayBlockCount == 1) {
            assertThat(lightServicesHints[0].renderer).hasToString(expectedInlayBlockText)
        }
    }
}
