//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.inlayhint;

import java.util.Collection;
import java.util.Objects;

import com.picimako.justkitting.PlatformPsiCache;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;

/**
 * Utility to search for light service classes in the current project.
 */
@SuppressWarnings("UnstableApiUsage")
public final class LightServiceLookup {
    
    /**
     * Returns the collection of PsiClasses that are annotated as {@link com.intellij.openapi.components.Service}.
     */
    public static Collection<PsiClass> lookupLightServiceClasses(Project project) {
        return ApplicationManager.getApplication().runReadAction((Computable<Collection<PsiClass>>) () -> getServiceAnnotationQuery(project).findAll());
    }

    /**
     * Returns whether there is at least on class in the project that is annotated as {@link com.intellij.openapi.components.Service}.
     */
    public static boolean isProjectHasLightService(Project project) {
        return ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () -> getServiceAnnotationQuery(project).findFirst() != null);
    }

    @NotNull
    private static Query<PsiClass> getServiceAnnotationQuery(Project project) {
        return ReferencesSearch.search(PlatformPsiCache.getInstance(project).getServiceAnnotation(), ProjectScope.getProjectScope(project))
            .filtering(PsiJavaCodeReferenceElement.class::isInstance)
            .filtering(ref -> ref.getElement().getParent() instanceof PsiAnnotation)
            .mapping(ref -> PsiTreeUtil.getParentOfType(ref.getElement().getParent(), PsiClass.class))
            .filtering(Objects::nonNull);
    }

    private LightServiceLookup() {
        //Utility class
    }
}
