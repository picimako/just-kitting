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
            """
                import com.intellij.openapi.components.Service;

                @Service(Service.Level.PROJECT)
                public final class SomeService {
                }
                """);

        var serviceLevel = ServiceLevelDecider.getServiceLevel(psiFile.getClasses()[0]);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.PROJECT);

    }

    public void testApplicationServiceForAnnotation() {
        PsiJavaFile psiFile = (PsiJavaFile) myFixture.configureByText("SomeService.java",
            """
                import com.intellij.openapi.components.Service;

                @Service(Service.Level.APP)
                public final class SomeService {
                }
                """);

        var serviceLevel = ServiceLevelDecider.getServiceLevel(psiFile.getClasses()[0]);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.APP);
    }

    public void testProjectAndApplicationServiceForAnnotation() {
        PsiJavaFile psiFile = (PsiJavaFile) myFixture.configureByText("SomeService.java",
            """
                import com.intellij.openapi.components.Service;

                @Service({ Service.Level.PROJECT, Service.Level.APP })
                public final class SomeService {
                }
                """);

        var serviceLevel = ServiceLevelDecider.getServiceLevel(psiFile.getClasses()[0]);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.PROJECT_AND_APP);
    }

    //By class name

    public void testProjectServiceForClassName() {
        PsiJavaFile psiFile = (PsiJavaFile) myFixture.configureByText("AProjectService.java",
            """
                import com.intellij.openapi.components.Service;

                @Service
                public final class AProjectService {
                }
                """);

        var serviceLevel = ServiceLevelDecider.getServiceLevel(psiFile.getClasses()[0]);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.PROJECT);
    }

    public void testApplicationServiceForClassName() {
        PsiJavaFile psiFile = (PsiJavaFile) myFixture.configureByText("AnApplicationService.java",
            """
                import com.intellij.openapi.components.Service;

                @Service
                public final class AnApplicationService {
                }
                """);

        var serviceLevel = ServiceLevelDecider.getServiceLevel(psiFile.getClasses()[0]);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.APP);
    }

    //Not sure

    public void testNotSureWhatServiceLevel() {
        PsiJavaFile psiFile = (PsiJavaFile) myFixture.configureByText("SomeService.java",
            """
                import com.intellij.openapi.components.Service;

                @Service
                public final class SomeService {
                }
                """);

        var serviceLevel = ServiceLevelDecider.getServiceLevel(psiFile.getClasses()[0]);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.NOT_SURE);
    }
}
