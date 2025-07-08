//Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.intention.state;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.psi.PsiFile;
import com.picimako.justkitting.JustKittingTestBase;
import org.junit.jupiter.api.Test;

/**
 * Integration test for {@link MakeJavaClassPersistentStateComponentIntention}.
 */
public final class MakeJavaClassPersistentStateComponentIntentionTest extends JustKittingTestBase {

    //Not available

    @Test
    public void testIntentionIsNotAvailableWhenAlreadyPersistentStateComponent() {
        var psiFile = getFixture().configureByText("SomeComponent.java",
            """
                import com.intellij.openapi.components.PersistentStateComponent;
                
                public final class SomeCo<caret>mponent implements PersistentStateComponent<SomeComponent> {
                    @Override
                    public SomeComponent getState() {
                        return this;
                    }
                
                    @Override
                    public void loadState(SomeComponent state) {
                    }
                }
                """);

        assertThat(isIntentionAvailable(psiFile)).isFalse();
    }

    @Test
    public void testIntentionIsNotAvailableForInterface() {
        var psiFile = getFixture().configureByText("SomeComponent.java",
            """
                public interface SomeCo<caret>mponent {
                }""");

        assertThat(isIntentionAvailable(psiFile)).isFalse();
    }

    @Test
    public void testIntentionIsNotAvailableForEnum() {
        var psiFile = getFixture().configureByText("SomeComponent.java",
            """
                public enum SomeCo<caret>mponent {
                }""");

        assertThat(isIntentionAvailable(psiFile)).isFalse();
    }

    @Test
    public void testIntentionIsNotAvailableForAbstractClass() {
        var psiFile = getFixture().configureByText("SomeComponent.java",
            """
                public abstract class SomeCo<caret>mponent {
                }""");

        assertThat(isIntentionAvailable(psiFile)).isFalse();
    }

    @Test
    public void testIntentionIsNotAvailableWhenInvokedOnANonPsiClassElement() {
        var psiFile = getFixture().configureByText("SomeComponent.java",
            """
                public final class SomeComponent {
                    public String fie<caret>ld;
                }""");

        assertThat(isIntentionAvailable(psiFile)).isFalse();
    }

    //Available

    @Test
    public void testIntentionIsAvailableForClass() {
        var psiFile = getFixture().configureByText("SomeComponent.java",
            """
                public final class SomeComp<caret>onent {
                    public String field;
                }""");

        assertThat(isIntentionAvailable(psiFile)).isTrue();
    }

    //Helpers

    private boolean isIntentionAvailable(PsiFile psiFile) {
        return new MakeJavaClassPersistentStateComponentIntention().isAvailable(getProject(), getFixture().getEditor(), psiFile);
    }
}
