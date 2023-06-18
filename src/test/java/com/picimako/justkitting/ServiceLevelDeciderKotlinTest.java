//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting;

import org.jetbrains.kotlin.psi.KtFile;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Functional test for {@link ServiceLevelDecider}.
 */
public class ServiceLevelDeciderKotlinTest extends JustKittingTestBase {

    //By annotation

    public void testProjectServiceForAnnotation() {
        KtFile psiFile = (KtFile) myFixture.configureByText("SomeService.kt",
            "import com.intellij.openapi.components.Service\n" +
                "\n" +
                "@Service(Service.Level.PROJECT)\n" +
                "class SomeService {\n" +
                "}\n");

        var serviceLevel = ServiceLevelDecider.getServiceLevel(psiFile.getClasses()[0]);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.PROJECT);

    }

    public void testApplicationServiceForAnnotation() {
        KtFile psiFile = (KtFile) myFixture.configureByText("SomeService.kt",
            "import com.intellij.openapi.components.Service\n" +
                "\n" +
                "@Service(Service.Level.APP)\n" +
                "class SomeService {\n" +
                "}\n");

        var serviceLevel = ServiceLevelDecider.getServiceLevel(psiFile.getClasses()[0]);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.APP);
    }

    public void testProjectAndApplicationServiceForAnnotation() {
        KtFile psiFile = (KtFile) myFixture.configureByText("SomeService.kt",
            "import com.intellij.openapi.components.Service\n" +
                "\n" +
                "@Service(Service.Level.PROJECT, Service.Level.APP)\n" +
                "class SomeService {\n" +
                "}\n");

        var serviceLevel = ServiceLevelDecider.getServiceLevel(psiFile.getClasses()[0]);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.PROJECT_AND_APP);
    }

    public void testProjectAndApplicationServiceForAnnotationServiceLevelImported() {
        KtFile psiFile = (KtFile) myFixture.configureByText("SomeService.kt",
            "import com.intellij.openapi.components.Service\n" +
                "import com.intellij.openapi.components.Service.Level\n" +
                "\n" +
                "@Service(Level.PROJECT, Level.APP)\n" +
                "class SomeService {\n" +
                "}\n");

        var serviceLevel = ServiceLevelDecider.getServiceLevel(psiFile.getClasses()[0]);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.PROJECT_AND_APP);
    }

    public void testProjectAndApplicationServiceForAnnotationServiceLevelsImported() {
        KtFile psiFile = (KtFile) myFixture.configureByText("SomeService.kt",
            "import com.intellij.openapi.components.Service\n" +
                "import com.intellij.openapi.components.Service.Level.PROJECT\n" +
                "import com.intellij.openapi.components.Service.Level.APP\n" +
                "\n" +
                "@Service(PROJECT, APP)\n" +
                "class SomeService {\n" +
                "}\n");

        var serviceLevel = ServiceLevelDecider.getServiceLevel(psiFile.getClasses()[0]);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.PROJECT_AND_APP);
    }

    //By class name

    public void testProjectServiceForClassName() {
        KtFile psiFile = (KtFile) myFixture.configureByText("AProjectService.kt",
            "import com.intellij.openapi.components.Service\n" +
                "\n" +
                "@Service\n" +
                "class AProjectService {\n" +
                "}\n");

        var serviceLevel = ServiceLevelDecider.getServiceLevel(psiFile.getClasses()[0]);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.PROJECT);
    }

    public void testApplicationServiceForClassName() {
        KtFile psiFile = (KtFile) myFixture.configureByText("AnApplicationService.kt",
            "import com.intellij.openapi.components.Service\n" +
                "\n" +
                "@Service\n" +
                "class AnApplicationService {\n" +
                "}\n");

        var serviceLevel = ServiceLevelDecider.getServiceLevel(psiFile.getClasses()[0]);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.APP);
    }

    //Not sure

    public void testNotSureWhatServiceLevel() {
        KtFile psiFile = (KtFile) myFixture.configureByText("SomeService.kt",
            "import com.intellij.openapi.components.Service\n" +
                "\n" +
                "@Service\n" +
                "class SomeService {\n" +
                "}\n");

        var serviceLevel = ServiceLevelDecider.getServiceLevel(psiFile.getClasses()[0]);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.NOT_SURE);
    }
}
