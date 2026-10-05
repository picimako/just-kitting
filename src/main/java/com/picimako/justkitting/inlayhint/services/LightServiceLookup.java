//Copyright 2026 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.inlayhint.services;

import static com.intellij.openapi.application.ReadAction.computeBlocking;
import static com.intellij.psi.util.PsiTreeUtil.getParentOfType;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.module.impl.scopes.ModulesScope;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.EmptyQuery;
import com.intellij.util.Query;
import com.picimako.justkitting.PlatformPsiCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.idea.references.KtSimpleNameReference;
import org.jetbrains.kotlin.psi.KtAnnotationEntry;
import org.jetbrains.kotlin.psi.KtClass;

import java.util.Collection;
import java.util.Objects;

/**
 * Utility to search for light service classes in the current project.
 */
@SuppressWarnings("UnstableApiUsage")
public final class LightServiceLookup {

    /**
     * Returns the collection of {@link PsiClass}es or {@link KtClass}es that are annotated as {@link com.intellij.openapi.components.Service}.
     */
    public static Collection<? extends PsiNameIdentifierOwner> lookupLightServiceClasses(PsiFile file, boolean isPluginXml) {
        //distinct() is called because Kotlin classes are displayed duplicated
        return computeBlocking(() -> getServiceAnnotationQuery(file, isPluginXml).findAll().stream().distinct().toList());
    }

    /**
     * Returns the collection of {@link PsiClass}es or {@link KtClass}es that are annotated as {@link com.intellij.openapi.components.Service}.
     */
    public static Collection<? extends PsiNameIdentifierOwner> lookupLightServiceClasses(Project project) {
        //distinct() is called because Kotlin classes are displayed duplicated
        return computeBlocking(() -> getServiceAnnotationQuery(ProjectScope.getProjectScope(project), project).findAll().stream().distinct().toList());
    }

    /**
     * Returns whether there is at least one class in the project that is annotated as {@link com.intellij.openapi.components.Service}.
     */
    public static boolean isProjectHasLightService(PsiFile file, boolean isPluginXml) {
        return computeBlocking(() -> getServiceAnnotationQuery(determineSearchScope(file, isPluginXml), file.getProject()).findFirst() != null);
    }

    @NotNull
    private static Query<? extends PsiNameIdentifierOwner> getServiceAnnotationQuery(PsiFile file, boolean isPluginXml) {
        return getServiceAnnotationQuery(determineSearchScope(file, isPluginXml), file.getProject());
    }

    private static GlobalSearchScope determineSearchScope(PsiFile file, boolean isPluginXml) {
        var project = file.getProject();
        if (isPluginXml) return ProjectScope.getProjectScope(project);
        else {
            var module = ModuleUtilCore.findModuleForFile(file);
            return module != null ? ModulesScope.moduleScope(module) : ProjectScope.getProjectScope(project);
        }
    }

    /**
     * Takes into account only those references of the @Service annotation class that are used as part of an annotation. E.g.:
     * <pre>{@code
     * @Service
     * class SomeService { }
     * }</pre>
     * or
     * <pre>{@code
     * @Service(Service.Level.APP)
     * class SomeService { }
     * }</pre>
     * <p>
     * This will filter out reference like the one below:
     * <pre>{@code
     * class SomeClass(serviceLevel: Service.Level)
     * }</pre>
     */
    @NotNull
    private static Query<? extends PsiNameIdentifierOwner> getServiceAnnotationQuery(GlobalSearchScope scope, Project project) {
        var serviceAnnotation = PlatformPsiCache.getInstance(project).getServiceAnnotation();
        if (serviceAnnotation == null) {
            Logger.getInstance(LightServiceLookup.class)
                .warn("Could not find class 'com.intellij.openapi.components.Service'. No light services will be shown in plugin descriptors.");
            return new EmptyQuery<>();
        }

        return ReferencesSearch.search(serviceAnnotation, scope)
            //Take into account only those references of the @Service annotation class that are used as part of an annotation.
            .filtering(ref -> {
                if (ref instanceof PsiJavaCodeReferenceElement)
                    return getParentOfType(ref.getElement(), PsiAnnotation.class) != null;

                if (ref instanceof KtSimpleNameReference)
                    return getParentOfType(ref.getElement(), KtAnnotationEntry.class) != null;

                return false;
            })
            //Maps the reference to its parent class, so the query returns the light service classes, also filtering out null classes.
            .mapping(ref ->
                ref.getElement().getParent() instanceof PsiAnnotation
                    ? getParentOfType(ref.getElement().getParent(), PsiClass.class)
                    : getParentOfType(ref.getElement(), KtClass.class))
            .filtering(Objects::nonNull);
    }

    private LightServiceLookup() {
        //Utility class
    }
}
