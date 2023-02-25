//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.action;

import static com.picimako.justkitting.LightServiceUtil.isLightService;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.BaseCodeInsightAction;
import com.intellij.lang.LanguageCodeInsightActionHandler;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.containers.ContainerUtil;
import com.picimako.justkitting.ServiceLevelDecider;
import com.picimako.justkitting.resources.JustKittingBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;

/**
 * Generates static {@code getInstance()} method in light-service Java classes.
 * <p>
 * <h3>The action is available when:</h3>
 * <ul>
 *     <li>the class is not anonymous, not an interface, not an enum, not abstract</li>
 *     <li>the class is annotated as {@link com.intellij.openapi.components.Service}</li>
 *     <li>the class doesn't have any static {@code getInstance()} method defined</li>
 * </ul>
 * <h3>Generation logic</h3>
 * <ul>
 *     <li>If {@link com.intellij.openapi.components.Service.Level#PROJECT} is specified, then project-level getter
 *     is generated.</li>
 *     <li>If {@link com.intellij.openapi.components.Service.Level#APP} is specified, then application-level getter
 *     is generated.</li>
 *     <li>If no {@link com.intellij.openapi.components.Service.Level} is specified and the class name ends with
 *     <ul>
 *         <li>ProjectService, ProjectSettings, or ProjectState, then project-level getter is generated,</li>
 *         <li>ApplicationService, ApplicationSettings or ApplicationState, then application-level getter is generated.</li>
 *     </ul>
 *     </li>
 * </ul>
 * In every other case, for now, a warning message is displayed that the service-level could not be determined.
 *
 * @see org.jetbrains.idea.devkit.inspections.NonDefaultConstructorInspection
 * @see <a href="https://plugins.jetbrains.com/docs/intellij/plugin-services.html#retrieving-a-service">Retrieving a Service</a>
 * @since 0.1.0
 */
public class GenerateLightServiceStaticGetInstanceAction extends BaseCodeInsightAction {

    private final LightServiceHandler handler = new LightServiceHandler();

    @Override
    protected @NotNull CodeInsightActionHandler getHandler() {
        return handler;
    }

    @Override
    protected boolean isValidForFile(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        return handler.isValidFor(editor, file);
    }

    private static final class LightServiceHandler implements LanguageCodeInsightActionHandler {
        private static final String GET_INSTANCE_FOR_PROJECT =
            "public static {0} getInstance(com.intellij.openapi.project.Project project) '{'return project.getService({0}.class);}";
        private static final String GET_INSTANCE_FOR_APPLICATION =
            "public static {0} getInstance() '{'return com.intellij.openapi.application.ApplicationManager.getApplication().getService({0}.class);}";
        private static final String GET_INSTANCE = "getInstance";

        @Override
        public boolean isValidFor(Editor editor, PsiFile file) {
            if (file instanceof PsiJavaFile && editor.getProject() != null) {
                PsiClass staticOrTopLevelClass = getStaticOrTopLevelClass(file, editor);
                return staticOrTopLevelClass != null
                    && staticOrTopLevelClass.getNameIdentifier() != null
                    && !staticOrTopLevelClass.isEnum()
                    && !staticOrTopLevelClass.isInterface()
                    && isLightService(staticOrTopLevelClass)
                    && !staticOrTopLevelClass.hasModifierProperty(PsiModifier.ABSTRACT)
                    && !ContainerUtil.exists(staticOrTopLevelClass.getMethods(), method -> GET_INSTANCE.equals(method.getName()) && isStatic(method));
            }
            return false;
        }

        @Override
        public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
            PsiClass staticOrTopLevelClass = getStaticOrTopLevelClass(file, editor);
            final PsiMethod getInstance = createGetInstance(staticOrTopLevelClass, project);
            if (getInstance != null) {
                PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
                //Add the method right after the list of fields, or the list of constructor if there is any
                WriteCommandAction.runWriteCommandAction(project, (Computable<PsiElement>) () -> staticOrTopLevelClass.add(JavaCodeStyleManager.getInstance(project).shortenClassReferences(getInstance)));
            }
        }

        @Nullable
        private PsiClass getStaticOrTopLevelClass(PsiFile file, Editor editor) {
            final PsiElement element = file.findElementAt(editor.getCaretModel().getOffset());
            if (element != null) {
                PsiClass topLevelClass = PsiUtil.getTopLevelClass(element);
                PsiClass parentClass = PsiTreeUtil.getParentOfType(element, PsiClass.class);
                if (parentClass != null && (isStatic(parentClass) || parentClass.getManager().areElementsEquivalent(parentClass, topLevelClass))) {
                    return parentClass;
                }
            }
            return null;
        }

        private boolean isStatic(PsiModifierListOwner psiModifierListOwner) {
            return psiModifierListOwner.hasModifierProperty(PsiModifier.STATIC);
        }

        @Nullable
        private PsiMethod createGetInstance(PsiClass staticOrTopLevelClass, Project project) {
            ServiceLevelDecider.ServiceLevel serviceLevel = ServiceLevelDecider.getServiceLevel(staticOrTopLevelClass);
            switch (serviceLevel) {
                case PROJECT:
                    return PsiElementFactory.getInstance(project)
                        .createMethodFromText(MessageFormat.format(GET_INSTANCE_FOR_PROJECT, staticOrTopLevelClass.getName()), staticOrTopLevelClass);
                case APP:
                    return PsiElementFactory.getInstance(project)
                        .createMethodFromText(MessageFormat.format(GET_INSTANCE_FOR_APPLICATION, staticOrTopLevelClass.getName()), staticOrTopLevelClass);
                case PROJECT_AND_APP:
                    //TODO: Not supported for now - Let users choose from a dialog/popup, maybe similarly to how getter and constructor creation happens.
                case NOT_SURE:
                default:
                    Messages.showWarningDialog(JustKittingBundle.message("justkitting.action.light.service.generate.static.getter.warning.message"), JustKittingBundle.message("justkitting.action.light.service.generate.static.getter.warning.title"));
            }
            return null;
        }

        @Override
        public boolean startInWriteAction() {
            return false;
        }
    }
}
