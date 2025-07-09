//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.psi.PsiJavaFile;
import org.junit.jupiter.api.Test;

/**
 * Functional test for {@link ServiceLevelDecider}.
 */
public final class ServiceLevelDeciderJavaTest extends JustKittingTestBase {

    @Test
    public void testProjectServiceForAnnotation() {
        PsiJavaFile psiFile = (PsiJavaFile) getFixture().configureByText("SomeService.java",
            """
                import com.intellij.openapi.components.Service;

                @Service(Service.Level.PROJECT)
                public final class SomeService {
                }
                """);

        var serviceLevel = ServiceLevelUtil.getServiceLevel(psiFile);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.PROJECT);

    }

    @Test
    public void testApplicationServiceForAnnotation() {
        PsiJavaFile psiFile = (PsiJavaFile) getFixture().configureByText("SomeService.java",
            """
                import com.intellij.openapi.components.Service;

                @Service(Service.Level.APP)
                public final class SomeService {
                }
                """);

        var serviceLevel = ServiceLevelUtil.getServiceLevel(psiFile);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.APP);
    }

    @Test
    public void testProjectAndApplicationServiceForAnnotation() {
        PsiJavaFile psiFile = (PsiJavaFile) getFixture().configureByText("SomeService.java",
            """
                import com.intellij.openapi.components.Service;

                @Service({ Service.Level.PROJECT, Service.Level.APP })
                public final class SomeService {
                }
                """);

        var serviceLevel = ServiceLevelUtil.getServiceLevel(psiFile);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.PROJECT_AND_APP);
    }

    @Test
    public void testApplicationServiceForDefaultLevel() {
        PsiJavaFile psiFile = (PsiJavaFile) getFixture().configureByText("SomeService.java",
            """
                import com.intellij.openapi.components.Service;

                @Service
                public final class SomeService {
                }
                """);

        var serviceLevel = ServiceLevelUtil.getServiceLevel(psiFile);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.APP);
    }
}
