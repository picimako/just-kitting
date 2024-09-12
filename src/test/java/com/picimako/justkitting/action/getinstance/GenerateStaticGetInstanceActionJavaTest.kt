//Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.picimako.justkitting.action.getinstance

import com.picimako.justkitting.JustKittingTestBase
import org.assertj.core.api.Assertions.assertThat

/**
 * Functional test for [GenerateStaticGetInstanceAction].
 *
 * NOTE: tests that would invoke the service level selection popup list, are disabled for now
 * due to no DataContext availability.
 */
class GenerateStaticGetInstanceActionJavaTest : JustKittingTestBase() {

    //Availability

    fun testNotAvailableInNonJavaFile() {
        val psiFile = myFixture.configureByText("non_java.txt", "<caret>")

        val isValid = GenerateStaticGetInstanceAction().isValidForFile(project, myFixture.editor, psiFile)

        assertThat(isValid).isFalse()
    }

    fun testNotAvailableInEnum() {
        val psiFile = myFixture.configureByText("Enum.java",
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
        val isValid = GenerateStaticGetInstanceAction().isValidForFile(project, myFixture.editor, psiFile)
        assertThat(isValid).isFalse()
    }

    fun testNotAvailableIfGetInstanceAlreadyExists() {
        val psiFile = myFixture.configureByText("SomeService.java",
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
        val isValid = GenerateStaticGetInstanceAction().isValidForFile(project, myFixture.editor, psiFile)
        assertThat(isValid).isFalse()
    }

    fun testAvailable() {
        val psiFile = myFixture.configureByText("SomeService.java",
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
        val isValid = GenerateStaticGetInstanceAction().isValidForFile(project, myFixture.editor, psiFile)
        assertThat(isValid).isTrue()
    }

    //Project-level light service

    fun testShouldGenerateProjectLevelGetterForServiceLevelProject() {
        myFixture.configureByText("SomeService.java",
            """
                import com.intellij.openapi.components.Service;

                @Service(Service.Level.PROJECT)
                public final class SomeService {
                    public boolean someField;
                <caret>
                    public void someMethod() {
                    }
                }
                """.trimIndent())

        myFixture.testAction(GenerateStaticGetInstanceAction())

        myFixture.checkResult(
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

    fun testShouldGenerateAppLevelGetterForServiceLevelApp() {
        myFixture.configureByText("SomeService.java",
            """
                import com.intellij.openapi.components.Service;

                @Service(Service.Level.APP)
                public final class SomeService {
                    public boolean someField;
                <caret>
                    public void someMethod() {
                    }
                }
                """.trimIndent())

        myFixture.testAction(GenerateStaticGetInstanceAction())

        myFixture.checkResult(
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
                """.trimIndent())
    }

    //Light service without level

    fun testShouldGenerateApplicationLevelGetterForDefaultServiceLevelWithProperClassName() {
        myFixture.configureByText("SomeApplicationService.java",
            """
                import com.intellij.openapi.components.Service;

                @Service
                public final class SomeApplicationService {
                    public boolean someField;
                <caret>
                    public void someMethod() {
                    }
                }
                """.trimIndent())

        myFixture.testAction(GenerateStaticGetInstanceAction())

        myFixture.checkResult(
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
                """.trimIndent())
    }

    fun testShouldGenerateProjectLevelGetterForNoServiceWithProperClassName() {
        myFixture.configureByText("SomeProjectService.java",
            """
                public final class SomeProjectService {
                    public boolean someField;
                <caret>
                    public void someMethod() {
                    }
                }
                """.trimIndent())

        myFixture.testAction(GenerateStaticGetInstanceAction())

        myFixture.checkResult(
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
                """.trimIndent())
    }

    fun testShouldGenerateAppLevelGetterForEmptyServiceLevelWithProperClassName() {
        myFixture.configureByText("SomeApplicationService.java",
            """
                import com.intellij.openapi.components.Service;

                @Service
                public final class SomeApplicationService {
                    public boolean someField;
                <caret>
                    public void someMethod() {
                    }
                }
                """.trimIndent())

        myFixture.testAction(GenerateStaticGetInstanceAction())

        myFixture.checkResult(
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
                """.trimIndent())
    }

    fun testShouldGenerateAppLevelGetterForNoServiceWithProperClassName() {
        myFixture.configureByText("SomeApplicationService.java",
            """
                public final class SomeApplicationService {
                    public boolean someField;
                <caret>
                    public void someMethod() {
                    }
                }
                """.trimIndent())

        myFixture.testAction(GenerateStaticGetInstanceAction())

        myFixture.checkResult(
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
                """.trimIndent())
    }

    //    public void testShouldGenerateGetterForEmptyServiceLevelWithNoProperClassName() {
    //        PsiFile psiFile = myFixture.configureByText("SomeService.java",
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
    //        myFixture.testAction(GenerateStaticGetInstanceAction())
    //
    //        //Project-level due to the popup list not available in test mode, it and defaulting to the first item in it.
    //        myFixture.checkResult(
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
    //        PsiFile psiFile = myFixture.configureByText("SomeService.java",
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
    //        myFixture.testAction(GenerateStaticGetInstanceAction())
    //
    //        //Project-level due to the popup list not available in test mode, it and defaulting to the first item in it.
    //        myFixture.checkResult(
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
    //        PsiFile psiFile = myFixture.configureByText("NonService.java",
    //            "public final class NonService {\n" +
    //                "    public boolean someField;\n" +
    //                "<caret>\n" +
    //                "    public void someMethod() {\n" +
    //                "    }\n" +
    //                "}");
    //
    //        myFixture.testAction(GenerateStaticGetInstanceAction())
    //
    //        //Project-level due to the popup list not available in test mode, it and defaulting to the first item in it.
    //        myFixture.checkResult(
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
