//Copyright 2021 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.devkitplus.intention;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.psi.PsiFile;

import com.picimako.devkitplus.DevKitPlusTestBase;

/**
 * Base test class for intention actions.
 */
public abstract class DevKitPlusIntentionTestBase extends DevKitPlusTestBase {

    protected void doIntentionTest(String filename, String beforeText, String afterText, IntentionAction intentionAction) {
        PsiFile psiFile = myFixture.configureByText(filename, beforeText);
        runIntentionOn(psiFile, intentionAction);
        myFixture.checkResult(afterText);
    }

    protected void doIntentionTest(String filename, String beforeText, String afterText) {
        doIntentionTest(filename, beforeText, afterText, getIntention());
    }

    protected void checkIfNotAvailableIn(PsiFile psiFile) {
        assertThat(getIntention().isAvailable(getProject(), myFixture.getEditor(), psiFile)).isFalse();
    }

    protected IntentionAction getIntention() {
        return null;
    }

    private void runIntentionOn(PsiFile psiFile, IntentionAction intentionAction) {
        ReadAction.run(() ->
            CommandProcessor.getInstance().executeCommand(getProject(),
                () -> intentionAction.invoke(getProject(), myFixture.getEditor(), psiFile), "Intention", ""));
    }
}
