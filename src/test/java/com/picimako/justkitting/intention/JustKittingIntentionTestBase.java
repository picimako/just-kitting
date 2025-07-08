//Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.intention;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.psi.PsiFile;

import com.picimako.justkitting.JustKittingTestBase;

/**
 * Base test class for intention actions.
 */
public abstract class JustKittingIntentionTestBase extends JustKittingTestBase {

    protected void doIntentionTest(String filename, String beforeText, String afterText, IntentionAction intentionAction) {
        PsiFile psiFile = getFixture().configureByText(filename, beforeText);
        runIntentionOn(psiFile, intentionAction);
        getFixture().checkResult(afterText);
    }

    protected void doIntentionTest(String filename, String beforeText, String afterText) {
        doIntentionTest(filename, beforeText, afterText, getIntention());
    }

    protected void checkIfAvailableIn(PsiFile psiFile) {
        assertThat(getIntention().isAvailable(getProject(), getFixture().getEditor(), psiFile)).isTrue();
    }

    protected void checkIfNotAvailableIn(PsiFile psiFile) {
        assertThat(getIntention().isAvailable(getProject(), getFixture().getEditor(), psiFile)).isFalse();
    }

    protected IntentionAction getIntention() {
        return null;
    }

    protected void runIntentionOn(PsiFile psiFile, IntentionAction intentionAction) {
        ApplicationManager.getApplication().invokeAndWait(() ->
        //        ReadAction.run(() ->
            CommandProcessor.getInstance().executeCommand(getProject(),
                () -> intentionAction.invoke(getProject(), getFixture().getEditor(), psiFile), "Intention", ""));
    }
}
