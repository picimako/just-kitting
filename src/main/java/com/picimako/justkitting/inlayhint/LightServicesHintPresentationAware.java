//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.inlayhint;

import com.intellij.codeInsight.hints.InlayPresentationFactory;
import com.intellij.codeInsight.hints.presentation.InlayPresentation;
import com.intellij.codeInsight.hints.presentation.PresentationFactory;
import com.intellij.ide.util.PsiClassListCellRenderer;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlToken;
import com.picimako.justkitting.resources.JustKittingBundle;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * Provides `InlayPresentation`s for displaying inlay hints.
 */
@SuppressWarnings("UnstableApiUsage")
public abstract class LightServicesHintPresentationAware {

    public PresentationFactory factory;
    public Editor editor;
    public PsiFile file;

    LightServicesHintPresentationAware(PresentationFactory factory, Editor editor, PsiFile file) {
        this.factory = factory;
        this.editor = editor;
        this.file = file;
    }

    /**
     * A simple presentation with a label.
     */
    public InlayPresentation basePresentation(String label, int padding) {
        return factory.container(factory.smallText(label), new InlayPresentationFactory.Padding(padding, padding, padding, padding), null, null, 0);
    }

    public InlayPresentation basePresentation(String label) {
        return basePresentation(label, 1);
    }

    /**
     * Clickable hint showing the referenced class name. It navigates to the PsiClass when clicked.
     */
    public InlayPresentation classReferencePresentation(PsiClass psiClass) {
        return factory.referenceOnHover(
            factory.smallText(psiClass.getName() != null ? psiClass.getName() : ""),
            (mouseEvent, point) -> psiClass.navigate(true));
    }

    /**
     * Hint for showing all light services available in the project.
     * On click, it brings up a popup with the list of light service classes from where users can navigate to the corresponding classes.
     *
     * @param classes the list of PsiClasses to populate the popup list with
     * @param startOffset the start offset of the `<extensions>` xml tag
     */
    public InlayPresentation viewAllServicesPresentation(Supplier<List<PsiClass>> classes, int startOffset) {
        return factory.referenceOnHover(factory.smallText(JustKittingBundle.inlayHints("light.services.view.all.light.services")), (mouseEvent, point) -> {
            var step = new BaseListPopupStep<>(JustKittingBundle.inlayHints("light.services.view.all.popup.title"), classes.get()) {
                @Override
                public @Nullable PopupStep<?> onChosen(PsiClass selectedValue, boolean finalChoice) {
                    selectedValue.navigate(true);
                    return null;
                }
            };

            //Moving the caret to the beginning of the <extensions> tag, so that the popup list is displayed right at the element's inlay hint.
            editor.getCaretModel().moveToOffset(startOffset);
            JBPopupFactory.getInstance().createListPopup(file.getProject(), step, renderer -> new PsiClassListCellRenderer()).showInBestPositionFor(editor);
        });
    }

    protected int calculateBlockInlayStartOffset(XmlToken element) {
        var width = EditorUtil.getPlainSpaceWidth(editor);
        var document = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
        var line = document.getLineNumber(element.getParent().getTextRange().getStartOffset());
        var startOffset = document.getLineStartOffset(line);
        return (element.getParent().getTextRange().getStartOffset() - startOffset) * width;
    }
}
