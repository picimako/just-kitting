//Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.picimako.justkitting.action.getinstance

import com.picimako.justkitting.JustKittingTestBase
import org.assertj.core.api.Assertions.assertThat

/**
 * Functional test for [GenerateStaticGetInstanceAction].
 */
class GenerateStaticGetInstanceActionKotlinTest : JustKittingTestBase() {

    //Availability

    fun testNotAvailableInNonKotlinFile() {
        val psiFile = myFixture.configureByText("non_kotlin.txt", "<caret>")

        val isValid = GenerateStaticGetInstanceAction().isValidForFile(project, myFixture.editor, psiFile)

        assertThat(isValid).isFalse()
    }

    fun testNotAvailableInEnum() {
        val psiFile = myFixture.configureByText("Enum.kt",
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
        val isValid = GenerateStaticGetInstanceAction().isValidForFile(project, myFixture.editor, psiFile)
        assertThat(isValid).isFalse()
    }

    fun testNotAvailableIfGetInstanceAlreadyExistsCaretInCompanion() {
        val psiFile = myFixture.configureByText("SomeService.kt",
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
        val isValid = GenerateStaticGetInstanceAction().isValidForFile(project, myFixture.editor, psiFile)
        assertThat(isValid).isFalse()
    }

    fun testNotAvailableIfGetInstanceAlreadyExistsCaretInServiceClass() {
        val psiFile = myFixture.configureByText("SomeService.kt",
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
        val isValid = GenerateStaticGetInstanceAction().isValidForFile(project, myFixture.editor, psiFile)
        assertThat(isValid).isFalse()
    }

    fun testAvailableWithoutCompanionObject() {
        val psiFile = myFixture.configureByText("SomeService.kt",
            """
                import com.intellij.openapi.components.Service

                @Service(Service.Level.APP)
                class SomeService() {
                <caret>
                }
                """.trimIndent())
        val isValid = GenerateStaticGetInstanceAction().isValidForFile(project, myFixture.editor, psiFile)
        assertThat(isValid).isTrue()
    }

    fun testAvailableWithEmptyCompanionObjectCaretInCompanion() {
        val psiFile = myFixture.configureByText("SomeService.kt",
            """
                import com.intellij.openapi.components.Service

                @Service(Service.Level.APP)
                class SomeService() {
                    companion object {
                        <caret>
                    }
                }
                """.trimIndent())
        val isValid = GenerateStaticGetInstanceAction().isValidForFile(project, myFixture.editor, psiFile)
        assertThat(isValid).isTrue()
    }

    fun testAvailableWithEmptyCompanionObjectCaretInServiceClass() {
        val psiFile = myFixture.configureByText("SomeService.kt",
            """
                import com.intellij.openapi.components.Service

                @Service(Service.Level.APP)
                class SomeService(project: Project) {
                    <caret>
                    companion object {
                    }
                }
                """.trimIndent())
        val isValid = GenerateStaticGetInstanceAction().isValidForFile(project, myFixture.editor, psiFile)
        assertThat(isValid).isTrue()
    }

    //Project-level light service

    fun testShouldGenerateProjectLevelGetterForServiceLevelProjectForNonExistentCompanion() {
        myFixture.configureByText("SomeService.kt",
            """
                import com.intellij.openapi.components.Service

                @Service(Service.Level.PROJECT)
                class SomeService {
                <caret>
                    fun someMethod() {
                    }
                }
                """.trimIndent())

        myFixture.testAction(GenerateStaticGetInstanceAction())

        myFixture.checkResult(
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
                """.trimIndent())
    }

    fun testShouldGenerateProjectLevelGetterForServiceLevelProjectForNonExistingCompanion() {
        myFixture.configureByText("SomeService.kt",
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
                """.trimIndent())

        myFixture.testAction(GenerateStaticGetInstanceAction())

        myFixture.checkResult(
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
                """.trimIndent())
    }

    //Application-level light service

    fun testShouldGenerateAppLevelGetterForServiceLevelProjectForNonExistentCompanion() {
        myFixture.configureByText("SomeService.kt",
            """
                import com.intellij.openapi.components.Service

                @Service(Service.Level.APP)
                class SomeService {
                <caret>
                    fun someMethod() {
                    }
                }
                """.trimIndent())

        myFixture.testAction(GenerateStaticGetInstanceAction())

        myFixture.checkResult(
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
                """.trimIndent())
    }

    fun testShouldGenerateAppLevelGetterForServiceLevelProjectForNonExistingCompanion() {
        myFixture.configureByText("SomeService.kt",
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
                """.trimIndent())

        myFixture.testAction(GenerateStaticGetInstanceAction())

        myFixture.checkResult(
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
                """.trimIndent())
    }

    //Light service without level

    fun testShouldGenerateProjectLevelGetterForEmptyServiceLevelWithProperClassName() {
        myFixture.configureByText("SomeProjectService.kt",
            """
                @Service
                class SomeProjectService {
                <caret>
                    fun someMethod() {
                    }
                }
                """.trimIndent())

        myFixture.testAction(GenerateStaticGetInstanceAction())

        myFixture.checkResult(
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
                """.trimIndent())
    }

    fun testShouldGenerateProjectLevelGetterForNoServiceWithProperClassName() {
        myFixture.configureByText("SomeProjectService.kt",
            """
                class SomeProjectService {
                <caret>
                    fun someMethod() {
                    }
                }
                """.trimIndent())

        myFixture.testAction(GenerateStaticGetInstanceAction())

        myFixture.checkResult(
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
                """.trimIndent())
    }

    fun testShouldGenerateAppLevelGetterForEmptyServiceLevelWithProperClassName() {
        myFixture.configureByText("SomeApplicationService.kt",
            """
                import com.intellij.openapi.components.Service

                @Service
                class SomeApplicationService {
                <caret>
                    fun someMethod() {
                    }
                }
                """.trimIndent())

        myFixture.testAction(GenerateStaticGetInstanceAction())

        myFixture.checkResult(
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
                """.trimIndent())
    }

    fun testShouldGenerateAppLevelGetterForNoServiceWithProperClassName() {
        myFixture.configureByText("SomeApplicationService.kt",
            """
                class SomeApplicationService {
                    <caret>
                    fun someMethod() {
                    }
                }
                """.trimIndent())

        myFixture.testAction(GenerateStaticGetInstanceAction())

        myFixture.checkResult(
            """
                import com.intellij.openapi.components.service

                class SomeApplicationService {
                    
                    fun someMethod() {
                    }
                
                    companion object {
                        fun getInstance(): SomeApplicationService = service()
                    }
                }
                """.trimIndent())
    }
}
