//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting;

import static com.picimako.justkitting.PlatformNames.SERVICE_ANNOTATION;
import static java.util.stream.Collectors.toList;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiReferenceExpression;
import com.picimako.justkitting.resources.JustKittingBundle;
import kotlin.Pair;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.asJava.elements.KtLightPsiLiteral;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UastFacade;
import org.jetbrains.uast.kotlin.KotlinUQualifiedReferenceExpression;
import org.jetbrains.uast.kotlin.KotlinUVarargExpression;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

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
    private static <T extends PsiNameIdentifierOwner> @Nullable List<? extends Object> getSpecifiedServiceLevels(@NotNull T targetClass) {
        if (targetClass instanceof PsiClass) {
            var serviceAnnotation = ((PsiClass) targetClass).getAnnotation(SERVICE_ANNOTATION);
            if (serviceAnnotation == null) return null;

            var serviceValueAttr = serviceAnnotation.findAttributeValue("value");
            return AnnotationUtil.arrayAttributeValues(serviceValueAttr);
        }

        /*
         * Since I didn't find a proper way to fetch the annotation from a KtClass by the annotation's fully qualified name,
         * and I didn't find a way to retrieve the fully qualified name of a KtAnnotation or KtAnnotationEntry,
         * it spiraled into this UAST-based implementation.
         *
         * It works.
         */
        if (targetClass instanceof KtClass) {
            var targetCls = UastFacade.INSTANCE.convertElementWithParent(targetClass, new Class[]{UClass.class});
            if (targetCls instanceof UClass) {
                var serviceAnnotation = ((UClass) targetCls).findAnnotation(SERVICE_ANNOTATION);
                if (serviceAnnotation == null) return null;

                var serviceValueAttr = serviceAnnotation.findAttributeValue("value");
                if (serviceValueAttr instanceof KotlinUVarargExpression) {
                    return ((KotlinUVarargExpression) serviceValueAttr).getValueArguments()
                        .stream()
                        .filter(KotlinUQualifiedReferenceExpression.class::isInstance)
                        .map(KotlinUQualifiedReferenceExpression.class::cast)
                        .collect(toList());
                } else if (serviceValueAttr instanceof KotlinUQualifiedReferenceExpression) {
                    return Collections.singletonList(serviceValueAttr);
                }
            }
        }
        return null;
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
                if (expression instanceof PsiReferenceExpression) {
                    var levelRef = (PsiReferenceExpression) expression;
                    return levelRef.isReferenceTo(cache.getServiceLevelProject()) || levelRef.isReferenceTo(cache.getServiceLevelApp()) ? levelRef.getReferenceName() : null;
                } else if (expression instanceof KotlinUQualifiedReferenceExpression) {
                    var levelRef = (KotlinUQualifiedReferenceExpression) expression;
                    var levelEnumConst = levelRef.resolve();

                    var psiManager = PsiManager.getInstance(project);
                    return psiManager.areElementsEquivalent(levelEnumConst, cache.getServiceLevelProject()) || psiManager.areElementsEquivalent(levelEnumConst, cache.getServiceLevelApp())
                        ? levelRef.getResolvedName()
                        : null;
                } else if (expression instanceof KtLightPsiLiteral) {
                    var levelRefValue = ((KtLightPsiLiteral) expression).getValue();
                    return levelRefValue instanceof Pair ? ((Pair) levelRefValue).getSecond().toString() : null;
                }
                return null;
            })
            .filter(Objects::nonNull)
            .distinct()
            .collect(toList());
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
