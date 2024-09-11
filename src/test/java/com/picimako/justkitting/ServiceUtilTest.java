//Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.psi.PsiClass;

/**
 * Unit test for {@link ServiceUtil}.
 */
public class ServiceUtilTest extends JustKittingTestBase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ThirdPartyLibraryLoader.loadUtil8(myFixture);
    }

    public void testIsLightService() {
        myFixture.configureByText("LightService.java",
            """
                import com.intellij.openapi.components.Service;

                @Service(Service.Level.PROJECT)
                public final class SomeProje<caret>ctService {
                }""");
        PsiClass psiClass = (PsiClass) myFixture.getFile().findElementAt(myFixture.getCaretOffset()).getParent();

        assertThat(ServiceUtil.isLightService(psiClass)).isTrue();
    }

    public void testIsNotLightServiceDueToNullClass() {
        assertThat(ServiceUtil.isLightService(null)).isFalse();
    }

    public void testIsNotLightServiceDueToNoAnnotation() {
        myFixture.configureByText("NotLightService.java",
            "public final class NotLight<caret>Service {\n" +
                "}");
        PsiClass psiClass = (PsiClass) myFixture.getFile().findElementAt(myFixture.getCaretOffset()).getParent();

        assertThat(ServiceUtil.isLightService(psiClass)).isFalse();
    }
}
