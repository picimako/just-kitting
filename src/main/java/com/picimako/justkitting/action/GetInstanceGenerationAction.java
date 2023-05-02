//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.action;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.BaseCodeInsightAction;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.picimako.justkitting.resources.JustKittingBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;

/**
 * Actions for generating {@code getInstance()} methods for services, components,
 * and classes that can benefit from such method.
 */
abstract class GetInstanceGenerationAction extends BaseCodeInsightAction {

    @Override
    protected @NotNull CodeInsightActionHandler getHandler() {
        return (project, editor, file) -> {
            PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
            var staticOrTopLevelClass = getStaticOrTopLevelClass(file, editor);
            var getInstance = createMethod(staticOrTopLevelClass, project);
            //Add the method right after the list of fields, or the list of constructor if there is any
            WriteCommandAction.runWriteCommandAction(project,
                () -> {
                    JavaCodeStyleManager.getInstance(project).shortenClassReferences(staticOrTopLevelClass.add(getInstance));
                });
        };
    }

    @Override
    protected void update(@NotNull Presentation presentation, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        super.update(presentation, project, editor, file);
        presentation.setText(getText());
    }

    /**
     * Creates a {@link PsiMethod} for the {@code getInstance()} from the current action's pattern text.
     * <p>
     * This method will be the one added to {@code staticOrTopLevelClass}.
     *
     * @param staticOrTopLevelClass the class in which the method is generated and added into
     */
    protected PsiMethod createMethod(PsiClass staticOrTopLevelClass, Project project) {
        return PsiElementFactory.getInstance(project)
            .createMethodFromText(MessageFormat.format(getGetInstancePattern(), staticOrTopLevelClass.getName()), staticOrTopLevelClass);
    }

    /**
     * Returns the pattern text of the {@code getInstance()} method that is being generated.
     */
    protected abstract String getGetInstancePattern();

    /**
     * Returns the text of the action. It appears in the action list in which users can choose which level of service they
     * are generating the method for.
     */
    protected abstract String getText();

    /**
     * Returns either the top level class in the file, or the immediate static class inside which the caret is place,
     * and where the action would be invoked.
     */
    @Nullable
    public static PsiClass getStaticOrTopLevelClass(PsiFile file, Editor editor) {
        final var element = file.findElementAt(editor.getCaretModel().getOffset());
        if (element != null) {
            var topLevelClass = PsiUtil.getTopLevelClass(element);
            var parentClass = PsiTreeUtil.getParentOfType(element, PsiClass.class);
            //If there is a parent class, and it is either static, or it is the same class as the top level class in the current file
            if (parentClass != null && (isStatic(parentClass) || parentClass.getManager().areElementsEquivalent(parentClass, topLevelClass))) {
                return parentClass;
            }
        }
        return null;
    }

    public static boolean isStatic(PsiModifierListOwner psiModifierListOwner) {
        return psiModifierListOwner.hasModifierProperty(PsiModifier.STATIC);
    }

    /**
     * Generates a {@code getInstance()} method for a project-level service.
     *
     * @since 0.2.0
     */
    static final class ProjectServiceAction extends GetInstanceGenerationAction {
        private static final String GET_INSTANCE_FOR_PROJECT =
            "public static {0} getInstance(com.intellij.openapi.project.Project project) '{'return project.getService({0}.class);}";

        @Override
        protected String getGetInstancePattern() {
            return GET_INSTANCE_FOR_PROJECT;
        }

        @Override
        protected String getText() {
            return JustKittingBundle.message("justkitting.action.generate.getinstance.project.level");
        }
    }

    /**
     * Generates a {@code getInstance()} method for an application-level service.
     *
     * @since 0.2.0
     */
    static final class ApplicationServiceAction extends GetInstanceGenerationAction {
        private static final String GET_INSTANCE_FOR_APPLICATION =
            "public static {0} getInstance() '{'return com.intellij.openapi.application.ApplicationManager.getApplication().getService({0}.class);}";

        @Override
        protected String getGetInstancePattern() {
            return GET_INSTANCE_FOR_APPLICATION;
        }

        @Override
        protected String getText() {
            return JustKittingBundle.message("justkitting.action.generate.getinstance.application.level");
        }
    }

    private GetInstanceGenerationAction() {
        //Utility class
    }
}
