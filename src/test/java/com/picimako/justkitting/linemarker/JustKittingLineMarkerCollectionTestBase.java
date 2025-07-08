//Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.picimako.justkitting.linemarker;

import static com.intellij.openapi.application.ReadAction.compute;
import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.psi.PsiElement;
import com.intellij.util.SmartList;
import com.picimako.justkitting.ContentRootsBasedJustKittingTestBase;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class JustKittingLineMarkerCollectionTestBase extends ContentRootsBasedJustKittingTestBase {

    public JustKittingLineMarkerCollectionTestBase() {
        super();
    }

    /**
     * This is used instead of just getting the line marker provider because
     * {@link com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider#collectNavigationMarkers(PsiElement, Collection)}
     * has protected access, so it couldn't be called from this class.
     */
    protected abstract BiConsumer<PsiElement, List<RelatedItemLineMarkerInfo<?>>> collectNavigationMarkers();

    protected abstract PsiElement getElementAtCaret();

    /**
     * Validates that there is one gutter icon in the tested file at the caret location.
     */
    protected void checkGutterIcon(String fileUnderTest, String lineMarkerToolTip) {
        var lineMarkers = getLineMarkers(fileUnderTest);

        assertThat(lineMarkers).hasSize(1);
        assertThat(compute(() -> lineMarkers.getFirst().getLineMarkerTooltip())).isEqualTo(lineMarkerToolTip);
    }

    /**
     * Validates that there is no gutter icon in the tested file at the caret location.
     */
    protected void checkNoGutterIcon(String fileUnderTest) {
        assertThat(getLineMarkers(fileUnderTest)).isEmpty();
    }

    protected List<RelatedItemLineMarkerInfo<?>> getLineMarkers(String fileUnderTest) {
        getFixture().configureFromTempProjectFile(fileUnderTest);

        var collection = new SmartList<RelatedItemLineMarkerInfo<?>>();
        collectNavigationMarkers().accept(getElementAtCaret(), collection);
        return collection;
    }
}
