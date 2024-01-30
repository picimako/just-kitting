//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.intention.state;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import com.picimako.justkitting.ListPopupHelper;
import com.picimako.justkitting.resources.JustKittingBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.lexer.KtTokens;
import org.jetbrains.kotlin.psi.KtClass;

import java.util.List;

/**
 * Converts a Kotlin class to a {@link com.intellij.openapi.components.PersistentStateComponent} by implementing that interface
 * and generating a simple implementation for its methods.
 * <p>
 * There are two options now, based on the
 * <a href="https://plugins.jetbrains.com/docs/intellij/persisting-state-of-components.html#implementing-the-persistentstatecomponent-interface">
 * Plugin SDK > Persisting State of Components > Implementing the PersistentStateComponent Interface</a> document:
 * <ul>
 *     <li>use a standalone inner class for providing the state for the component</li>
 *     <li>use the class itself as the state object</li>
 * </ul>
 * <p>
 * For now, only non-abstract classes are supported.
 *
 * @see JavaConversionActions
 * @since 0.1.0
 */
public class MakeKotlinClassPersistentStateComponentIntention extends BaseIntentionAction {
    private static final List<AnAction> KOTLIN_ACTIONS = List.of(
        new KotlinConversionActions.WithStandaloneStateObject(),
        new KotlinConversionActions.WithSelfAsState());

    @Override
    public @IntentionName @NotNull String getText() {
        return JustKittingBundle.message("intention.convert.to.persistent.state.component.text");
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return JustKittingBundle.message("intention.convert.to.persistent.state.component.family", "Kotlin");
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        final var element = file.findElementAt(editor.getCaretModel().getOffset());
        if (element != null && element.getNode().getElementType() == KtTokens.IDENTIFIER && element.getParent() instanceof KtClass parentClass) {
            return !parentClass.isInterface()
                   && !parentClass.isEnum()
                   && !parentClass.hasModifier(KtTokens.ABSTRACT_KEYWORD)
                   && !parentClass.isValue()
                   && parentClass.getSuperTypeListEntries().stream()
                       .noneMatch(entry -> entry.getTypeAsUserType() != null && "PersistentStateComponent".equals(entry.getTypeAsUserType().getReferencedName()));
        }

        return false;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        ListPopupHelper.showActionsInListPopup("", KOTLIN_ACTIONS, editor);
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }
}
