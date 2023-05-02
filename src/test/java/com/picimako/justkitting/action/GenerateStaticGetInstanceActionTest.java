//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.action;

import static org.assertj.core.api.Assertions.assertThat;

import com.picimako.justkitting.JustKittingTestBase;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.psi.PsiFile;

/**
 * Functional test for {@link GenerateStaticGetInstanceAction}.
 * <p>
 * NOTE: tests that would invoke the service level selection popup list, are disabled for now
 * due to no DataContext availability.
 */
public class GenerateStaticGetInstanceActionTest extends JustKittingTestBase {

    //Availability

    public void testNotAvailableInNonJavaFile() {
        PsiFile psiFile = myFixture.configureByText("non_java.txt", "<caret>");

        boolean isValid = new GenerateStaticGetInstanceAction().isValidForFile(getProject(), myFixture.getEditor(), psiFile);

        assertThat(isValid).isFalse();
    }

    public void testNotAvailableInEnum() {
        PsiFile psiFile = myFixture.configureByText("Enum.java",
            "import com.intellij.openapi.components.Service;\n" +
                "\n" +
                "@Service(Service.Level.PROJECT)\n" +
                "public enum SomeService {\n" +
                "    ENTRY;\n" +
                "    public boolean someField;\n" +
                "<caret>\n" +
                "    public void someMethod() {\n" +
                "    }\n" +
                "}");

        boolean isValid = new GenerateStaticGetInstanceAction().isValidForFile(getProject(), myFixture.getEditor(), psiFile);

        assertThat(isValid).isFalse();
    }

    public void testNotAvailableIfGetInstanceAlreadyExists() {
        PsiFile psiFile = myFixture.configureByText("SomeService.java",
            "import com.intellij.openapi.components.Service;\n" +
                "\n" +
                "@Service(Service.Level.PROJECT)\n" +
                "public final class SomeService {\n" +
                "    public boolean someField;\n" +
                "<caret>\n" +
                "    public static SomeService getInstance() {\n" +
                "    }\n" +
                "}");

        boolean isValid = new GenerateStaticGetInstanceAction().isValidForFile(getProject(), myFixture.getEditor(), psiFile);

        assertThat(isValid).isFalse();
    }

    public void testAvailable() {
        PsiFile psiFile = myFixture.configureByText("SomeService.java",
            "import com.intellij.openapi.components.Service;\n" +
                "\n" +
                "@Service(Service.Level.PROJECT)\n" +
                "public final class SomeService {\n" +
                "    public boolean someField;\n" +
                "<caret>\n" +
                "    public static void someMethod() {\n" +
                "    }\n" +
                "}");

        boolean isValid = new GenerateStaticGetInstanceAction().isValidForFile(getProject(), myFixture.getEditor(), psiFile);

        assertThat(isValid).isTrue();
    }

    //Project-level light service

    public void testShouldGenerateProjectLevelGetterForServiceLevelProject() {
        PsiFile psiFile = myFixture.configureByText("SomeService.java",
            "import com.intellij.openapi.components.Service;\n" +
                "\n" +
                "@Service(Service.Level.PROJECT)\n" +
                "public final class SomeService {\n" +
                "    public boolean someField;\n" +
                "<caret>\n" +
                "    public void someMethod() {\n" +
                "    }\n" +
                "}");

        runGenerateGetInstanceActionOn(psiFile);

        myFixture.checkResult(
            "import com.intellij.openapi.components.Service;\n" +
                "import com.intellij.openapi.project.Project;\n" +
                "\n" +
                "@Service(Service.Level.PROJECT)\n" +
                "public final class SomeService {\n" +
                "    public boolean someField;\n" +
                "\n" +
                "    public static SomeService getInstance(Project project) {\n" +
                "        return project.getService(SomeService.class);\n" +
                "    }\n" +
                "\n" +
                "    public void someMethod() {\n" +
                "    }\n" +
                "}");
    }

    //Application-level light service

    public void testShouldGenerateAppLevelGetterForServiceLevelApp() {
        PsiFile psiFile = myFixture.configureByText("SomeService.java",
            "import com.intellij.openapi.components.Service;\n" +
                "\n" +
                "@Service(Service.Level.APP)\n" +
                "public final class SomeService {\n" +
                "    public boolean someField;\n" +
                "<caret>\n" +
                "    public void someMethod() {\n" +
                "    }\n" +
                "}");

        runGenerateGetInstanceActionOn(psiFile);

        myFixture.checkResult(
            "import com.intellij.openapi.application.ApplicationManager;\n" +
                "import com.intellij.openapi.components.Service;\n" +
                "\n" +
                "@Service(Service.Level.APP)\n" +
                "public final class SomeService {\n" +
                "    public boolean someField;\n" +
                "\n" +
                "    public static SomeService getInstance() {\n" +
                "        return ApplicationManager.getApplication().getService(SomeService.class);\n" +
                "    }\n" +
                "\n" +
                "    public void someMethod() {\n" +
                "    }\n" +
                "}");
    }

    //Light service without level

    public void testShouldGenerateProjectLevelGetterForEmptyServiceLevelWithProperClassName() {
        PsiFile psiFile = myFixture.configureByText("SomeProjectService.java",
            "import com.intellij.openapi.components.Service;\n" +
                "\n" +
                "@Service\n" +
                "public final class SomeProjectService {\n" +
                "    public boolean someField;\n" +
                "<caret>\n" +
                "    public void someMethod() {\n" +
                "    }\n" +
                "}");

        runGenerateGetInstanceActionOn(psiFile);

        myFixture.checkResult(
            "import com.intellij.openapi.components.Service;\n" +
                "import com.intellij.openapi.project.Project;\n" +
                "\n" +
                "@Service\n" +
                "public final class SomeProjectService {\n" +
                "    public boolean someField;\n" +
                "\n" +
                "    public static SomeProjectService getInstance(Project project) {\n" +
                "        return project.getService(SomeProjectService.class);\n" +
                "    }\n" +
                "\n" +
                "    public void someMethod() {\n" +
                "    }\n" +
                "}");
    }

    public void testShouldGenerateProjectLevelGetterForNoServiceWithProperClassName() {
        PsiFile psiFile = myFixture.configureByText("SomeProjectService.java",
                "public final class SomeProjectService {\n" +
                "    public boolean someField;\n" +
                "<caret>\n" +
                "    public void someMethod() {\n" +
                "    }\n" +
                "}");

        runGenerateGetInstanceActionOn(psiFile);

        myFixture.checkResult(
                "import com.intellij.openapi.project.Project;\n" +
                "\n" +
                "public final class SomeProjectService {\n" +
                "    public boolean someField;\n" +
                "\n" +
                "    public static SomeProjectService getInstance(Project project) {\n" +
                "        return project.getService(SomeProjectService.class);\n" +
                "    }\n" +
                "\n" +
                "    public void someMethod() {\n" +
                "    }\n" +
                "}");
    }

    public void testShouldGenerateAppLevelGetterForEmptyServiceLevelWithProperClassName() {
        PsiFile psiFile = myFixture.configureByText("SomeApplicationService.java",
            "import com.intellij.openapi.components.Service;\n" +
                "\n" +
                "@Service\n" +
                "public final class SomeApplicationService {\n" +
                "    public boolean someField;\n" +
                "<caret>\n" +
                "    public void someMethod() {\n" +
                "    }\n" +
                "}");

        runGenerateGetInstanceActionOn(psiFile);

        myFixture.checkResult(
            "import com.intellij.openapi.application.ApplicationManager;\n" +
                "import com.intellij.openapi.components.Service;\n" +
                "\n" +
                "@Service\n" +
                "public final class SomeApplicationService {\n" +
                "    public boolean someField;\n" +
                "\n" +
                "    public static SomeApplicationService getInstance() {\n" +
                "        return ApplicationManager.getApplication().getService(SomeApplicationService.class);\n" +
                "    }\n" +
                "\n" +
                "    public void someMethod() {\n" +
                "    }\n" +
                "}");
    }

    public void testShouldGenerateAppLevelGetterForNoServiceWithProperClassName() {
        PsiFile psiFile = myFixture.configureByText("SomeApplicationService.java",
                "public final class SomeApplicationService {\n" +
                "    public boolean someField;\n" +
                "<caret>\n" +
                "    public void someMethod() {\n" +
                "    }\n" +
                "}");

        runGenerateGetInstanceActionOn(psiFile);

        myFixture.checkResult(
            "import com.intellij.openapi.application.ApplicationManager;\n" +
                "\n" +
                "public final class SomeApplicationService {\n" +
                "    public boolean someField;\n" +
                "\n" +
                "    public static SomeApplicationService getInstance() {\n" +
                "        return ApplicationManager.getApplication().getService(SomeApplicationService.class);\n" +
                "    }\n" +
                "\n" +
                "    public void someMethod() {\n" +
                "    }\n" +
                "}");
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
//        runGenerateGetInstanceActionOn(psiFile);
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

    //Other cases

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
//        runGenerateGetInstanceActionOn(psiFile);
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

//    public void testShouldGenerateGetterForNonServiceClass() {
//        PsiFile psiFile = myFixture.configureByText("NonService.java",
//            "public final class NonService {\n" +
//                "    public boolean someField;\n" +
//                "<caret>\n" +
//                "    public void someMethod() {\n" +
//                "    }\n" +
//                "}");
//
//        runGenerateGetInstanceActionOn(psiFile);
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

    private void runGenerateGetInstanceActionOn(PsiFile psiFile) {
        ReadAction.run(() ->
            CommandProcessor.getInstance()
                .executeCommand(getProject(), () -> new GenerateStaticGetInstanceAction().getHandler().invoke(getProject(), myFixture.getEditor(), psiFile),
                    "Action", ""));
    }
}
