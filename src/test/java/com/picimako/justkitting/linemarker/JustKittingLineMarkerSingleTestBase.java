//Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.picimako.justkitting.linemarker;

import static com.intellij.openapi.application.ReadAction.compute;
import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor;
import com.intellij.psi.PsiElement;
import com.picimako.justkitting.JustKittingTestBase;
import org.jetbrains.annotations.Nullable;

public abstract class JustKittingLineMarkerSingleTestBase extends JustKittingTestBase {

    public JustKittingLineMarkerSingleTestBase() {
        super();
    }

    protected abstract LineMarkerProviderDescriptor getLineMarkerProvider();

    protected abstract PsiElement getElementAtCaret();

    /**
     * Validates that there is one gutter icon in the tested file at the caret location.
     */
    protected void checkGutterIcon(String fileUnderTest, String lineMarkerToolTip) {
        var lineMarker = getLineMarker(fileUnderTest);

        assertThat(lineMarker).isNotNull();
        assertThat(compute(lineMarker::getLineMarkerTooltip)).isEqualTo(lineMarkerToolTip);
    }

    /**
     * Validates that there is no gutter icon in the tested file at the caret location.
     */
    protected void checkNoGutterIcon(String fileUnderTest) {
        assertThat(getLineMarker(fileUnderTest)).isNull();
    }

    @Nullable
    protected LineMarkerInfo<?> getLineMarker(String fileUnderTest) {
        getFixture().configureByFile(fileUnderTest);

        return getLineMarkerProvider().getLineMarkerInfo(getElementAtCaret());
    }
}
