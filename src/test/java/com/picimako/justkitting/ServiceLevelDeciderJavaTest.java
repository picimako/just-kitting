//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.psi.PsiJavaFile;

/**
 * Functional test for {@link ServiceLevelDecider}.
 */
public class ServiceLevelDeciderJavaTest extends JustKittingTestBase {

    //By annotation

    public void testProjectServiceForAnnotation() {
        PsiJavaFile psiFile = (PsiJavaFile) myFixture.configureByText("SomeService.java",
            "import com.intellij.openapi.components.Service;\n" +
                "\n" +
                "@Service(Service.Level.PROJECT)\n" +
                "public final class SomeService {\n" +
                "}\n");

        var serviceLevel = ServiceLevelDecider.getServiceLevel(psiFile.getClasses()[0]);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.PROJECT);

    }

    public void testApplicationServiceForAnnotation() {
        PsiJavaFile psiFile = (PsiJavaFile) myFixture.configureByText("SomeService.java",
            "import com.intellij.openapi.components.Service;\n" +
                "\n" +
                "@Service(Service.Level.APP)\n" +
                "public final class SomeService {\n" +
                "}\n");

        var serviceLevel = ServiceLevelDecider.getServiceLevel(psiFile.getClasses()[0]);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.APP);
    }

    public void testProjectAndApplicationServiceForAnnotation() {
        PsiJavaFile psiFile = (PsiJavaFile) myFixture.configureByText("SomeService.java",
            "import com.intellij.openapi.components.Service;\n" +
                "\n" +
                "@Service({ Service.Level.PROJECT, Service.Level.APP })\n" +
                "public final class SomeService {\n" +
                "}\n");

        var serviceLevel = ServiceLevelDecider.getServiceLevel(psiFile.getClasses()[0]);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.PROJECT_AND_APP);
    }

    //By class name

    public void testProjectServiceForClassName() {
        PsiJavaFile psiFile = (PsiJavaFile) myFixture.configureByText("AProjectService.java",
            "import com.intellij.openapi.components.Service;\n" +
                "\n" +
                "@Service\n" +
                "public final class AProjectService {\n" +
                "}\n");

        var serviceLevel = ServiceLevelDecider.getServiceLevel(psiFile.getClasses()[0]);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.PROJECT);
    }

    public void testApplicationServiceForClassName() {
        PsiJavaFile psiFile = (PsiJavaFile) myFixture.configureByText("AnApplicationService.java",
            "import com.intellij.openapi.components.Service;\n" +
                "\n" +
                "@Service\n" +
                "public final class AnApplicationService {\n" +
                "}\n");

        var serviceLevel = ServiceLevelDecider.getServiceLevel(psiFile.getClasses()[0]);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.APP);
    }

    //Not sure

    public void testNotSureWhatServiceLevel() {
        PsiJavaFile psiFile = (PsiJavaFile) myFixture.configureByText("SomeService.java",
            "import com.intellij.openapi.components.Service;\n" +
                "\n" +
                "@Service\n" +
                "public final class SomeService {\n" +
                "}\n");

        var serviceLevel = ServiceLevelDecider.getServiceLevel(psiFile.getClasses()[0]);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.NOT_SURE);
    }
}
