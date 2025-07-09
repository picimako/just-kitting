//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.linemarker;

import static com.intellij.openapi.application.ReadAction.compute;

import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor;
import com.intellij.lang.properties.psi.Property;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.junit.jupiter.api.Test;

/**
 * Integration test for {@link UpdateGradleVersionLineMarkerProvider}.
 */
public final class UpdateGradleVersionLineMarkerProviderDifferentGradleVersionTest extends JustKittingLineMarkerSingleTestBase {

    @Override
    protected String getTestDataPath() {
        return "src/test/testData/linemarker/updategradle/differentGradleVersion";
    }

    @Override
    protected PsiElement getElementAtCaret() {
        return compute(() -> PsiTreeUtil.getParentOfType(getFixture().getFile().findElementAt(getFixture().getCaretOffset()), Property.class));
    }
    @Override
    protected LineMarkerProviderDescriptor getLineMarkerProvider() {
        return new UpdateGradleVersionLineMarkerProvider();
    }

    @Test
    public void testGutterForDifferentGradleVersionProperty() {
        getFixture().copyFileToProject("gradle/wrapper/gradle-wrapper.properties");
        checkGutterIcon("gradle.properties", "Update Gradle Wrapper version");
    }
}