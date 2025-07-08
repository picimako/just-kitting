//Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.picimako.justkitting.action.getinstance

import com.picimako.justkitting.action.JustKittingActionTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Functional test for [GenerateStaticGetInstanceAction].
 *
 * NOTE: tests that would invoke the service level selection popup list, are disabled for now
 * due to no DataContext availability.
 */
class GenerateStaticGetInstanceActionJavaTest : JustKittingActionTestBase() {

    //Availability

    @Test
    fun testNotAvailableInNonJavaFile() {
        val psiFile = fixture.configureByText("non_java.txt", "<caret>")

        val isValid = GenerateStaticGetInstanceAction().isValidForFile(project, fixture.editor, psiFile)

        assertThat(isValid).isFalse()
    }

    @Test
    fun testNotAvailableInEnum() {
        val psiFile = fixture.configureByText("Enum.java",
            """
                import com.intellij.openapi.components.Service;

                @Service(Service.Level.PROJECT)
                public enum SomeService {
                    ENTRY;
                    public boolean someField;
                <caret>
                    public void someMethod() {
                    }
                }
                """.trimIndent())
        val isValid = GenerateStaticGetInstanceAction().isValidForFile(project, fixture.editor, psiFile)
        assertThat(isValid).isFalse()
    }

    @Test
    fun testNotAvailableIfGetInstanceAlreadyExists() {
        val psiFile = fixture.configureByText("SomeService.java",
            """
                import com.intellij.openapi.components.Service;

                @Service(Service.Level.PROJECT)
                public final class SomeService {
                    public boolean someField;
                <caret>
                    public static SomeService getInstance() {
                    }
                }
                """.trimIndent())
        val isValid = GenerateStaticGetInstanceAction().isValidForFile(project, fixture.editor, psiFile)
        assertThat(isValid).isFalse()
    }

    @Test
    fun testAvailable() {
        val psiFile = fixture.configureByText("SomeService.java",
            """
                import com.intellij.openapi.components.Service;
                import com.intellij.openapi.project.Project

                @Service(Service.Level.PROJECT)
                public final class SomeService {
                    public boolean someField;
                <caret>
                    public static void someMethod(Project project) {
                    }
                }
                """.trimIndent())
        val isValid = GenerateStaticGetInstanceAction().isValidForFile(project, fixture.editor, psiFile)
        assertThat(isValid).isTrue()
    }

    //Project-level light service

    @Test
    fun testShouldGenerateProjectLevelGetterForServiceLevelProject() {
        checkAction("SomeService.java", { GenerateStaticGetInstanceAction() },
            """
                import com.intellij.openapi.components.Service;

                @Service(Service.Level.PROJECT)
                public final class SomeService {
                    public boolean someField;
                <caret>
                    public void someMethod() {
                    }
                }
                """.trimIndent(),
            """
                import com.intellij.openapi.components.Service;
                import com.intellij.openapi.project.Project;

                @Service(Service.Level.PROJECT)
                public final class SomeService {
                    public boolean someField;

                    public static SomeService getInstance(Project project) {
                        return project.getService(SomeService.class);
                    }

                    public void someMethod() {
                    }
                }
                """.trimIndent())
    }

    //Application-level light service

    @Test
    fun testShouldGenerateAppLevelGetterForServiceLevelApp() {
        checkAction("SomeService.java", { GenerateStaticGetInstanceAction() },
            """
                import com.intellij.openapi.components.Service;

                @Service(Service.Level.APP)
                public final class SomeService {
                    public boolean someField;
                <caret>
                    public void someMethod() {
                    }
                }
                """.trimIndent(),
            """
                import com.intellij.openapi.application.ApplicationManager;
                import com.intellij.openapi.components.Service;

                @Service(Service.Level.APP)
                public final class SomeService {
                    public boolean someField;

                    public static SomeService getInstance() {
                        return ApplicationManager.getApplication().getService(SomeService.class);
                    }

                    public void someMethod() {
                    }
                }
                """.trimIndent()
            )
    }

    //Light service without level

    @Test
    fun testShouldGenerateApplicationLevelGetterForDefaultServiceLevelWithProperClassName() {
        checkAction("SomeApplicationService.java", { GenerateStaticGetInstanceAction() },
            """
                import com.intellij.openapi.components.Service;

                @Service
                public final class SomeApplicationService {
                    public boolean someField;
                <caret>
                    public void someMethod() {
                    }
                }
                """.trimIndent(),
            """
                import com.intellij.openapi.application.ApplicationManager;
                import com.intellij.openapi.components.Service;
                
                @Service
                public final class SomeApplicationService {
                    public boolean someField;
                
                    public static SomeApplicationService getInstance() {
                        return ApplicationManager.getApplication().getService(SomeApplicationService.class);
                    }
                
                    public void someMethod() {
                    }
                }
                """.trimIndent()
        )
    }

    @Test
    fun testShouldGenerateProjectLevelGetterForNoServiceWithProperClassName() {
        checkAction("SomeProjectService.java", { GenerateStaticGetInstanceAction() },
            """
                public final class SomeProjectService {
                    public boolean someField;
                <caret>
                    public void someMethod() {
                    }
                }
                """.trimIndent(),
            """
                import com.intellij.openapi.project.Project;

                public final class SomeProjectService {
                    public boolean someField;

                    public static SomeProjectService getInstance(Project project) {
                        return project.getService(SomeProjectService.class);
                    }

                    public void someMethod() {
                    }
                }
                """.trimIndent()
        )
    }

    @Test
    fun testShouldGenerateAppLevelGetterForEmptyServiceLevelWithProperClassName() {
        checkAction("SomeApplicationService.java", { GenerateStaticGetInstanceAction() },
            """
                import com.intellij.openapi.components.Service;

                @Service
                public final class SomeApplicationService {
                    public boolean someField;
                <caret>
                    public void someMethod() {
                    }
                }
                """.trimIndent(),
            """
                import com.intellij.openapi.application.ApplicationManager;
                import com.intellij.openapi.components.Service;

                @Service
                public final class SomeApplicationService {
                    public boolean someField;

                    public static SomeApplicationService getInstance() {
                        return ApplicationManager.getApplication().getService(SomeApplicationService.class);
                    }

                    public void someMethod() {
                    }
                }
                """.trimIndent()
        )
    }

    @Test
    fun testShouldGenerateAppLevelGetterForNoServiceWithProperClassName() {
        checkAction("SomeApplicationService.java", { GenerateStaticGetInstanceAction() },
            """
                public final class SomeApplicationService {
                    public boolean someField;
                <caret>
                    public void someMethod() {
                    }
                }
                """.trimIndent(),
            """
                import com.intellij.openapi.application.ApplicationManager;

                public final class SomeApplicationService {
                    public boolean someField;

                    public static SomeApplicationService getInstance() {
                        return ApplicationManager.getApplication().getService(SomeApplicationService.class);
                    }

                    public void someMethod() {
                    }
                }
                """.trimIndent()
        )
    }

    //    public void testShouldGenerateGetterForEmptyServiceLevelWithNoProperClassName() {
    //        PsiFile psiFile = fixture.configureByText("SomeService.java",
    //            "import com.intellij.openapi.components.Service;\n" +
    //                "\n" +
    //                "@Service\n" +
    //                "public final class SomeService {\n" +
    //                "    public boolean someField;\n" +
    //                "<caret>\n" +
    //                "    public void someMethod() {\n" +
    //                "    }\n" +
    //                "}");
    //
    //        fixture.testAction(GenerateStaticGetInstanceAction())
    //
    //        //Project-level due to the popup list not available in test mode, it and defaulting to the first item in it.
    //        fixture.checkResult(
    //            "import com.intellij.openapi.components.Service;\n" +
    //                "import com.intellij.openapi.project.Project;\n" +
    //                "\n" +
    //                "@Service\n" +
    //                "public final class SomeService {\n" +
    //                "    public boolean someField;\n" +
    //                "\n" +
    //                "    public static SomeService getInstance(Project project) {\n" +
    //                "        return project.getService(SomeService.class);\n" +
    //                "    }\n" +
    //                "\n" +
    //                "    public void someMethod() {\n" +
    //                "    }\n" +
    //                "}");
    //    }
    //
    //Other cases
    //
    //    public void testShouldGenerateGetterForServiceWithBothLevelsWithNoProperClassName() {
    //        PsiFile psiFile = fixture.configureByText("SomeService.java",
    //            "import com.intellij.openapi.components.Service;\n" +
    //                "\n" +
    //                "@Service(Service.Level.APP, Service.Level.PROJECT)\n" +
    //                "public final class SomeService {\n" +
    //                "    public boolean someField;\n" +
    //                "<caret>\n" +
    //                "    public void someMethod() {\n" +
    //                "    }\n" +
    //                "}");
    //
    //        fixture.testAction(GenerateStaticGetInstanceAction())
    //
    //        //Project-level due to the popup list not available in test mode, it and defaulting to the first item in it.
    //        fixture.checkResult(
    //            "import com.intellij.openapi.components.Service;\n" +
    //                "import com.intellij.openapi.project.Project;\n" +
    //                "\n" +
    //                "@Service\n" +
    //                "public final class SomeService {\n" +
    //                "    public boolean someField;\n" +
    //                "\n" +
    //                "    public static SomeService getInstance(Project project) {\n" +
    //                "        return project.getService(SomeService.class);\n" +
    //                "    }\n" +
    //                "\n" +
    //                "    public void someMethod() {\n" +
    //                "    }\n" +
    //                "}");
    //    }
//
    //    public void testShouldGenerateGetterForNonServiceClass() {
    //        PsiFile psiFile = fixture.configureByText("NonService.java",
    //            "public final class NonService {\n" +
    //                "    public boolean someField;\n" +
    //                "<caret>\n" +
    //                "    public void someMethod() {\n" +
    //                "    }\n" +
    //                "}");
    //
    //        fixture.testAction(GenerateStaticGetInstanceAction())
    //
    //        //Project-level due to the popup list not available in test mode, it and defaulting to the first item in it.
    //        fixture.checkResult(
    //            "import com.intellij.openapi.project.Project;\n" +
    //                "\n" +
    //                "@Service\n" +
    //                "public final class NonService {\n" +
    //                "    public boolean someField;\n" +
    //                "\n" +
    //                "    public static NonService getInstance(Project project) {\n" +
    //                "        return project.getService(NonService.class);\n" +
    //                "    }\n" +
    //                "\n" +
    //                "    public void someMethod() {\n" +
    //                "    }\n" +
    //                "}");
    //    }
}
