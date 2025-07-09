//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting;

import static com.intellij.openapi.application.ReadAction.compute;
import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.psi.PsiClass;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link ServiceUtil}.
 */
public final class ServiceUtilTest extends JustKittingTestBase {

    @Test
    public void testIsLightService() {
        getFixture().configureByText("LightService.java",
            """
                import com.intellij.openapi.components.Service;

                @Service(Service.Level.PROJECT)
                public final class SomeProje<caret>ctService {
                }""");
        PsiClass psiClass = (PsiClass) compute(() -> getFixture().getFile().findElementAt(getFixture().getCaretOffset()).getParent());

        assertThat(ServiceUtil.isLightService(psiClass)).isTrue();
    }

    @Test
    public void testIsNotLightServiceDueToNullClass() {
        assertThat(ServiceUtil.isLightService(null)).isFalse();
    }

    @Test
    public void testIsNotLightServiceDueToNoAnnotation() {
        getFixture().configureByText("NotLightService.java",
            "public final class NotLight<caret>Service {\n" +
                "}");
        PsiClass psiClass = (PsiClass) compute(() -> getFixture().getFile().findElementAt(getFixture().getCaretOffset()).getParent());

        assertThat(ServiceUtil.isLightService(psiClass)).isFalse();
    }
}
