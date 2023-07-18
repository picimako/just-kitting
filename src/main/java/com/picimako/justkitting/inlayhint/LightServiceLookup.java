//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.inlayhint;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.EmptyQuery;
import com.intellij.util.Query;
import com.picimako.justkitting.PlatformPsiCache;
import com.picimako.justkitting.action.diff.CompareConfigFileWithPluginTemplateAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.idea.references.KtSimpleNameReference;
import org.jetbrains.kotlin.psi.KtAnnotationEntry;
import org.jetbrains.kotlin.psi.KtClass;

import java.util.Collection;
import java.util.Objects;

import static com.intellij.psi.util.PsiTreeUtil.getParentOfType;
import static java.util.stream.Collectors.toList;

/**
 * Utility to search for light service classes in the current project.
 */
@SuppressWarnings("UnstableApiUsage")
public final class LightServiceLookup {

    private static final Logger LOG = Logger.getInstance(LightServiceLookup.class);

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

    /**
     * Takes into account only those references of the @Service annotation class that are used as part of an annotation. E.g.:
     * <pre>
     * &#064;Service
     * class SomeService { }
     * </pre>
     * or
     * <pre>
     * &#064;Service(Service.Level.APP)
     * class SomeService { }
     * </pre>
     * <p>
     * This will filter out reference like the one below:
     * <pre>
     * class SomeClass(serviceLevel: Service.Level)
     * </pre>
     */
    @NotNull
    private static Query<? extends PsiNameIdentifierOwner> getServiceAnnotationQuery(Project project) {
        var serviceAnnotation = PlatformPsiCache.getInstance(project).getServiceAnnotation();
        if (serviceAnnotation == null) {
            LOG.warn("Could not find class 'com.intellij.openapi.components.Service'. This will result in no light service being shown in plugin descriptors.");
            return new EmptyQuery<>();
        }

        return ReferencesSearch.search(serviceAnnotation, ProjectScope.getProjectScope(project))
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
