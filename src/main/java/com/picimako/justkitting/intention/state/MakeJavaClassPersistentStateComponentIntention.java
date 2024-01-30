//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.intention.state;

import static com.picimako.justkitting.PlatformNames.PERSISTENT_STATE_COMPONENT;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.util.IncorrectOperationException;
import com.picimako.justkitting.ListPopupHelper;
import com.picimako.justkitting.resources.JustKittingBundle;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Converts a Java class to a {@link com.intellij.openapi.components.PersistentStateComponent} by implementing that interface
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
public class MakeJavaClassPersistentStateComponentIntention extends BaseIntentionAction {
    private static final List<AnAction> JAVA_ACTIONS = List.of(
        new JavaConversionActions.WithStandaloneStateObject(),
        new JavaConversionActions.WithSelfAsState());

    @Override
    public @IntentionName @NotNull String getText() {
        return JustKittingBundle.message("intention.convert.to.persistent.state.component.text");
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return JustKittingBundle.message("intention.convert.to.persistent.state.component.family", "Java");
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        final var element = file.findElementAt(editor.getCaretModel().getOffset());
        if (element instanceof PsiIdentifier && element.getParent() instanceof PsiClass parentClass) {
            return !parentClass.isInterface()
                   && !parentClass.hasModifierProperty(PsiModifier.ABSTRACT)
                   && !parentClass.isEnum()
                   && !InheritanceUtil.isInheritor(parentClass, true, PERSISTENT_STATE_COMPONENT);
        }

        return false;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        ListPopupHelper.showActionsInListPopup("", JAVA_ACTIONS, editor);
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }
}
