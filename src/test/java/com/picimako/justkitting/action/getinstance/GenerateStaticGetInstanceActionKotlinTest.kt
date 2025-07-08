//Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.picimako.justkitting.action.getinstance

import com.picimako.justkitting.action.JustKittingActionTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Functional test for [GenerateStaticGetInstanceAction].
 */
class GenerateStaticGetInstanceActionKotlinTest : JustKittingActionTestBase() {

    //Availability

    @Test
    fun testNotAvailableInNonKotlinFile() {
        val psiFile = fixture.configureByText("non_kotlin.txt", "<caret>")

        val isValid = GenerateStaticGetInstanceAction().isValidForFile(project, fixture.editor, psiFile)

        assertThat(isValid).isFalse()
    }

    @Test
    fun testNotAvailableInEnum() {
        val psiFile = fixture.configureByText("Enum.kt",
            """
                import com.intellij.openapi.components.Service

                @Service(Service.Level.PROJECT)
                enum SomeService() {
                    ENTRY
                <caret>
                    fun someMethod() {
                    }
                }
                """.trimIndent())
        val isValid = GenerateStaticGetInstanceAction().isValidForFile(project, fixture.editor, psiFile)
        assertThat(isValid).isFalse()
    }

    @Test
    fun testNotAvailableIfGetInstanceAlreadyExistsCaretInCompanion() {
        val psiFile = fixture.configureByText("SomeService.kt",
            """
                import com.intellij.openapi.components.Service
                import com.intellij.openapi.components.service
                import com.intellij.openapi.project.Project

                @Service(Service.Level.PROJECT)
                class SomeService(project: Project) {
                    companion object {
                        <caret>
                        fun getInstance(project: Project): SomeService = project.service()
                    }
                }
                """.trimIndent())
        val isValid = GenerateStaticGetInstanceAction().isValidForFile(project, fixture.editor, psiFile)
        assertThat(isValid).isFalse()
    }

    @Test
    fun testNotAvailableIfGetInstanceAlreadyExistsCaretInServiceClass() {
        val psiFile = fixture.configureByText("SomeService.kt",
            """
                import com.intellij.openapi.components.Service
                import com.intellij.openapi.components.service
                import com.intellij.openapi.project.Project

                @Service(Service.Level.PROJECT)
                class SomeService(project: Project) {
                    <caret>
                    companion object {
                        fun getInstance(project: Project): SomeService = project.service()
                    }
                }
                """.trimIndent())
        val isValid = GenerateStaticGetInstanceAction().isValidForFile(project, fixture.editor, psiFile)
        assertThat(isValid).isFalse()
    }

    @Test
    fun testAvailableWithoutCompanionObject() {
        val psiFile = fixture.configureByText("SomeService.kt",
            """
                import com.intellij.openapi.components.Service

                @Service(Service.Level.APP)
                class SomeService() {
                <caret>
                }
                """.trimIndent())
        val isValid = GenerateStaticGetInstanceAction().isValidForFile(project, fixture.editor, psiFile)
        assertThat(isValid).isTrue()
    }

    @Test
    fun testAvailableWithEmptyCompanionObjectCaretInCompanion() {
        val psiFile = fixture.configureByText("SomeService.kt",
            """
                import com.intellij.openapi.components.Service

                @Service(Service.Level.APP)
                class SomeService() {
                    companion object {
                        <caret>
                    }
                }
                """.trimIndent())
        val isValid = GenerateStaticGetInstanceAction().isValidForFile(project, fixture.editor, psiFile)
        assertThat(isValid).isTrue()
    }

    @Test
    fun testAvailableWithEmptyCompanionObjectCaretInServiceClass() {
        val psiFile = fixture.configureByText("SomeService.kt",
            """
                import com.intellij.openapi.components.Service

                @Service(Service.Level.APP)
                class SomeService(project: Project) {
                    <caret>
                    companion object {
                    }
                }
                """.trimIndent())
        val isValid = GenerateStaticGetInstanceAction().isValidForFile(project, fixture.editor, psiFile)
        assertThat(isValid).isTrue()
    }

    //Project-level light service

    @Test
    fun testShouldGenerateProjectLevelGetterForServiceLevelProjectForNonExistentCompanion() {
        checkAction("SomeService.kt", { GenerateStaticGetInstanceAction() },
            """
                import com.intellij.openapi.components.Service

                @Service(Service.Level.PROJECT)
                class SomeService {
                <caret>
                    fun someMethod() {
                    }
                }
                """.trimIndent(),
            """
                import com.intellij.openapi.components.Service
                import com.intellij.openapi.components.service
                import com.intellij.openapi.project.Project

                @Service(Service.Level.PROJECT)
                class SomeService {
                
                    fun someMethod() {
                    }
                
                    companion object {
                        fun getInstance(project: Project): SomeService = project.service()
                    }
                }
                """.trimIndent()
            )
    }

    @Test
    fun testShouldGenerateProjectLevelGetterForServiceLevelProjectForNonExistingCompanion() {
        checkAction("SomeService.kt", { GenerateStaticGetInstanceAction() },
            """
                import com.intellij.openapi.components.Service

                @Service(Service.Level.PROJECT)
                class SomeService {
                <caret>
                    fun someMethod() {
                    }
                    
                    companion object {
                    }
                }
                """.trimIndent(),
            """
                import com.intellij.openapi.components.Service
                import com.intellij.openapi.components.service
                import com.intellij.openapi.project.Project

                @Service(Service.Level.PROJECT)
                class SomeService {
                
                    fun someMethod() {
                    }
                    
                    companion object {
                        fun getInstance(project: Project): SomeService = project.service()
                    }
                }
                """.trimIndent()
        )
    }

    //Application-level light service

    @Test
    fun testShouldGenerateAppLevelGetterForServiceLevelProjectForNonExistentCompanion() {
        checkAction("SomeService.kt", { GenerateStaticGetInstanceAction() },
            """
                import com.intellij.openapi.components.Service

                @Service(Service.Level.APP)
                class SomeService {
                <caret>
                    fun someMethod() {
                    }
                }
                """.trimIndent(),
            """
                import com.intellij.openapi.components.Service
                import com.intellij.openapi.components.service

                @Service(Service.Level.APP)
                class SomeService {
                
                    fun someMethod() {
                    }
                
                    companion object {
                        fun getInstance(): SomeService = service()
                    }
                }
                """.trimIndent()
        )
    }

    @Test
    fun testShouldGenerateAppLevelGetterForServiceLevelProjectForNonExistingCompanion() {
        checkAction("SomeService.kt", { GenerateStaticGetInstanceAction() },
            """
                import com.intellij.openapi.components.Service

                @Service(Service.Level.APP)
                class SomeService {
                <caret>
                    fun someMethod() {
                    }
                    
                    companion object {
                    }
                }
                """.trimIndent(),
            """
                import com.intellij.openapi.components.Service
                import com.intellij.openapi.components.service

                @Service(Service.Level.APP)
                class SomeService {
                
                    fun someMethod() {
                    }
                    
                    companion object {
                        fun getInstance(): SomeService = service()
                    }
                }
                """.trimIndent()
        )
    }

    //Light service without level

    @Test
    fun testShouldGenerateProjectLevelGetterForEmptyServiceLevelWithProperClassName() {
        checkAction("SomeService.kt", { GenerateStaticGetInstanceAction() },
            """
                @Service
                class SomeProjectService {
                <caret>
                    fun someMethod() {
                    }
                }
                """.trimIndent(),
            """
                import com.intellij.openapi.components.service
                import com.intellij.openapi.project.Project

                @Service
                class SomeProjectService {
                
                    fun someMethod() {
                    }
                
                    companion object {
                        fun getInstance(project: Project): SomeProjectService = project.service()
                    }
                }
                """.trimIndent()
        )
    }

    @Test
    fun testShouldGenerateProjectLevelGetterForNoServiceWithProperClassName() {
        checkAction("SomeService.kt", { GenerateStaticGetInstanceAction() },
            """
                class SomeProjectService {
                <caret>
                    fun someMethod() {
                    }
                }
                """.trimIndent(),
            """
                import com.intellij.openapi.components.service
                import com.intellij.openapi.project.Project

                class SomeProjectService {
                
                    fun someMethod() {
                    }
                
                    companion object {
                        fun getInstance(project: Project): SomeProjectService = project.service()
                    }
                }
                """.trimIndent()
        )
    }

    @Test
    fun testShouldGenerateAppLevelGetterForEmptyServiceLevelWithProperClassName() {
        checkAction("SomeService.kt", { GenerateStaticGetInstanceAction() },
            """
                import com.intellij.openapi.components.Service

                @Service
                class SomeApplicationService {
                <caret>
                    fun someMethod() {
                    }
                }
                """.trimIndent(),
            """
                import com.intellij.openapi.components.Service
                import com.intellij.openapi.components.service

                @Service
                class SomeApplicationService {
                
                    fun someMethod() {
                    }
                
                    companion object {
                        fun getInstance(): SomeApplicationService = service()
                    }
                }
                """.trimIndent()
        )
    }

    @Test
    fun testShouldGenerateAppLevelGetterForNoServiceWithProperClassName() {
        checkAction("SomeService.kt", { GenerateStaticGetInstanceAction() },
            """
                class SomeApplicationService {
                    <caret>
                    fun someMethod() {
                    }
                }
                """.trimIndent(),
            """
                import com.intellij.openapi.components.service

                class SomeApplicationService {
                    
                    fun someMethod() {
                    }
                
                    companion object {
                        fun getInstance(): SomeApplicationService = service()
                    }
                }
                """.trimIndent()
        )
    }
}
