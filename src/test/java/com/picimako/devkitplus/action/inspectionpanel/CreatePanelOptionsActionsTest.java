//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.devkitplus.action.inspectionpanel;

import com.picimako.devkitplus.action.DevKitPlusActionTestBase;

/**
 * Functional test for {@link CreatePanelOptionsActions}.
 */
public class CreatePanelOptionsActionsTest extends DevKitPlusActionTestBase {

    //MultipleCheckboxOptionsPanel

    public void testReplacesEmptyCreateOptionsPanelWithMultipleCheckboxOptionsPanel() {
        checkAction("SomeInspection.java", () -> new CreatePanelOptionsActions.GenerateMultipleCheckboxOptionsPanel(3),
            "import javax.swing.JComponent;\n" +
                "import com.intellij.codeInspection.LocalInspectionTool;\n" +
                "import org.jetbrains.annotations.Nullable;\n" +
                "\n" +
                "public class SomeInspection extends LocalInspectionTool {\n" +
                "    @Override\n" +
                "    public @Nullable JComponent createOptionsPanel() {\n" +
                "        <caret>\n" +
                "    }\n" +
                "}",
            "import javax.swing.JComponent;\n" +
                "import com.intellij.codeInspection.LocalInspectionTool;\n" +
                "import com.intellij.codeInspection.ui.MultipleCheckboxOptionsPanel;\n" +
                "import org.jetbrains.annotations.Nullable;\n" +
                "\n" +
                "public class SomeInspection extends LocalInspectionTool {\n" +
                "    @Override\n" +
                "    public @Nullable JComponent createOptionsPanel() {\n" +
                "        MultipleCheckboxOptionsPanel panel = new MultipleCheckboxOptionsPanel(this);\n" +
                "        panel.addCheckbox(\"\", \"\");\n" +
                "        panel.addCheckbox(\"\", \"\");\n" +
                "        panel.addCheckbox(\"\", \"\");\n" +
                "        return panel;\n" +
                "    }\n" +
                "}");
    }

    public void testReplacesNonEmptyCreateOptionsPanelWithMultipleCheckboxOptionsPanel() {
        checkAction("SomeInspection.java", () -> new CreatePanelOptionsActions.GenerateMultipleCheckboxOptionsPanel(4),
            "import javax.swing.*;\n" +
                "import com.intellij.codeInspection.LocalInspectionTool;\n" +
                "\n" +
                "public class SomeInspection extends LocalInspectionTool {\n" +
                "    @Override\n" +
                "    public @Nullable JComponent createOptionsPanel() {\n" +
                "        <caret>return super.createOptionsPanel();\n" +
                "    }\n" +
                "}",
            "import javax.swing.*;\n" +
                "import com.intellij.codeInspection.LocalInspectionTool;\n" +
                "import com.intellij.codeInspection.ui.MultipleCheckboxOptionsPanel;\n" +
                "\n" +
                "public class SomeInspection extends LocalInspectionTool {\n" +
                "    @Override\n" +
                "    public @Nullable JComponent createOptionsPanel() {\n" +
                "        MultipleCheckboxOptionsPanel panel = new MultipleCheckboxOptionsPanel(this);\n" +
                "        panel.addCheckbox(\"\", \"\");\n" +
                "        panel.addCheckbox(\"\", \"\");\n" +
                "        panel.addCheckbox(\"\", \"\");\n" +
                "        panel.addCheckbox(\"\", \"\");\n" +
                "        return panel;\n" +
                "    }\n" +
                "}");
    }

    //SingleCheckboxOptionsPanel

    public void testReplacesEmptyCreateOptionsPanelWithSingleCheckboxOptionsPanel() {
        checkAction("SomeInspection.java", CreatePanelOptionsActions.GenerateSingleCheckboxOptionsPanel::new,
            "import javax.swing.JComponent;\n" +
                "import com.intellij.codeInspection.LocalInspectionTool;\n" +
                "import org.jetbrains.annotations.Nullable;\n" +
                "\n" +
                "public class SomeInspection extends LocalInspectionTool {\n" +
                "    @Override\n" +
                "    public @Nullable JComponent createOptionsPanel() {\n" +
                "        <caret>\n" +
                "    }\n" +
                "}",
            "import javax.swing.JComponent;\n" +
                "import com.intellij.codeInspection.LocalInspectionTool;\n" +
                "import com.intellij.codeInspection.ui.SingleCheckboxOptionsPanel;\n" +
                "import org.jetbrains.annotations.Nullable;\n" +
                "\n" +
                "public class SomeInspection extends LocalInspectionTool {\n" +
                "    @Override\n" +
                "    public @Nullable JComponent createOptionsPanel() {\n" +
                "        return new SingleCheckboxOptionsPanel(\"\", this, \"\");\n" +
                "    }\n" +
                "}");
    }

    public void testReplacesNonEmptyCreateOptionsPanelWithSingleCheckboxOptionsPanel() {
        checkAction("SomeInspection.java", CreatePanelOptionsActions.GenerateSingleCheckboxOptionsPanel::new,
            "import javax.swing.*;\n" +
                "import com.intellij.codeInspection.LocalInspectionTool;\n" +
                "\n" +
                "public class SomeInspection extends LocalInspectionTool {\n" +
                "    @Override\n" +
                "    public @Nullable JComponent createOptionsPanel() {\n" +
                "        <caret>return super.createOptionsPanel();\n" +
                "    }\n" +
                "}",
            "import javax.swing.*;\n" +
                "import com.intellij.codeInspection.LocalInspectionTool;\n" +
                "import com.intellij.codeInspection.ui.SingleCheckboxOptionsPanel;\n" +
                "\n" +
                "public class SomeInspection extends LocalInspectionTool {\n" +
                "    @Override\n" +
                "    public @Nullable JComponent createOptionsPanel() {\n" +
                "        return new SingleCheckboxOptionsPanel(\"\", this, \"\");\n" +
                "    }\n" +
                "}");
    }

    //SingleIntegerFieldOptionsPanel

    public void testReplacesEmptyCreateOptionsPanelWithSingleIntegerFieldOptionsPanel() {
        checkAction("SomeInspection.java", CreatePanelOptionsActions.GenerateSingleIntegerFieldOptionsPanel::new,
            "import javax.swing.JComponent;\n" +
                "import com.intellij.codeInspection.LocalInspectionTool;\n" +
                "import org.jetbrains.annotations.Nullable;\n" +
                "\n" +
                "public class SomeInspection extends LocalInspectionTool {\n" +
                "    @Override\n" +
                "    public @Nullable JComponent createOptionsPanel() {\n" +
                "        <caret>\n" +
                "    }\n" +
                "}",
            "import javax.swing.JComponent;\n" +
                "import com.intellij.codeInspection.LocalInspectionTool;\n" +
                "import com.intellij.codeInspection.ui.SingleIntegerFieldOptionsPanel;\n" +
                "import org.jetbrains.annotations.Nullable;\n" +
                "\n" +
                "public class SomeInspection extends LocalInspectionTool {\n" +
                "    @Override\n" +
                "    public @Nullable JComponent createOptionsPanel() {\n" +
                "        return new SingleIntegerFieldOptionsPanel(\"\", this, \"\");\n" +
                "    }\n" +
                "}");
    }

    public void testReplacesNonEmptyCreateOptionsPanelWithSingleIntegerFieldOptionsPanel() {
        checkAction("SomeInspection.java", CreatePanelOptionsActions.GenerateSingleIntegerFieldOptionsPanel::new,
            "import javax.swing.*;\n" +
                "import com.intellij.codeInspection.LocalInspectionTool;\n" +
                "\n" +
                "public class SomeInspection extends LocalInspectionTool {\n" +
                "    @Override\n" +
                "    public @Nullable JComponent createOptionsPanel() {\n" +
                "        <caret>return super.createOptionsPanel();\n" +
                "    }\n" +
                "}",
            "import javax.swing.*;\n" +
                "import com.intellij.codeInspection.LocalInspectionTool;\n" +
                "import com.intellij.codeInspection.ui.SingleIntegerFieldOptionsPanel;\n" +
                "\n" +
                "public class SomeInspection extends LocalInspectionTool {\n" +
                "    @Override\n" +
                "    public @Nullable JComponent createOptionsPanel() {\n" +
                "        return new SingleIntegerFieldOptionsPanel(\"\", this, \"\");\n" +
                "    }\n" +
                "}");
    }

    //ConventionOptionsPanel

    public void testReplacesEmptyCreateOptionsPanelWithConventionOptionsPanel() {
        checkAction("SomeInspection.java", CreatePanelOptionsActions.GenerateConventionOptionsPanel::new,
            "import javax.swing.JComponent;\n" +
                "import com.intellij.codeInspection.LocalInspectionTool;\n" +
                "import org.jetbrains.annotations.Nullable;\n" +
                "\n" +
                "public class SomeInspection extends LocalInspectionTool {\n" +
                "    @Override\n" +
                "    public @Nullable JComponent createOptionsPanel() {\n" +
                "        <caret>\n" +
                "    }\n" +
                "}",
            "import javax.swing.JComponent;\n" +
                "import com.intellij.codeInspection.LocalInspectionTool;\n" +
                "import com.intellij.codeInspection.ui.ConventionOptionsPanel;\n" +
                "import org.jetbrains.annotations.Nullable;\n" +
                "\n" +
                "public class SomeInspection extends LocalInspectionTool {\n" +
                "    @Override\n" +
                "    public @Nullable JComponent createOptionsPanel() {\n" +
                "        return new ConventionOptionsPanel(this, \"\", \"\", \"\", \"\");\n" +
                "    }\n" +
                "}");
    }

    public void testReplacesNonEmptyCreateOptionsPanelWithConventionOptionsPanel() {
        checkAction("SomeInspection.java", CreatePanelOptionsActions.GenerateConventionOptionsPanel::new,
            "import javax.swing.*;\n" +
                "import com.intellij.codeInspection.LocalInspectionTool;\n" +
                "\n" +
                "public class SomeInspection extends LocalInspectionTool {\n" +
                "    @Override\n" +
                "    public @Nullable JComponent createOptionsPanel() {\n" +
                "        <caret>return super.createOptionsPanel();\n" +
                "    }\n" +
                "}",
            "import javax.swing.*;\n" +
                "import com.intellij.codeInspection.LocalInspectionTool;\n" +
                "import com.intellij.codeInspection.ui.ConventionOptionsPanel;\n" +
                "\n" +
                "public class SomeInspection extends LocalInspectionTool {\n" +
                "    @Override\n" +
                "    public @Nullable JComponent createOptionsPanel() {\n" +
                "        return new ConventionOptionsPanel(this, \"\", \"\", \"\", \"\");\n" +
                "    }\n" +
                "}");
    }
}
