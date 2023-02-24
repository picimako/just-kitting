//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.devkitplus.action;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.picimako.devkitplus.DevKitPlusTestBase;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.psi.PsiFile;

/**
 * Functional test for {@link GenerateLightServiceStaticGetInstanceAction}.
 */
public class GenerateLightServiceStaticGetInstanceActionTest extends DevKitPlusTestBase {

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

    public void testShouldShowMessageForEmptyServiceLevelWithNoProperClassName() {
        PsiFile psiFile = myFixture.configureByText("SomeService.java",
            "import com.intellij.openapi.components.Service;\n" +
                "\n" +
                "@Service\n" +
                "public final class SomeService {\n" +
                "    public boolean someField;\n" +
                "<caret>\n" +
                "    public void someMethod() {\n" +
                "    }\n" +
                "}");

        assertThatThrownBy(() -> runGenerateGetInstanceActionOn(psiFile))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Could not determine the service level based on the @Service annotation and the class name.");
    }

    private void runGenerateGetInstanceActionOn(PsiFile psiFile) {
        ReadAction.run(() ->
            CommandProcessor.getInstance()
                .executeCommand(getProject(), () -> new GenerateLightServiceStaticGetInstanceAction().getHandler().invoke(getProject(), myFixture.getEditor(), psiFile),
                    "Action", ""));
    }
}
