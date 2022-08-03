//Copyright 2021 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.devkitplus.action.inspectionpanel;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.BaseCodeInsightAction;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.SimpleListCellRenderer;
import com.picimako.devkitplus.PlatformPsiCache;
import com.picimako.devkitplus.resources.DevKitPlusBundle;
import com.siyeh.ig.psiutils.ImportUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.util.function.Supplier;

/**
 * Provides intentions for generating code snippets for {@link com.intellij.codeInspection.InspectionProfileEntry#createOptionsPanel()} based
 * on the available UI components in the {@link com.intellij.codeInspection.ui} package.
 * <p>
 * <strong>NOTE:</strong>
 * These intentions, for now, replace the whole body of the {@code createOptionsPanel()} method instead of inserting the corresponding code snippets.
 * This is partly because of the general usage pattern in the intellij-community project.
 *
 * @see CreateInspectionOptionsPanelAction
 */
@SuppressWarnings("HardCodedStringLiteral")
public final class CreatePanelOptionsActions {

    private CreatePanelOptionsActions() {
    }

    private static void replaceCreateOptionsPanelWith(String replacementCodeBlock, Editor editor, PsiFile file, Supplier<PsiClass> classToImport) {
        PsiDocumentManager.getInstance(file.getProject()).commitDocument(editor.getDocument());
        PsiMethod createOptionsPanelMethod = PsiTreeUtil.getParentOfType(file.findElementAt(editor.getCaretModel().getOffset()), PsiMethod.class);
        WriteCommandAction.runWriteCommandAction(file.getProject(), () -> {
            var methodBodyBlock = PsiElementFactory.getInstance(file.getProject()).createCodeBlockFromText(replacementCodeBlock, createOptionsPanelMethod);
            ImportUtils.addImportIfNeeded(classToImport.get(), file);
            createOptionsPanelMethod.getBody().replace(methodBodyBlock);
        });
    }

    /**
     * Replaces the body of the {@link com.intellij.codeInspection.InspectionProfileEntry#createOptionsPanel()} method with a code snippet creating
     * a {@link com.intellij.codeInspection.ui.MultipleCheckboxOptionsPanel} with a pre-selected number of checkbox addition calls.
     * <p>
     * <strong>From:</strong>
     * <pre>
     * public class SomeInspection extends LocalInspectionTool {
     *     &#064;Override
     *     public @Nullable JComponent createOptionsPanel() {
     *         return super.createOptionsPanel();
     *     }
     * }
     * </pre>
     * <strong>To (given 3 checkboxes were selected):</strong>
     * <pre>
     * public class SomeInspection extends LocalInspectionTool {
     *     &#064;Override
     *     public @Nullable JComponent createOptionsPanel() {
     *         MultipleCheckboxOptionsPanel panel = new MultipleCheckboxOptionsPanel(this);
     * 		   panel.addCheckbox("", "");
     * 		   panel.addCheckbox("", "");
     * 		   panel.addCheckbox("", "");
     * 		   return panel;
     *     }
     * }
     * </pre>
     * <p>
     * The number of checkboxes selectable by users ranges from 1 to 5.
     */
    static final class GenerateMultipleCheckboxOptionsPanel extends BaseCodeInsightAction {
        static final GenerateMultipleCheckboxOptionsPanel INSTANCE = new GenerateMultipleCheckboxOptionsPanel();
        private final int numberOfCheckboxes;

        public GenerateMultipleCheckboxOptionsPanel() {
            this.numberOfCheckboxes = 1;
        }

        /**
         * Used in integration test to be able to define the number of checkboxes without the involvement of any UI component.
         */
        @TestOnly
        GenerateMultipleCheckboxOptionsPanel(int numberOfCheckboxes) {
            this.numberOfCheckboxes = numberOfCheckboxes;
        }

        @Override
        protected @NotNull CodeInsightActionHandler getHandler() {
            return new CodeInsightActionHandler() {
                @Override
                public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
                    BaseListPopupStep<String> step = new BaseListPopupStep<>(
                        DevKitPlusBundle.message("intention.ui.select.checkbox.count.title"),
                        "1", "2", "3", "4", "5") {
                        @Override
                        public @Nullable PopupStep<?> onChosen(String selectedNumberOfCheckboxes, boolean finalChoice) {
                            replaceCreateOptionsPanelWith(
                                "{\nMultipleCheckboxOptionsPanel panel = new MultipleCheckboxOptionsPanel(this);\n"
                                    + "panel.addCheckbox(\"\", \"\");\n".repeat(getNumberOfCheckboxes(selectedNumberOfCheckboxes))
                                    + "return panel;\n}",
                                editor, file, () -> PlatformPsiCache.getInstance(project).getMultipleCheckboxOptionsPanel());
                            return null;
                        }
                    };

                    JBPopupFactory.getInstance()
                        .createListPopup(project, step, listCellRenderer -> SimpleListCellRenderer.create("", Object::toString))
                        .showInBestPositionFor(editor);
                }

                @Override
                public boolean startInWriteAction() {
                    return false;
                }
            };
        }

        @Override
        protected void update(@NotNull Presentation presentation, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
            super.update(presentation, project, editor, file);
            presentation.setText(DevKitPlusBundle.message("intention.ui.generate.multi.checkbox.panel"));
        }

        private int getNumberOfCheckboxes(String selectedNumberOfCheckboxes) {
            return ApplicationManager.getApplication().isUnitTestMode() ? numberOfCheckboxes : Integer.parseInt(selectedNumberOfCheckboxes);
        }
    }

    /**
     * Replaces the body of the {@link com.intellij.codeInspection.InspectionProfileEntry#createOptionsPanel()} method with a code snippet creating
     * a {@link com.intellij.codeInspection.ui.SingleCheckboxOptionsPanel}.
     * <p>
     * <strong>From:</strong>
     * <pre>
     * public class SomeInspection extends LocalInspectionTool {
     *     &#064;Override
     *     public @Nullable JComponent createOptionsPanel() {
     *         return super.createOptionsPanel();
     *     }
     * }
     * </pre>
     * <strong>To:</strong>
     * <pre>
     * public class SomeInspection extends LocalInspectionTool {
     *     &#064;Override
     *     public @Nullable JComponent createOptionsPanel() {
     *         return new SingleCheckboxOptionsPanel("", this, "");
     *     }
     * }
     * </pre>
     */
    static final class GenerateSingleCheckboxOptionsPanel extends BaseCodeInsightAction {
        private static final CodeInsightActionHandler HANDLER = new CodeInsightActionHandler() {
            @Override
            public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
                replaceCreateOptionsPanelWith("{return new com.intellij.codeInspection.ui.SingleCheckboxOptionsPanel(\"\", this, \"\");}",
                    editor, file, () -> PlatformPsiCache.getInstance(project).getSingleCheckboxOptionsPanel());
            }

            @Override
            public boolean startInWriteAction() {
                return false;
            }
        };
        static final GenerateSingleCheckboxOptionsPanel INSTANCE = new GenerateSingleCheckboxOptionsPanel();

        @Override
        protected void update(@NotNull Presentation presentation, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
            super.update(presentation, project, editor, file);
            presentation.setText(DevKitPlusBundle.message("intention.ui.generate.single.checkbox.panel"));
        }

        @Override
        protected @NotNull CodeInsightActionHandler getHandler() {
            return HANDLER;
        }
    }

    /**
     * Replaces the body of the {@link com.intellij.codeInspection.InspectionProfileEntry#createOptionsPanel()} method with a code snippet creating
     * a {@link com.intellij.codeInspection.ui.SingleIntegerFieldOptionsPanel}.
     * <p>
     * <strong>From:</strong>
     * <pre>
     * public class SomeInspection extends LocalInspectionTool {
     *     &#064;Override
     *     public @Nullable JComponent createOptionsPanel() {
     *         return super.createOptionsPanel();
     *     }
     * }
     * </pre>
     * <strong>To:</strong>
     * <pre>
     * public class SomeInspection extends LocalInspectionTool {
     *     &#064;Override
     *     public @Nullable JComponent createOptionsPanel() {
     *         return new SingleIntegerFieldOptionsPanel("", this, "");
     *     }
     * }
     * </pre>
     */
    static final class GenerateSingleIntegerFieldOptionsPanel extends BaseCodeInsightAction {
        private static final CodeInsightActionHandler HANDLER = new CodeInsightActionHandler() {
            @Override
            public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
                replaceCreateOptionsPanelWith("{return new com.intellij.codeInspection.ui.SingleIntegerFieldOptionsPanel(\"\", this, \"\");}",
                    editor, file, () -> PlatformPsiCache.getInstance(project).getSingleIntegerFieldOptionsPanel());
            }

            @Override
            public boolean startInWriteAction() {
                return false;
            }
        };
        static final GenerateSingleIntegerFieldOptionsPanel INSTANCE = new GenerateSingleIntegerFieldOptionsPanel();

        @Override
        protected void update(@NotNull Presentation presentation, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
            super.update(presentation, project, editor, file);
            presentation.setText(DevKitPlusBundle.message("intention.ui.generate.single.integer.field.options.panel"));

        }

        @Override
        protected @NotNull CodeInsightActionHandler getHandler() {
            return HANDLER;
        }
    }

    /**
     * Replaces the body of the {@link com.intellij.codeInspection.InspectionProfileEntry#createOptionsPanel()} method with a code snippet creating
     * a {@link com.intellij.codeInspection.ui.ConventionOptionsPanel}.
     * <p>
     * <strong>From:</strong>
     * <pre>
     * public class SomeInspection extends LocalInspectionTool {
     *     &#064;Override
     *     public @Nullable JComponent createOptionsPanel() {
     *         return super.createOptionsPanel();
     *     }
     * }
     * </pre>
     * <strong>To:</strong>
     * <pre>
     * public class SomeInspection extends LocalInspectionTool {
     *     &#064;Override
     *     public @Nullable JComponent createOptionsPanel() {
     *         return new ConventionOptionsPanel(this, "", "", "", "");
     *     }
     * }
     * </pre>
     */
    static final class GenerateConventionOptionsPanel extends BaseCodeInsightAction {
        private static final CodeInsightActionHandler HANDLER = new CodeInsightActionHandler() {
            @Override
            public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
                replaceCreateOptionsPanelWith("{return new com.intellij.codeInspection.ui.ConventionOptionsPanel(this, \"\", \"\", \"\", \"\");}",
                    editor, file, () -> PlatformPsiCache.getInstance(project).getConventionOptionsPanel());
            }

            @Override
            public boolean startInWriteAction() {
                return false;
            }
        };
        static final GenerateConventionOptionsPanel INSTANCE = new GenerateConventionOptionsPanel();

        @Override
        protected void update(@NotNull Presentation presentation, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
            super.update(presentation, project, editor, file);
            presentation.setText(DevKitPlusBundle.message("intention.ui.generate.convention.options.panel"));
        }

        @Override
        protected @NotNull CodeInsightActionHandler getHandler() {
            return HANDLER;
        }
    }
}
