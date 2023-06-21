//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.action.inspectionpanel;

import static org.assertj.core.api.Assertions.assertThat;

import com.picimako.justkitting.JustKittingTestBase;
import com.intellij.psi.PsiFile;

/**
 * Functional test for {@link CreateInspectionOptionsPanelAction}.
 */
public class CreateInspectionOptionsPanelActionTest extends JustKittingTestBase {

    public void testActionIsAvailableInsideEmptyCreateOptionsPanel() {
        PsiFile psiFile = myFixture.configureByText("SomeInspection.java",
            """
                import javax.swing.JComponent;
                import com.intellij.codeInspection.LocalInspectionTool;
                import org.jetbrains.annotations.Nullable;

                public class SomeInspection extends LocalInspectionTool {
                    @Override
                    public @Nullable JComponent createOptionsPanel() {
                        <caret>
                    }
                }""");

        assertThat(new CreateInspectionOptionsPanelAction().isValidForFile(getProject(), myFixture.getEditor(), psiFile)).isTrue();
    }

    public void testActionIsAvailableInsideNonEmptyCreateOptionsPanel() {
        PsiFile psiFile = myFixture.configureByText("SomeInspection.java",
            """
                import javax.swing.JComponent;
                import com.intellij.codeInspection.LocalInspectionTool;
                import org.jetbrains.annotations.Nullable;

                public class SomeInspection extends LocalInspectionTool {
                    @Override
                    public @Nullable JComponent createOptionsPanel() {
                        <caret>return super.createOptionsPanel();
                    }
                }""");

        assertThat(new CreateInspectionOptionsPanelAction().isValidForFile(getProject(), myFixture.getEditor(), psiFile)).isTrue();
    }

    public void testActionIsAvailableOnCreateOptionsPanel() {
        PsiFile psiFile = myFixture.configureByText("SomeInspection.java",
            """
                import javax.swing.JComponent;
                import com.intellij.codeInspection.LocalInspectionTool;
                import org.jetbrains.annotations.Nullable;

                public class SomeInspection extends LocalInspectionTool {
                    @Override
                    public @Nullable JComponent create<caret>OptionsPanel() {
                        return super.createOptionsPanel();
                    }
                }""");

        assertThat(new CreateInspectionOptionsPanelAction().isValidForFile(getProject(), myFixture.getEditor(), psiFile)).isTrue();
    }

    public void testActionIsNotAvailableWhenInsideCreateOptionsPanel() {
        PsiFile psiFile = myFixture.configureByText("SomeInspection.java",
            """
                import javax.swing.JComponent;
                import com.intellij.codeInspection.LocalInspectionTool;
                import org.jetbrains.annotations.Nullable;

                public class SomeInspection extends LocalInspectionTool {
                <caret>
                    @Override
                    public @Nullable JComponent createOptionsPanel() {
                        return super.createOptionsPanel();
                    }
                }""");

        assertThat(new CreateInspectionOptionsPanelAction().isValidForFile(getProject(), myFixture.getEditor(), psiFile)).isFalse();
    }
}
