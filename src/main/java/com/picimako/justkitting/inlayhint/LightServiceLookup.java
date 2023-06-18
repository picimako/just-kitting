//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.inlayhint;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Query;
import com.picimako.justkitting.PlatformPsiCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.idea.references.KtSimpleNameReference;
import org.jetbrains.kotlin.psi.KtClass;

import java.util.Collection;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

/**
 * Utility to search for light service classes in the current project.
 */
@SuppressWarnings("UnstableApiUsage")
public final class LightServiceLookup {

    /**
     * Returns the collection of {@link PsiClass}es or {@link KtClass}es that are annotated as {@link com.intellij.openapi.components.Service}.
     */
    public static Collection<? extends PsiNameIdentifierOwner> lookupLightServiceClasses(Project project) {
        return ApplicationManager.getApplication()
            .runReadAction((Computable<Collection<? extends PsiNameIdentifierOwner>>)
                //distinct() is called because Kotlin classes are displayed duplicated
                () -> getServiceAnnotationQuery(project).findAll().stream().distinct().collect(toList()));
    }

    /**
     * Returns whether there is at least one class in the project that is annotated as {@link com.intellij.openapi.components.Service}.
     */
    public static boolean isProjectHasLightService(Project project) {
        return ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () -> getServiceAnnotationQuery(project).findFirst() != null);
    }

    @NotNull
    private static Query<? extends PsiNameIdentifierOwner> getServiceAnnotationQuery(Project project) {
        return ReferencesSearch.search(PlatformPsiCache.getInstance(project).getServiceAnnotation(), ProjectScope.getProjectScope(project))
            .filtering(ref -> ref instanceof PsiJavaCodeReferenceElement || ref instanceof KtSimpleNameReference)
            .mapping(ref ->
                ref.getElement().getParent() instanceof PsiAnnotation
                    ? PsiTreeUtil.getParentOfType(ref.getElement().getParent(), PsiClass.class)
                    : PsiTreeUtil.getParentOfType(ref.getElement(), KtClass.class))
            .filtering(Objects::nonNull);
    }

    private LightServiceLookup() {
        //Utility class
    }
}
