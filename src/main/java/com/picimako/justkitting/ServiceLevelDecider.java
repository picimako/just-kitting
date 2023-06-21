//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiReferenceExpression;
import com.picimako.justkitting.resources.JustKittingBundle;
import kotlin.Pair;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.asJava.elements.KtLightPsiLiteral;
import org.jetbrains.kotlin.psi.KtAnnotationEntry;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.ValueArgument;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static com.picimako.justkitting.PlatformNames.SERVICE_ANNOTATION;

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
     * Otherwise, NOT_SURE is returned, meaning the service level could not be determined.
     *
     * @param targetClass the class of which the service level is determined
     */
    @NotNull
    public static <T extends PsiNameIdentifierOwner> ServiceLevel getServiceLevel(@NotNull T targetClass) {
        var specifiedServiceLevels = getSpecifiedServiceLevels(targetClass);

        final var levels = specifiedServiceLevels != null
            ? convertToServiceLevelNames(specifiedServiceLevels, targetClass.getProject())
            : Collections.emptyList();

        var level = ServiceLevel.NOT_SURE;
        if (!levels.isEmpty()) {
            if (levels.contains(PROJECT) && levels.contains(APP)) level = ServiceLevel.PROJECT_AND_APP;
            if (levels.contains(PROJECT) && !levels.contains(APP)) level = ServiceLevel.PROJECT;
            if (!levels.contains(PROJECT) && levels.contains(APP)) level = ServiceLevel.APP;
        } else {
            //NOTE: existing constructor and parameter injection are not taken into account in the determination process, for now
            String className = targetClass.getName();
            if (PROJECT_SERVICE_CLASS_NAME_PATTERN.matcher(className).matches())
                level = ServiceLevel.PROJECT;
            else if (APP_SERVICE_CLASS_NAME_PATTERN.matcher(className).matches())
                level = ServiceLevel.APP;
        }
        return level;
    }

    /**
     * Returns the {@link com.intellij.openapi.components.Service.Level} values specified in the target class' {@code @Service} annotation.
     * <p>
     * These values are then handled in {@link #convertToServiceLevelNames(List, Project)}.
     */
    private static <T extends PsiNameIdentifierOwner> @Nullable List<?> getSpecifiedServiceLevels(@NotNull T targetClass) {
        if (targetClass instanceof PsiClass javaServiceClass) {
            var serviceAnnotation = javaServiceClass.getAnnotation(SERVICE_ANNOTATION);
            return serviceAnnotation != null
                ? AnnotationUtil.arrayAttributeValues(serviceAnnotation.findAttributeValue("value"))
                : null;
        }

        return targetClass instanceof KtClass kotlinServiceClass
            ? kotlinServiceClass.getAnnotationEntries()
            .stream()
            //Find the @Service annotation's entry by its name
            .filter(entry -> entry.getShortName() != null && "Service".equals(entry.getShortName().asString()))
            .findFirst()
            .map(KtAnnotationEntry::getValueArguments)
            .orElse(null)
            : null;
    }

    /**
     * More than one service level can be specified in the {@link com.intellij.openapi.components.Service}
     * annotation's value attribute too, hence the more generic solution.
     * <p>
     * The returned collection's size can be 0 to 2 - Empty, "APP" or "PROJECT", "APP" and "PROJECT".
     */
    @NotNull
    private static List<String> convertToServiceLevelNames(List<? extends Object> serviceLevels, Project project) {
        var cache = PlatformPsiCache.getInstance(project);
        return serviceLevels.stream()
            .map(expression -> {
                //Handles Java annotation values
                if (expression instanceof PsiReferenceExpression levelRef)
                    return levelRef.isReferenceTo(cache.getServiceLevelProject()) || levelRef.isReferenceTo(cache.getServiceLevelApp())
                        ? levelRef.getReferenceName()
                        : null;

                //Handles Kotlin annotation values
                if (expression instanceof ValueArgument levelArg) {
                    String argumentText = levelArg.asElement().getText();

                    /*
                     * Returns the service level type based on if the annotation value ends with the proper string.
                     * This is a very simplified logic that handles cases when the service level is specified with
                     * or without a qualifier.
                     */
                    if (argumentText.endsWith(PROJECT)) return PROJECT;
                    if (argumentText.endsWith(APP)) return APP;
                }
                //Handles Kotlin cases at least in integration tests, but might occur in the wild too.
                else if (expression instanceof KtLightPsiLiteral levelRef) {
                    return levelRef.getValue() instanceof Pair levelRefValue ? levelRefValue.getSecond().toString() : null;
                }
                return null;
            })
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    }

    private ServiceLevelDecider() {
        //Utility class
    }

    @RequiredArgsConstructor
    public enum ServiceLevel {
        PROJECT(JustKittingBundle.message("service.level.display.name.project")),
        APP(JustKittingBundle.message("service.level.display.name.app")),
        PROJECT_AND_APP(JustKittingBundle.message("service.level.display.name.project.and.app")),
        NOT_SURE(JustKittingBundle.message("service.level.display.name.not.sure"));

        private final String displayName;

        public String getDisplayName() {
            return displayName;
        }

        public boolean isProject() {
            return this == PROJECT;
        }
    }
}
