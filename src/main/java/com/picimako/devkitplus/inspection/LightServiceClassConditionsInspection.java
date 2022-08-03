//Copyright 2021 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.devkitplus.inspection;

import static com.picimako.devkitplus.LightServiceUtil.isLightService;

import java.util.List;
import java.util.stream.Collectors;

import com.picimako.devkitplus.resources.DevKitPlusBundle;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.devkit.dom.ExtensionPoint;
import org.jetbrains.idea.devkit.dom.index.ExtensionPointIndex;
import org.jetbrains.idea.devkit.util.DescriptorUtil;
import org.jetbrains.idea.devkit.util.ExtensionCandidate;
import org.jetbrains.idea.devkit.util.ExtensionLocatorKt;
import org.jetbrains.idea.devkit.util.PluginRelatedLocatorsUtils;

/**
 * Validates some of the conditions that light services must (or nice to) meet according to the
 * <a href="https://plugins.jetbrains.com/docs/intellij/plugin-services.html#light-services">Light Services</a>
 * documentation.
 * <p>
 * <strong>Final light service classes</strong>
 * <p>
 * Light service classes must be final, otherwise the platform won't be able to properly instantiate it.
 * <p>
 * <strong>Light service class registered in plugin descriptor</strong>
 * <p>
 * Services that are not intended to be overridden are not necessary to be registered in plugin.xml,
 * but are enough to be annotated with {@link com.intellij.openapi.components.Service}.
 * <p>
 * <strong>Other validations</strong>
 * <p>
 * Detecting unsupported constructor parameter types is already implemented in Plugin DevKit under 'Non-default constructors
 * for service and extension class' inspection.
 *
 * @since 0.1.0
 */
public class LightServiceClassConditionsInspection extends LocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitClass(PsiClass aClass) {
                if (aClass.getNameIdentifier() == null
                    || aClass.isEnum()
                    || aClass.isInterface()
                    || aClass.isRecord()
                    || aClass.hasModifierProperty(PsiModifier.ABSTRACT)
                    || !isLightService(aClass)) {
                    return;
                }
                checkForFinalModifier(aClass, holder);
                checkForRegistrationInPluginXml(aClass, holder);
            }
        };
    }

    /**
     * Reports the light service class if it is not marked as {@code final}.
     */
    private void checkForFinalModifier(PsiClass aClass, @NotNull ProblemsHolder holder) {
        if (!aClass.hasModifierProperty(PsiModifier.FINAL)) {
            holder.registerProblem(aClass.getNameIdentifier(), DevKitPlusBundle.inspection("light.service.must.be.final"));
        }
    }

    /**
     * Reports a problem if there is any service declaration whose {@code serviceImplementation} class contains the fully qualified name of
     * a class annotated with {@link com.intellij.openapi.components.Service}.
     */
    private void checkForRegistrationInPluginXml(PsiClass aClass, @NotNull ProblemsHolder holder) {
        var isRegistered = getServices(holder.getProject()).stream()
            //not checking serviceInterface for now
            .anyMatch(extensionCandidate -> aClass.getQualifiedName().equals(extensionCandidate.pointer.getElement().getAttributeValue("serviceImplementation")));

        if (isRegistered) {
            holder.registerProblem(aClass.getNameIdentifier(), DevKitPlusBundle.inspection("light.service.registered.in.plugin.xml"), ProblemHighlightType.WEAK_WARNING);
        }
    }

    /**
     * Retrieves all project, application and module service declarations from all plugin descriptor files in the current project.
     */
    private List<ExtensionCandidate> getServices(Project project) {
        return CachedValuesManager.getManager(project).getCachedValue(project, () -> {
            var services = ExtensionLocatorKt.locateExtensionsByExtensionPoint(getServiceEP(project, "com.intellij.projectService"));
            services.addAll(ExtensionLocatorKt.locateExtensionsByExtensionPoint(getServiceEP(project, "com.intellij.applicationService")));
            services.addAll(ExtensionLocatorKt.locateExtensionsByExtensionPoint(getServiceEP(project, "com.intellij.moduleService")));

            //Since, in each plugin, there must be at least one plugin descriptor (the plugin.xml),
            // DescriptorUtil.getPlugins() shouldn't return an empty collection, thus that case is not handled.
            //If a new dependent plugin descriptor file is added to the project, regardless of it is added to its parent
            // descriptor file or not, it is found by this call, thus the inspection will use the up-to-date project status
            // for validation.
            return CachedValueProvider.Result.create(services,
                DescriptorUtil.getPlugins(project,
                    GlobalSearchScope.projectScope(project)).stream().map(DomElement::getXmlElement).collect(Collectors.toList()));
        });
    }

    private static ExtensionPoint getServiceEP(Project project, String serviceEPId) {
        return CachedValuesManager.getManager(project).getCachedValue(project, () -> new CachedValueProvider.Result<>(
            ExtensionPointIndex.findExtensionPoint(project, PluginRelatedLocatorsUtils.getCandidatesScope(project), serviceEPId),
            ModificationTracker.NEVER_CHANGED));
    }
}
