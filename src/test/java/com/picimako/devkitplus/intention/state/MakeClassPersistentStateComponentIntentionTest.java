//Copyright 2021 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.devkitplus.intention.state;

import static org.assertj.core.api.Assertions.assertThat;

import com.picimako.devkitplus.DevKitPlusTestBase;

import com.intellij.psi.PsiFile;

/**
 * Functional test for {@link MakeClassPersistentStateComponentIntention}.
 */
public class MakeClassPersistentStateComponentIntentionTest extends DevKitPlusTestBase {

    public void testIntentionIsNotAvailableWhenAlreadyPersistentStateComponent() {
        PsiFile psiFile = myFixture.configureByText("SomeComponent.java",
            "import com.intellij.openapi.components.PersistentStateComponent;\n" +
                "\n" +
                "public final class SomeCo<caret>mponent implements PersistentStateComponent<SomeComponent> {\n" +
                "    public SomeComponent getState() {\n" +
                "        return this;\n" +
                "    }\n" +
                "\n" +
                "    public void loadState(SomeComponent state) {\n" +
                "    }\n" +
                "}");

        assertThat(new MakeClassPersistentStateComponentIntention().isAvailable(getProject(), myFixture.getEditor(), psiFile)).isFalse();
    }

    public void testIntentionIsNotAvailableForInterface() {
        PsiFile psiFile = myFixture.configureByText("SomeComponent.java",
            "public interface SomeCo<caret>mponent {\n" +
                "}");

        assertThat(new MakeClassPersistentStateComponentIntention().isAvailable(getProject(), myFixture.getEditor(), psiFile)).isFalse();
    }

    public void testIntentionIsNotAvailableForEnum() {
        PsiFile psiFile = myFixture.configureByText("SomeComponent.java",
            "public enum SomeCo<caret>mponent {\n" +
                "}");

        assertThat(new MakeClassPersistentStateComponentIntention().isAvailable(getProject(), myFixture.getEditor(), psiFile)).isFalse();
    }

    public void testIntentionIsNotAvailableForAbstractClass() {
        PsiFile psiFile = myFixture.configureByText("SomeComponent.java",
            "public abstract class SomeCo<caret>mponent {\n" +
                "}");

        assertThat(new MakeClassPersistentStateComponentIntention().isAvailable(getProject(), myFixture.getEditor(), psiFile)).isFalse();
    }

    public void testIntentionIsNotAvailableWhenInvokedOnANonPsiClassElement() {
        PsiFile psiFile = myFixture.configureByText("SomeComponent.java",
            "public final class SomeComponent {\n" +
                "    public String fie<caret>ld;\n" +
                "}");

        assertThat(new MakeClassPersistentStateComponentIntention().isAvailable(getProject(), myFixture.getEditor(), psiFile)).isFalse();
    }
}
