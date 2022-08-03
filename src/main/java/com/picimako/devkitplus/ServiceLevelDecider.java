//Copyright 2021 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.devkitplus;

import static com.picimako.devkitplus.PlatformNames.SERVICE_ANNOTATION;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import com.picimako.devkitplus.resources.DevKitPlusBundle;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiReferenceExpression;
import org.jetbrains.annotations.NotNull;

/**
 * Utility for determining the service level of classes that are annotated with {@link com.intellij.openapi.components.Service}.
 */
public final class ServiceLevelDecider {

    private static final Pattern PROJECT_SERVICE_CLASS_NAME_PATTERN = Pattern.compile(".*(ProjectService|ProjectSettings|ProjectState)$");
    private static final Pattern APP_SERVICE_CLASS_NAME_PATTERN = Pattern.compile(".*(ApplicationService|ApplicationSettings|ApplicationState)$");
    private static final String PROJECT = "PROJECT";
    private static final String APP = "APP";

    /**
     * Returns the service level associated with the argument class.
     * <p>
     * If there is at least one {@link com.intellij.openapi.components.Service.Level} attribute specified, then based on the specified ones
     * the returned service level may be PROJECT, APP and PROJECT_AND_APP.
     * <p>
     * If there is NO {@link com.intellij.openapi.components.Service.Level} attribute specified, then PROJECT or APP is returned if one
     * of the predefined regex patterns match the class name.
     * <p>
     * Otherwise NOT_SURE is returned, meaning the service level could not be determined.
     *
     * @param targetClass the class of which the service level is determined
     */
    @NotNull
    public static ServiceLevel getServiceLevel(@NotNull PsiClass targetClass) {
        final List<String> levels = convertToServiceLevelNames(getSpecifiedServiceLevels(targetClass), targetClass.getProject());

        ServiceLevel level = ServiceLevel.NOT_SURE;
        if (!levels.isEmpty()) {
            if (levels.contains(PROJECT) && levels.contains(APP)) level = ServiceLevel.PROJECT_AND_APP;
            if (levels.contains(PROJECT) && !levels.contains(APP)) level = ServiceLevel.PROJECT;
            if (!levels.contains(PROJECT) && levels.contains(APP)) level = ServiceLevel.APP;
        } else {
            //NOTE: existing constructor and parameter injection are not taken into account in the determination process, for now
            String className = targetClass.getName();
            if (PROJECT_SERVICE_CLASS_NAME_PATTERN.matcher(className).matches()) {
                level = ServiceLevel.PROJECT;
            } else if (APP_SERVICE_CLASS_NAME_PATTERN.matcher(className).matches()) {
                level = ServiceLevel.APP;
            }
        }
        return level;
    }

    /**
     * Returns the {@link com.intellij.openapi.components.Service.Level} values specified in the target class' {@code @Service} annotation.
     */
    private static @NotNull List<PsiAnnotationMemberValue> getSpecifiedServiceLevels(@NotNull PsiClass targetClass) {
        var serviceValueAttr = targetClass.getAnnotation(SERVICE_ANNOTATION).findAttributeValue("value");
        return AnnotationUtil.arrayAttributeValues(serviceValueAttr);
    }

    /**
     * More than one service level can be specified in the {@link com.intellij.openapi.components.Service}
     * annotation's value attribute too, hence the more generic solution.
     * <p>
     * The returned collection's size can be 0 to 2 - Empty, "APP" or "PROJECT", "APP" and "PROJECT".
     */
    @NotNull
    private static List<String> convertToServiceLevelNames(List<PsiAnnotationMemberValue> serviceLevels, Project project) {
        var cache = PlatformPsiCache.getInstance(project);
        return serviceLevels.stream()
            .filter(PsiReferenceExpression.class::isInstance)
            .map(PsiReferenceExpression.class::cast)
            .map(levelRef -> (levelRef.isReferenceTo(cache.getServiceLevelProject()) || levelRef.isReferenceTo(cache.getServiceLevelApp()) ? levelRef.getReferenceName() : null))
            .filter(Objects::nonNull)
            .distinct()
            .collect(toList());
    }

    private ServiceLevelDecider() {
        //Utility class
    }

    public enum ServiceLevel {
        PROJECT(DevKitPlusBundle.message("service.level.display.name.project")),
        APP(DevKitPlusBundle.message("service.level.display.name.app")),
        PROJECT_AND_APP(DevKitPlusBundle.message("service.level.display.name.project.and.app")),
        NOT_SURE(DevKitPlusBundle.message("service.level.display.name.not.sure"));

        private final String displayName;

        ServiceLevel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public boolean isProject() {
            return this == PROJECT;
        }

        public boolean isApp() {
            return this == APP;
        }
    }
}
