//Copyright 2021 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.devkitplus.inspection;

import static com.picimako.devkitplus.LightServiceUtil.isLightService;
import static com.picimako.devkitplus.PlatformNames.COMPONENT_MANAGER;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiClassObjectAccessExpression;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiTypeElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.picimako.devkitplus.PlatformNames;
import com.picimako.devkitplus.ServiceLevelDecider;
import com.picimako.devkitplus.ServiceLevelDecider.ServiceLevel;
import com.picimako.devkitplus.resources.DevKitPlusBundle;
import com.siyeh.ig.callMatcher.CallMatcher;
import com.siyeh.ig.psiutils.TypeUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Reports references to light service classes that are marked with a certain {@link com.intellij.openapi.components.Service.Level} but are retrieved
 * on a different level. E.g.: project service retrieved via {@link com.intellij.openapi.application.Application} or vice versa.
 * <p>
 * Light services that are marked with both {@link com.intellij.openapi.components.Service.Level#PROJECT} and {@link com.intellij.openapi.components.Service.Level#APP}
 * or neither of them, are excluded from the report.
 *
 * @since 0.1.0
 */
public class LightServiceRetrievalInspection extends LocalInspectionTool {
    private static final CallMatcher COMPONENT_MANAGER_GET_SERVICE_MATCHER =
        CallMatcher.instanceCall(COMPONENT_MANAGER, "getService", "getServiceIfCreated").parameterCount(1);

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitClassObjectAccessExpression(PsiClassObjectAccessExpression expression) {
                var parentCall = PsiTreeUtil.getParentOfType(expression, PsiMethodCallExpression.class);
                if (expression.getOperand().getType() instanceof PsiClassType && COMPONENT_MANAGER_GET_SERVICE_MATCHER.matches(parentCall)) {
                    var referencedServiceClass = expression.getOperand();
                    var serviceClass = ((PsiClassType) referencedServiceClass.getType()).resolve();
                    if (isLightService(serviceClass)) {
                        var serviceClassServiceLevel = ServiceLevelDecider.getServiceLevel(serviceClass);
                        var projectOrApplication = (PsiClassType) parentCall.getMethodExpression().getQualifierExpression().getType();
                        if (checkForAppServiceRetrievedViaProject(referencedServiceClass, serviceClassServiceLevel, projectOrApplication))
                            return;
                        checkForProjectServiceRetrievedViaApplication(referencedServiceClass, serviceClassServiceLevel, projectOrApplication);
                    }
                }
            }

            /**
             * Reports a problem if an application service is retrieved via Project.
             */
            private boolean checkForAppServiceRetrievedViaProject(PsiTypeElement referencedClass, ServiceLevel serviceClassServiceLevel, PsiClassType projectOrApplication) {
                if (serviceClassServiceLevel.isApp() && TypeUtils.typeEquals(PlatformNames.PROJECT, projectOrApplication)) {
                    holder.registerProblem(referencedClass, DevKitPlusBundle.inspection("light.service.app.level.retrieved.via.project"), ProblemHighlightType.WEAK_WARNING);
                    return true;
                }
                return false;
            }

            /**
             * Reports a problem if a project service is retrieved via Application.
             */
            private void checkForProjectServiceRetrievedViaApplication(PsiTypeElement referencedClass, ServiceLevel serviceClassServiceLevel, PsiClassType projectOrApplication) {
                if (serviceClassServiceLevel.isProject() && TypeUtils.typeEquals(PlatformNames.APPLICATION, projectOrApplication)) {
                    holder.registerProblem(referencedClass, DevKitPlusBundle.inspection("light.service.project.level.retrieved.via.application"), ProblemHighlightType.WEAK_WARNING);
                }
            }
        };
    }
}
