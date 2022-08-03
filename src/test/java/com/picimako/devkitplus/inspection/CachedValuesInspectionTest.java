//Copyright 2021 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.devkitplus.inspection;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;

/**
 * Functional test for {@link CachedValuesInspection}.
 */
public class CachedValuesInspectionTest extends DevKitPlusInspectionTestBase {

    @Override
    protected String getTestDataPath() {
        return super.getTestDataPath() + "inspection";
    }

    @Override
    protected void tuneFixture(JavaModuleFixtureBuilder<?> moduleBuilder) throws Exception {
        super.tuneFixture(moduleBuilder);
        loadJavaApiJar(moduleBuilder);
    }

    @Override
    protected InspectionProfileEntry getInspection() {
        return new CachedValuesInspection();
    }

    //Result.create()

    public void testNeverChangedIsAddedForEmptyDependency() {
        doQuickFixTest("Add ModificationTracker.NEVER_CHANGED as dependency", "NeverChangedIsAddedForEmptyDependency.java",
            "import com.intellij.psi.util.CachedValueProvider;\n" +
                "\n" +
                "public class NeverChangedIsAddedForEmptyDependency {\n" +
                "   public void method() {\n" +
                "       CachedValueProvider.Result.cre<caret>ate(new Object[1]);\n" +
                "   }\n" +
                "}",
            "import com.intellij.openapi.util.ModificationTracker;\n" +
                "import com.intellij.psi.util.CachedValueProvider;\n" +
                "\n" +
                "public class NeverChangedIsAddedForEmptyDependency {\n" +
                "   public void method() {\n" +
                "       CachedValueProvider.Result.create(new Object[1], ModificationTracker.NEVER_CHANGED);\n" +
                "   }\n" +
                "}");
    }

    public void testModificationCountIsAddedForEmptyDependency() {
        doQuickFixTest("Add PsiModificationTracker.MODIFICATION_COUNT as dependency", "ModificationCountIsAddedForEmptyDependency.java",
            "import com.intellij.psi.util.CachedValueProvider;\n" +
                "\n" +
                "public class ModificationCountIsAddedForEmptyDependency {\n" +
                "   public void method() {\n" +
                "       CachedValueProvider.Result.cre<caret>ate(new Object[1]);\n" +
                "   }\n" +
                "}",
            "import com.intellij.psi.util.CachedValueProvider;\n" +
                "import com.intellij.psi.util.PsiModificationTracker;\n" +
                "\n" +
                "public class ModificationCountIsAddedForEmptyDependency {\n" +
                "   public void method() {\n" +
                "       CachedValueProvider.Result.create(new Object[1], PsiModificationTracker.MODIFICATION_COUNT);\n" +
                "   }\n" +
                "}");
    }

    public void testNeverChangedIsAddedForEmptyCollectionDependency() {
        doQuickFixTest("Replace empty collection with ModificationTracker.NEVER_CHANGED", "NeverChangedIsAddedForEmptyCollectionDependency.java",
            "import com.intellij.psi.util.CachedValueProvider;\n" +
                "import java.util.Collections;\n" +
                "\n" +
                "public class NeverChangedIsAddedForEmptyCollectionDependency {\n" +
                "   public void method() {\n" +
                "       CachedValueProvider.Result.cre<caret>ate(new Object[1], Collections.emptyList());\n" +
                "   }\n" +
                "}",
            "import com.intellij.openapi.util.ModificationTracker;\n" +
                "import com.intellij.psi.util.CachedValueProvider;\n" +
                "import java.util.Collections;\n" +
                "\n" +
                "public class NeverChangedIsAddedForEmptyCollectionDependency {\n" +
                "   public void method() {\n" +
                "       CachedValueProvider.Result.create(new Object[1], ModificationTracker.NEVER_CHANGED);\n" +
                "   }\n" +
                "}");
    }

    public void testModificationCountIsAddedForEmptyCollectionDependency() {
        doQuickFixTest("Replace empty collection with PsiModificationTracker.MODIFICATION_COUNT", "ModificationCountIsAddedForEmptyCollectionDependency.java",
            "import com.intellij.psi.util.CachedValueProvider;\n" +
                "import java.util.Collections;\n" +
                "\n" +
                "public class ModificationCountIsAddedForEmptyCollectionDependency {\n" +
                "   public void method() {\n" +
                "       CachedValueProvider.Result.cre<caret>ate(new Object[1], Collections.emptyList());\n" +
                "   }\n" +
                "}",
            "import com.intellij.psi.util.CachedValueProvider;\n" +
                "import com.intellij.psi.util.PsiModificationTracker;\n" +
                "\n" +
                "import java.util.Collections;\n" +
                "\n" +
                "public class ModificationCountIsAddedForEmptyCollectionDependency {\n" +
                "   public void method() {\n" +
                "       CachedValueProvider.Result.create(new Object[1], PsiModificationTracker.MODIFICATION_COUNT);\n" +
                "   }\n" +
                "}");
    }

    public void testNoHighlightForNonEmptyDependencies() {
        doJavaTest("",
            "import com.intellij.psi.util.CachedValueProvider;\n" +
                "import com.intellij.psi.PsiClass;\n" +
                "\n" +
                "public class NoHighlightForNonEmptyDependencies {\n" +
                "   public void method(PsiClass psiClass) {\n" +
                "       CachedValueProvider.Result.create(new Object[1], psiClass);\n" +
                "   }\n" +
                "}");
    }

    public void testNoHighlightForModificationTrackerNeverChangedDependency() {
        doJavaTest("NoHighlightForModificationTrackerNeverChangedDependency.java",
            "import com.intellij.psi.util.CachedValueProvider;\n" +
                "import com.intellij.openapi.util.ModificationTracker;\n" +
                "import com.intellij.psi.PsiClass;\n" +
                "\n" +
                "public class NoHighlightForModificationTrackerNeverChangedDependency {\n" +
                "   public void method(PsiClass psiClass) {\n" +
                "       CachedValueProvider.Result.create(new Object[1], ModificationTracker.NEVER_CHANGED);\n" +
                "   }\n" +
                "}");
    }

    public void testNoHighlightForEmptyResultCreateParameterList() {
        doJavaTest("NoHighlightForEmptyResultCreateParameterList.java",
            "import com.intellij.psi.util.CachedValueProvider;\n" +
                "\n" +
                "public class NoHighlightForEmptyResultCreateParameterList {\n" +
                "   public void method() {\n" +
                "       CachedValueProvider.Result.create<error descr=\"Cannot resolve method 'create()'\">()</error>;\n" +
                "   }\n" +
                "}");
    }

    public void testNoHighlightForMoreThanTwoDependencies() {
        doJavaTest("NoHighlightForMoreThanTwoDependencies.java",
            "import com.intellij.psi.util.CachedValueProvider;\n" +
                "import com.intellij.psi.PsiMethod;\n" +
                "import com.intellij.psi.PsiClass;\n" +
                "\n" +
                "public class NoHighlightForMoreThanTwoDependencies {\n" +
                "   public void method(PsiClass psiClass, PsiMethod psiMethod) {\n" +
                "       CachedValueProvider.Result.create(new Object[1], psiClass, psiMethod);\n" +
                "   }\n" +
                "}");
    }

    //new Result()

    public void testNeverChangedIsAddedForEmptyDependencyNewResult() {
        doQuickFixTest("Add ModificationTracker.NEVER_CHANGED as dependency", "NeverChangedIsAddedForEmptyDependency.java",
            "import com.intellij.psi.util.CachedValueProvider;\n" +
                "\n" +
                "public class NeverChangedIsAddedForEmptyDependency {\n" +
                "   public void method() {\n" +
                "       new CachedValueProvider.Res<caret>ult<>(new Object[1]);\n" +
                "   }\n" +
                "}",
            "import com.intellij.openapi.util.ModificationTracker;\n" +
                "import com.intellij.psi.util.CachedValueProvider;\n" +
                "\n" +
                "public class NeverChangedIsAddedForEmptyDependency {\n" +
                "   public void method() {\n" +
                "       new CachedValueProvider.Result<>(new Object[1], ModificationTracker.NEVER_CHANGED);\n" +
                "   }\n" +
                "}");
    }

    public void testNeverChangedIsAddedForEmptyCollectionDependencyNewResult() {
        doQuickFixTest("Replace empty collection with ModificationTracker.NEVER_CHANGED", "NeverChangedIsAddedForEmptyCollectionDependency.java",
            "import com.intellij.psi.util.CachedValueProvider;\n" +
                "import java.util.Collections;\n" +
                "\n" +
                "public class NeverChangedIsAddedForEmptyCollectionDependency {\n" +
                "   public void method() {\n" +
                "       new CachedValueProvider.Res<caret>ult<>(new Object[1], Collections.emptyList());\n" +
                "   }\n" +
                "}",
            "import com.intellij.openapi.util.ModificationTracker;\n" +
                "import com.intellij.psi.util.CachedValueProvider;\n" +
                "import java.util.Collections;\n" +
                "\n" +
                "public class NeverChangedIsAddedForEmptyCollectionDependency {\n" +
                "   public void method() {\n" +
                "       new CachedValueProvider.Res<caret>ult<>(new Object[1], ModificationTracker.NEVER_CHANGED);\n" +
                "   }\n" +
                "}");
    }

    public void testNoHighlightForNonEmptyDependenciesNewResult() {
        doJavaTest("",
            "import com.intellij.psi.util.CachedValueProvider;\n" +
                "import com.intellij.psi.PsiClass;\n" +
                "\n" +
                "public class NoHighlightForNonEmptyDependencies {\n" +
                "   public void method(PsiClass psiClass) {\n" +
                "       new CachedValueProvider.Result<>(new Object[1], psiClass);\n" +
                "   }\n" +
                "}");
    }

    public void testNoHighlightForModificationTrackerNeverChangedDependencyNewResult() {
        doJavaTest("NoHighlightForModificationTrackerNeverChangedDependency.java",
            "import com.intellij.psi.util.CachedValueProvider;\n" +
                "import com.intellij.openapi.util.ModificationTracker;\n" +
                "import com.intellij.psi.PsiClass;\n" +
                "\n" +
                "public class NoHighlightForModificationTrackerNeverChangedDependency {\n" +
                "   public void method(PsiClass psiClass) {\n" +
                "       new CachedValueProvider.Result<>(new Object[1], ModificationTracker.NEVER_CHANGED);\n" +
                "   }\n" +
                "}");
    }

    public void testNoHighlightForEmptyResultCreateParameterListNewResult() {
        doJavaTest("NoHighlightForEmptyResultCreateParameterList.java",
            "import com.intellij.psi.util.CachedValueProvider;\n" +
                "\n" +
                "public class NoHighlightForEmptyResultCreateParameterList {\n" +
                "   public void method() {\n" +
                "       new CachedValueProvider.Result<error descr=\"Cannot infer arguments (unable to resolve constructor)\"><></error>();\n" +
                "   }\n" +
                "}");
    }

    public void testNoHighlightForMoreThanTwoDependenciesNewResult() {
        doJavaTest("NoHighlightForMoreThanTwoDependencies.java",
            "import com.intellij.psi.util.CachedValueProvider;\n" +
                "import com.intellij.psi.PsiMethod;\n" +
                "import com.intellij.psi.PsiClass;\n" +
                "\n" +
                "public class NoHighlightForMoreThanTwoDependencies {\n" +
                "   public void method(PsiClass psiClass, PsiMethod psiMethod) {\n" +
                "       new CachedValueProvider.Result<>(new Object[1], psiClass, psiMethod);\n" +
                "   }\n" +
                "}");
    }
}
