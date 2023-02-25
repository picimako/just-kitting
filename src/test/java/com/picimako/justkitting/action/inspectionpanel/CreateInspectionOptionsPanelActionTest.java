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
            "import javax.swing.JComponent;\n" +
                "import com.intellij.codeInspection.LocalInspectionTool;\n" +
                "import org.jetbrains.annotations.Nullable;\n" +
                "\n" +
                "public class SomeInspection extends LocalInspectionTool {\n" +
                "    @Override\n" +
                "    public @Nullable JComponent createOptionsPanel() {\n" +
                "        <caret>\n" +
                "    }\n" +
                "}");

        assertThat(new CreateInspectionOptionsPanelAction().isValidForFile(getProject(), myFixture.getEditor(), psiFile)).isTrue();
    }

    public void testActionIsAvailableInsideNonEmptyCreateOptionsPanel() {
        PsiFile psiFile = myFixture.configureByText("SomeInspection.java",
            "import javax.swing.JComponent;\n" +
                "import com.intellij.codeInspection.LocalInspectionTool;\n" +
                "import org.jetbrains.annotations.Nullable;\n" +
                "\n" +
                "public class SomeInspection extends LocalInspectionTool {\n" +
                "    @Override\n" +
                "    public @Nullable JComponent createOptionsPanel() {\n" +
                "        <caret>return super.createOptionsPanel();\n" +
                "    }\n" +
                "}");

        assertThat(new CreateInspectionOptionsPanelAction().isValidForFile(getProject(), myFixture.getEditor(), psiFile)).isTrue();
    }

    public void testActionIsAvailableOnCreateOptionsPanel() {
        PsiFile psiFile = myFixture.configureByText("SomeInspection.java",
            "import javax.swing.JComponent;\n" +
                "import com.intellij.codeInspection.LocalInspectionTool;\n" +
                "import org.jetbrains.annotations.Nullable;\n" +
                "\n" +
                "public class SomeInspection extends LocalInspectionTool {\n" +
                "    @Override\n" +
                "    public @Nullable JComponent create<caret>OptionsPanel() {\n" +
                "        return super.createOptionsPanel();\n" +
                "    }\n" +
                "}");

        assertThat(new CreateInspectionOptionsPanelAction().isValidForFile(getProject(), myFixture.getEditor(), psiFile)).isTrue();
    }

    public void testActionIsNotAvailableWhenInsideCreateOptionsPanel() {
        PsiFile psiFile = myFixture.configureByText("SomeInspection.java",
            "import javax.swing.JComponent;\n" +
                "import com.intellij.codeInspection.LocalInspectionTool;\n" +
                "import org.jetbrains.annotations.Nullable;\n" +
                "\n" +
                "public class SomeInspection extends LocalInspectionTool {\n" +
                "<caret>\n" +
                "    @Override\n" +
                "    public @Nullable JComponent createOptionsPanel() {\n" +
                "        return super.createOptionsPanel();\n" +
                "    }\n" +
                "}");

        assertThat(new CreateInspectionOptionsPanelAction().isValidForFile(getProject(), myFixture.getEditor(), psiFile)).isFalse();
    }
}
