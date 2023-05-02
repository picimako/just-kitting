//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.action;

import static com.picimako.justkitting.action.GetInstanceGenerationAction.getStaticOrTopLevelClass;
import static com.picimako.justkitting.action.GetInstanceGenerationAction.isStatic;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.BaseCodeInsightAction;
import com.intellij.lang.LanguageCodeInsightActionHandler;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.util.containers.ContainerUtil;
import com.picimako.justkitting.ListPopupHelper;
import com.picimako.justkitting.ServiceLevelDecider;
import com.picimako.justkitting.action.GetInstanceGenerationAction.ApplicationServiceAction;
import com.picimako.justkitting.action.GetInstanceGenerationAction.ProjectServiceAction;
import com.picimako.justkitting.resources.JustKittingBundle;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Generates static {@code getInstance()} method in service and other eligible Java classes.
 * <p>
 * <h3>The action is available when:</h3>
 * <ul>
 *     <li>the class is not anonymous, not an enum</li>
 *     <li>the class doesn't have a static {@code getInstance()} method defined</li>
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
 *     <li>Otherwise, users can choose between generating application- and project-level method.</li>
 * </ul>
 *
 * @see org.jetbrains.idea.devkit.inspections.NonDefaultConstructorInspection
 * @see <a href="https://plugins.jetbrains.com/docs/intellij/plugin-services.html#retrieving-a-service">Retrieving a Service</a>
 * @since 0.1.0
 */
public class GenerateStaticGetInstanceAction extends BaseCodeInsightAction {
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
        private static final String GET_INSTANCE = "getInstance";
        private final List<AnAction> actions = List.of(new ProjectServiceAction(), new ApplicationServiceAction());

        @Override
        public boolean isValidFor(Editor editor, PsiFile file) {
            if (file instanceof PsiJavaFile && editor.getProject() != null) {
                var staticOrTopLevelClass = getStaticOrTopLevelClass(file, editor);
                return staticOrTopLevelClass != null
                    && staticOrTopLevelClass.getNameIdentifier() != null
                    && !staticOrTopLevelClass.isEnum()
                    //There is no static 'getInstance()' method
                    && !ContainerUtil.exists(staticOrTopLevelClass.getMethods(), method -> GET_INSTANCE.equals(method.getName()) && isStatic(method));
            }
            return false;
        }

        @Override
        public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
            //Null check for 'staticOrTopLevelClass' is not required due to it being checked in 'isValidFor()'
            var serviceLevel = ServiceLevelDecider.getServiceLevel(getStaticOrTopLevelClass(file, editor));
            switch (serviceLevel) {
                case PROJECT:
                    new ProjectServiceAction().getHandler().invoke(project, editor, file);
                    break;
                case APP:
                    new ApplicationServiceAction().getHandler().invoke(project, editor, file);
                    break;
                case PROJECT_AND_APP:
                case NOT_SURE:
                default:
                    ListPopupHelper.showActionsInListPopup(
                        JustKittingBundle.message("action.generate.getinstance.level.list.title"), actions, editor);
            }
        }

        @Override
        public boolean startInWriteAction() {
            return false;
        }
    }
}
