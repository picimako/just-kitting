//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.action.inspectionpanel;

import com.picimako.justkitting.action.JustKittingActionTestBase;

/**
 * Functional test for {@link CreatePanelOptionsActions}.
 */
public class CreatePanelOptionsActionsTest extends JustKittingActionTestBase {

    //MultipleCheckboxOptionsPanel

    public void testReplacesEmptyCreateOptionsPanelWithMultipleCheckboxOptionsPanel() {
        checkAction("SomeInspection.java", () -> new CreatePanelOptionsActions.GenerateMultipleCheckboxOptionsPanel(3),
            """
                import javax.swing.JComponent;
                import com.intellij.codeInspection.LocalInspectionTool;
                import org.jetbrains.annotations.Nullable;

                public class SomeInspection extends LocalInspectionTool {
                    @Override
                    public @Nullable JComponent createOptionsPanel() {
                        <caret>
                    }
                }""",
            """
                import javax.swing.JComponent;
                import com.intellij.codeInspection.LocalInspectionTool;
                import com.intellij.codeInspection.ui.MultipleCheckboxOptionsPanel;
                import org.jetbrains.annotations.Nullable;

                public class SomeInspection extends LocalInspectionTool {
                    @Override
                    public @Nullable JComponent createOptionsPanel() {
                        MultipleCheckboxOptionsPanel panel = new MultipleCheckboxOptionsPanel(this);
                        panel.addCheckbox("", "");
                        panel.addCheckbox("", "");
                        panel.addCheckbox("", "");
                        return panel;
                    }
                }""");
    }

    public void testReplacesNonEmptyCreateOptionsPanelWithMultipleCheckboxOptionsPanel() {
        checkAction("SomeInspection.java", () -> new CreatePanelOptionsActions.GenerateMultipleCheckboxOptionsPanel(4),
            """
                import javax.swing.*;
                import com.intellij.codeInspection.LocalInspectionTool;

                public class SomeInspection extends LocalInspectionTool {
                    @Override
                    public @Nullable JComponent createOptionsPanel() {
                        <caret>return super.createOptionsPanel();
                    }
                }""",
            """
                import javax.swing.*;
                import com.intellij.codeInspection.LocalInspectionTool;
                import com.intellij.codeInspection.ui.MultipleCheckboxOptionsPanel;

                public class SomeInspection extends LocalInspectionTool {
                    @Override
                    public @Nullable JComponent createOptionsPanel() {
                        MultipleCheckboxOptionsPanel panel = new MultipleCheckboxOptionsPanel(this);
                        panel.addCheckbox("", "");
                        panel.addCheckbox("", "");
                        panel.addCheckbox("", "");
                        panel.addCheckbox("", "");
                        return panel;
                    }
                }""");
    }

    //SingleCheckboxOptionsPanel

    public void testReplacesEmptyCreateOptionsPanelWithSingleCheckboxOptionsPanel() {
        checkAction("SomeInspection.java", CreatePanelOptionsActions.GenerateSingleCheckboxOptionsPanel::new,
            """
                import javax.swing.JComponent;
                import com.intellij.codeInspection.LocalInspectionTool;
                import org.jetbrains.annotations.Nullable;

                public class SomeInspection extends LocalInspectionTool {
                    @Override
                    public @Nullable JComponent createOptionsPanel() {
                        <caret>
                    }
                }""",
            """
                import javax.swing.JComponent;
                import com.intellij.codeInspection.LocalInspectionTool;
                import com.intellij.codeInspection.ui.SingleCheckboxOptionsPanel;
                import org.jetbrains.annotations.Nullable;

                public class SomeInspection extends LocalInspectionTool {
                    @Override
                    public @Nullable JComponent createOptionsPanel() {
                        return new SingleCheckboxOptionsPanel("", this, "");
                    }
                }""");
    }

    public void testReplacesNonEmptyCreateOptionsPanelWithSingleCheckboxOptionsPanel() {
        checkAction("SomeInspection.java", CreatePanelOptionsActions.GenerateSingleCheckboxOptionsPanel::new,
            """
                import javax.swing.*;
                import com.intellij.codeInspection.LocalInspectionTool;

                public class SomeInspection extends LocalInspectionTool {
                    @Override
                    public @Nullable JComponent createOptionsPanel() {
                        <caret>return super.createOptionsPanel();
                    }
                }""",
            """
                import javax.swing.*;
                import com.intellij.codeInspection.LocalInspectionTool;
                import com.intellij.codeInspection.ui.SingleCheckboxOptionsPanel;

                public class SomeInspection extends LocalInspectionTool {
                    @Override
                    public @Nullable JComponent createOptionsPanel() {
                        return new SingleCheckboxOptionsPanel("", this, "");
                    }
                }""");
    }

    //SingleIntegerFieldOptionsPanel

    public void testReplacesEmptyCreateOptionsPanelWithSingleIntegerFieldOptionsPanel() {
        checkAction("SomeInspection.java", CreatePanelOptionsActions.GenerateSingleIntegerFieldOptionsPanel::new,
            """
                import javax.swing.JComponent;
                import com.intellij.codeInspection.LocalInspectionTool;
                import org.jetbrains.annotations.Nullable;

                public class SomeInspection extends LocalInspectionTool {
                    @Override
                    public @Nullable JComponent createOptionsPanel() {
                        <caret>
                    }
                }""",
            """
                import javax.swing.JComponent;
                import com.intellij.codeInspection.LocalInspectionTool;
                import com.intellij.codeInspection.ui.SingleIntegerFieldOptionsPanel;
                import org.jetbrains.annotations.Nullable;

                public class SomeInspection extends LocalInspectionTool {
                    @Override
                    public @Nullable JComponent createOptionsPanel() {
                        return new SingleIntegerFieldOptionsPanel("", this, "");
                    }
                }""");
    }

    public void testReplacesNonEmptyCreateOptionsPanelWithSingleIntegerFieldOptionsPanel() {
        checkAction("SomeInspection.java", CreatePanelOptionsActions.GenerateSingleIntegerFieldOptionsPanel::new,
            """
                import javax.swing.*;
                import com.intellij.codeInspection.LocalInspectionTool;

                public class SomeInspection extends LocalInspectionTool {
                    @Override
                    public @Nullable JComponent createOptionsPanel() {
                        <caret>return super.createOptionsPanel();
                    }
                }""",
            """
                import javax.swing.*;
                import com.intellij.codeInspection.LocalInspectionTool;
                import com.intellij.codeInspection.ui.SingleIntegerFieldOptionsPanel;

                public class SomeInspection extends LocalInspectionTool {
                    @Override
                    public @Nullable JComponent createOptionsPanel() {
                        return new SingleIntegerFieldOptionsPanel("", this, "");
                    }
                }""");
    }

    //ConventionOptionsPanel

    public void testReplacesEmptyCreateOptionsPanelWithConventionOptionsPanel() {
        checkAction("SomeInspection.java", CreatePanelOptionsActions.GenerateConventionOptionsPanel::new,
            """
                import javax.swing.JComponent;
                import com.intellij.codeInspection.LocalInspectionTool;
                import org.jetbrains.annotations.Nullable;

                public class SomeInspection extends LocalInspectionTool {
                    @Override
                    public @Nullable JComponent createOptionsPanel() {
                        <caret>
                    }
                }""",
            """
                import javax.swing.JComponent;
                import com.intellij.codeInspection.LocalInspectionTool;
                import com.intellij.codeInspection.ui.ConventionOptionsPanel;
                import org.jetbrains.annotations.Nullable;

                public class SomeInspection extends LocalInspectionTool {
                    @Override
                    public @Nullable JComponent createOptionsPanel() {
                        return new ConventionOptionsPanel(this, "", "", "", "");
                    }
                }""");
    }

    public void testReplacesNonEmptyCreateOptionsPanelWithConventionOptionsPanel() {
        checkAction("SomeInspection.java", CreatePanelOptionsActions.GenerateConventionOptionsPanel::new,
            """
                import javax.swing.*;
                import com.intellij.codeInspection.LocalInspectionTool;

                public class SomeInspection extends LocalInspectionTool {
                    @Override
                    public @Nullable JComponent createOptionsPanel() {
                        <caret>return super.createOptionsPanel();
                    }
                }""",
            """
                import javax.swing.*;
                import com.intellij.codeInspection.LocalInspectionTool;
                import com.intellij.codeInspection.ui.ConventionOptionsPanel;

                public class SomeInspection extends LocalInspectionTool {
                    @Override
                    public @Nullable JComponent createOptionsPanel() {
                        return new ConventionOptionsPanel(this, "", "", "", "");
                    }
                }""");
    }
}
