//Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.picimako.justkitting.linemarker;

import static com.intellij.openapi.application.ReadAction.compute;

import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor;
import com.intellij.lang.properties.psi.Property;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * Integration test for {@link UpdateGradleVersionLineMarkerProvider}.
 */
public class UpdateGradleVersionLineMarkerProviderDifferentGradleVersionTest extends JustKittingLineMarkerSingleTestBase {

    @Override
    protected String getTestDataPath() {
        return "src/test/testData/linemarker/updategradle/differentGradleVersion";
    }

    @Override
    protected PsiElement getElementAtCaret() {
        return compute(() -> PsiTreeUtil.getParentOfType(getFile().findElementAt(myFixture.getCaretOffset()), Property.class));
    }
    @Override
    protected LineMarkerProviderDescriptor getLineMarkerProvider() {
        return new UpdateGradleVersionLineMarkerProvider();
    }

    public void testGutterForDifferentGradleVersionProperty() {
        myFixture.copyFileToProject("gradle/wrapper/gradle-wrapper.properties");
        checkGutterIcon("gradle.properties", "Update Gradle Wrapper version");
    }
}