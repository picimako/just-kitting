//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.intention.state;

import static org.assertj.core.api.Assertions.assertThat;

import com.picimako.justkitting.JustKittingTestBase;

import com.intellij.psi.PsiFile;

/**
 * Functional test for {@link MakeClassPersistentStateComponentIntention}.
 */
public class MakeClassPersistentStateComponentIntentionTest extends JustKittingTestBase {

    public void testIntentionIsNotAvailableWhenAlreadyPersistentStateComponent() {
        PsiFile psiFile = myFixture.configureByText("SomeComponent.java",
            """
                import com.intellij.openapi.components.PersistentStateComponent;

                public final class SomeCo<caret>mponent implements PersistentStateComponent<SomeComponent> {
                    public SomeComponent getState() {
                        return this;
                    }

                    public void loadState(SomeComponent state) {
                    }
                }""");

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
            """
                public final class SomeComponent {
                    public String fie<caret>ld;
                }""");

        assertThat(new MakeClassPersistentStateComponentIntention().isAvailable(getProject(), myFixture.getEditor(), psiFile)).isFalse();
    }
}
