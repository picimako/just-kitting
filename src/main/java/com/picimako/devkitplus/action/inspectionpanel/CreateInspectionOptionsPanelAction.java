//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.devkitplus.action.inspectionpanel;

import static com.intellij.psi.util.MethodSignatureUtil.areSignaturesEqual;
import static com.picimako.devkitplus.ListPopupHelper.showActionsInListPopup;
import static com.picimako.devkitplus.PlatformNames.INSPECTION_PROFILE_ENTRY;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.BaseCodeInsightAction;
import com.intellij.lang.LanguageCodeInsightActionHandler;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiSubstitutor;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.picimako.devkitplus.PlatformPsiCache;
import com.picimako.devkitplus.resources.DevKitPlusBundle;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Action to generate code snippets for various UI components available in the {@link com.intellij.codeInspection.ui} package for
 * {@link com.intellij.codeInspection.InspectionProfileEntry} implementations.
 *
 * @see CreatePanelOptionsActions.GenerateMultipleCheckboxOptionsPanel
 * @see CreatePanelOptionsActions.GenerateSingleCheckboxOptionsPanel
 * @see CreatePanelOptionsActions.GenerateSingleIntegerFieldOptionsPanel
 * @see CreatePanelOptionsActions.GenerateConventionOptionsPanel
 * @since 0.1.0
 */
public class CreateInspectionOptionsPanelAction extends BaseCodeInsightAction {
    private final GenerationOptionsPanelHandler handler = new GenerationOptionsPanelHandler();

    @Override
    protected @NotNull CodeInsightActionHandler getHandler() {
        return handler;
    }

    @Override
    protected boolean isValidForFile(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        return handler.isValidFor(editor, file);
    }

    private static final class GenerationOptionsPanelHandler implements LanguageCodeInsightActionHandler {
        private static final List<AnAction> ACTIONS = List.of(
            CreatePanelOptionsActions.GenerateMultipleCheckboxOptionsPanel.INSTANCE,
            CreatePanelOptionsActions.GenerateSingleCheckboxOptionsPanel.INSTANCE,
            CreatePanelOptionsActions.GenerateSingleIntegerFieldOptionsPanel.INSTANCE,
            CreatePanelOptionsActions.GenerateConventionOptionsPanel.INSTANCE
        );

        @Override
        public boolean isValidFor(Editor editor, PsiFile file) {
            PsiElement element = file.findElementAt(editor.getCaretModel().getOffset());
            PsiClass parentClass = PsiTreeUtil.getParentOfType(element, PsiClass.class);
            if (InheritanceUtil.isInheritor(parentClass, false, INSPECTION_PROFILE_ENTRY)) {
                PsiMethod parentMethod = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
                return parentMethod != null && areSignaturesEqual(parentMethod.getSignature(PsiSubstitutor.EMPTY), PlatformPsiCache.getInstance(file.getProject()).getCreateOptionsPanelMethod());
            }
            return false;
        }

        @Override
        public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
            showActionsInListPopup(DevKitPlusBundle.message("intention.ui.select.panel.type.title"), ACTIONS, editor);
        }

        @Override
        public boolean startInWriteAction() {
            return false;
        }
    }
}
