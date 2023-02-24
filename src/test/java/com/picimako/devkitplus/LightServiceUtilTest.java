//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.devkitplus;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.psi.PsiClass;

/**
 * Unit test for {@link LightServiceUtil}.
 */
public class LightServiceUtilTest extends DevKitPlusTestBase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ThirdPartyLibraryLoader.loadUtil(myFixture);
    }

    public void testIsLightService() {
        myFixture.configureByText("LightService.java",
            "import com.intellij.openapi.components.Service;\n" +
                "\n" +
                "@Service(Service.Level.PROJECT)\n" +
                "public final class SomeProje<caret>ctService {\n" +
                "}");
        PsiClass psiClass = (PsiClass) myFixture.getFile().findElementAt(myFixture.getCaretOffset()).getParent();

        assertThat(LightServiceUtil.isLightService(psiClass)).isTrue();
    }

    public void testIsNotLightServiceDueToNullClass() {
        assertThat(LightServiceUtil.isLightService(null)).isFalse();
    }

    public void testIsNotLightServiceDueToNoAnnotation() {
        myFixture.configureByText("NotLightService.java",
            "public final class NotLight<caret>Service {\n" +
                "}");
        PsiClass psiClass = (PsiClass) myFixture.getFile().findElementAt(myFixture.getCaretOffset()).getParent();

        assertThat(LightServiceUtil.isLightService(psiClass)).isFalse();
    }
}
