//Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.inlayhint;

import static java.util.Comparator.comparing;

import com.intellij.codeInsight.hints.InlayPresentationFactory;
import com.intellij.codeInsight.hints.presentation.InlayPresentation;
import com.intellij.codeInsight.hints.presentation.PresentationFactory;
import com.intellij.ide.util.DelegatingPsiElementCellRenderer;
import com.intellij.ide.util.PsiElementRenderingInfo;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.xml.XmlToken;
import com.picimako.justkitting.resources.JustKittingBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.KtClass;

import java.util.List;
import java.util.function.Supplier;

/**
 * Provides `InlayPresentation`s for displaying inlay hints.
 */
@SuppressWarnings("UnstableApiUsage")
public abstract class LightServicesHintPresentationAware {

    public PresentationFactory presentationFactory;
    public Editor editor;
    public PsiFile file;

    LightServicesHintPresentationAware(PresentationFactory presentationFactory, Editor editor, PsiFile file) {
        this.presentationFactory = presentationFactory;
        this.editor = editor;
        this.file = file;
    }

    /**
     * A simple presentation with a label.
     */
    public InlayPresentation basePresentation(String label, int padding) {
        return presentationFactory.container(presentationFactory.smallText(label), new InlayPresentationFactory.Padding(padding, padding, padding, padding), null, null, 0);
    }

    public InlayPresentation basePresentation(String label) {
        return basePresentation(label, 1);
    }

    /**
     * Clickable hint showing the referenced class name. It navigates to the PsiClass when clicked.
     */
    public <T extends PsiNameIdentifierOwner> InlayPresentation classReferencePresentation(T psiClass) {
        return presentationFactory.referenceOnHover(
            presentationFactory.smallText(psiClass.getName() != null ? psiClass.getName() : ""),
            (mouseEvent, point) -> ((Navigatable) psiClass).navigate(true));
    }

    /**
     * Hint for showing all light services available in the project.
     * On click, it brings up a popup with the list of light service classes from where users can navigate to the corresponding classes.
     *
     * @param classes     the list of PsiClasses to populate the popup list with
     * @param startOffset the start offset of the `<extensions>` xml tag
     */
    public <T extends PsiNameIdentifierOwner> InlayPresentation viewAllServicesPresentation(Supplier<List<T>> classes, int startOffset) {
        return presentationFactory.referenceOnHover(presentationFactory.smallText(JustKittingBundle.message("inlay.hints.light.services.view.all.light.services")), (mouseEvent, point) -> {
            var step = new BaseListPopupStep<>(
                JustKittingBundle.message("inlay.hints.light.services.view.all.popup.title"),
                //Sorts the list items alphabetically by class names
                classes.get().stream().sorted(comparing(PsiNameIdentifierOwner::getName)).toList()) {
                @Override
                public @Nullable PopupStep<?> onChosen(T selectedValue, boolean finalChoice) {
                    if (selectedValue instanceof PsiClass || selectedValue instanceof KtClass) {
                        ((Navigatable) selectedValue).navigate(true);
                    }

                    return null;
                }
            };

            //Moving the caret to the beginning of the <extensions> tag, so that the popup list is displayed right at the element's inlay hint.
            editor.getCaretModel().moveToOffset(startOffset);
            JBPopupFactory.getInstance()
                //DelegatingPsiElementCellRenderer and ClassRenderingInfo replaces PsiClassListCellRenderer,
                // so that both PsiClasses and KtClasses can be rendered.
                .createListPopup(file.getProject(), step, renderer -> new DelegatingPsiElementCellRenderer<>(ClassRenderingInfo.INSTANCE))
                .showInBestPositionFor(editor);
        });
    }

    protected int calculateBlockInlayStartOffset(XmlToken element) {
        var width = EditorUtil.getPlainSpaceWidth(editor);
        var document = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
        var line = document.getLineNumber(element.getParent().getTextRange().getStartOffset());
        var startOffset = document.getLineStartOffset(line);
        return (element.getParent().getTextRange().getStartOffset() - startOffset) * width;
    }

    /**
     * Rendering info for various classes.
     */
    private static final class ClassRenderingInfo implements PsiElementRenderingInfo<PsiNameIdentifierOwner> {
        public static final ClassRenderingInfo INSTANCE = new ClassRenderingInfo();

        private ClassRenderingInfo() { }
        
        @Override
        public @NlsSafe @NotNull String getPresentableText(@NotNull PsiNameIdentifierOwner element) {
            return element.getName();
        }
    }
}
