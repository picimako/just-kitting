//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.intention.callmatcher;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.designer.clipboard.SimpleTransferable;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.intellij.util.IncorrectOperationException;
import com.picimako.justkitting.resources.JustKittingBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.datatransfer.DataFlavor;

/**
 * This intention generates a {@link com.siyeh.ig.callMatcher.CallMatcher} initializer call
 * based on the Java class method it is invoked on.
 * <p>
 * The generated code is copied to the clipboard, so after pasting it, {@code CallMatcher} has
 * to be imported manually.
 * <p>
 * Kotlin or other JVM language methods are not supported yet.
 *
 * @since 0.1.0
 */
public class GenerateCallMatcherFromSignatureIntention implements IntentionAction {
    @Override
    public @IntentionName @NotNull String getText() {
        return JustKittingBundle.message("intention.call.matcher.generate.from.signature");
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return JustKittingBundle.message("intention.call.matcher.generate.from.signature");
    }

    //---- Availability check ----

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        if (!file.getFileType().equals(JavaFileType.INSTANCE)) return false;

        return getMethodSignatureAtCaretOrEmpty(file, editor) != null;
    }

    @Nullable
    public static PsiMethod getMethodSignatureAtCaretOrEmpty(PsiFile file, Editor editor) {
        var elementAtCaret = file.findElementAt(editor.getCaretModel().getOffset());
        return elementAtCaret instanceof PsiIdentifier && elementAtCaret.getParent() instanceof PsiMethod
            ? (PsiMethod) elementAtCaret.getParent()
            : null;
    }

    //---- Invocation ----

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        var targetMethod = (PsiMethod) file.findElementAt(editor.getCaretModel().getOffset()).getParent();
        new JavaCallMatcherGenerator(project, editor)
            .generateCallMatcher(targetMethod,
                callMatcher -> CopyPasteManager.getInstance().setContents(new SimpleTransferable(callMatcher, DataFlavor.stringFlavor)));
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }
}
