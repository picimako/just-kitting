//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.intention.state;

import static org.assertj.core.api.Assertions.assertThat;

import com.picimako.justkitting.JustKittingTestBase;
import com.picimako.justkitting.ThirdPartyLibraryLoader;

/**
 * Integration test for {@link MakeJavaClassPersistentStateComponentIntention}.
 */
public class MakeJavaClassPersistentStateComponentIntentionTest extends JustKittingTestBase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ThirdPartyLibraryLoader.loadAppClient(myFixture);
    }

    //Not available

    public void testIntentionIsNotAvailableWhenAlreadyPersistentStateComponent() {
        var psiFile = myFixture.configureByText("SomeComponent.java",
            """
                import com.intellij.openapi.components.PersistentStateComponent;

                public final class SomeCo<caret>mponent implements PersistentStateComponent<SomeComponent> {
                    public SomeComponent getState() {
                        return this;
                    }

                    public void loadState(SomeComponent state) {
                    }
                }""");

        assertThat(new MakeJavaClassPersistentStateComponentIntention().isAvailable(getProject(), myFixture.getEditor(), psiFile)).isFalse();
    }

    public void testIntentionIsNotAvailableForInterface() {
        var psiFile = myFixture.configureByText("SomeComponent.java",
            "public interface SomeCo<caret>mponent {\n" +
                "}");

        assertThat(new MakeJavaClassPersistentStateComponentIntention().isAvailable(getProject(), myFixture.getEditor(), psiFile)).isFalse();
    }

    public void testIntentionIsNotAvailableForEnum() {
        var psiFile = myFixture.configureByText("SomeComponent.java",
            "public enum SomeCo<caret>mponent {\n" +
                "}");

        assertThat(new MakeJavaClassPersistentStateComponentIntention().isAvailable(getProject(), myFixture.getEditor(), psiFile)).isFalse();
    }

    public void testIntentionIsNotAvailableForAbstractClass() {
        var psiFile = myFixture.configureByText("SomeComponent.java",
            "public abstract class SomeCo<caret>mponent {\n" +
                "}");

        assertThat(new MakeJavaClassPersistentStateComponentIntention().isAvailable(getProject(), myFixture.getEditor(), psiFile)).isFalse();
    }

    public void testIntentionIsNotAvailableWhenInvokedOnANonPsiClassElement() {
        var psiFile = myFixture.configureByText("SomeComponent.java",
            """
                public final class SomeComponent {
                    public String fie<caret>ld;
                }""");

        assertThat(new MakeJavaClassPersistentStateComponentIntention().isAvailable(getProject(), myFixture.getEditor(), psiFile)).isFalse();
    }

    //Available

    public void testIntentionIsAvailableForClass() {
        var psiFile = myFixture.configureByText("SomeComponent.java",
            """
                public final class SomeComp<caret>onent {
                    public String field;
                }""");

        assertThat(new MakeJavaClassPersistentStateComponentIntention().isAvailable(getProject(), myFixture.getEditor(), psiFile)).isTrue();
    }
}
