//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.inspection;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.picimako.justkitting.ThirdPartyLibraryLoader;

/**
 * Functional test for {@link CachedValuesInspection}.
 */
public class CachedValuesInspectionTest extends JustKittingInspectionTestBase {

    @Override
    protected String getTestDataPath() {
        return super.getTestDataPath() + "inspection";
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ThirdPartyLibraryLoader.loadUtil8(myFixture);
        ThirdPartyLibraryLoader.loadJavaImpl(myFixture);
    }

    @Override
    protected InspectionProfileEntry getInspection() {
        return new CachedValuesInspection();
    }

    //Result.create()

    public void testNeverChangedIsAddedForEmptyDependency() {
        doQuickFixTest("Add ModificationTracker.NEVER_CHANGED as dependency", "NeverChangedIsAddedForEmptyDependency.java",
            """
                import com.intellij.psi.util.CachedValueProvider;

                public class NeverChangedIsAddedForEmptyDependency {
                   public void method() {
                       CachedValueProvider.Result.cre<caret>ate(new Object[1]);
                   }
                }""",
            """
                import com.intellij.openapi.util.ModificationTracker;
                import com.intellij.psi.util.CachedValueProvider;

                public class NeverChangedIsAddedForEmptyDependency {
                   public void method() {
                       CachedValueProvider.Result.create(new Object[1], ModificationTracker.NEVER_CHANGED);
                   }
                }""");
    }

    public void testModificationCountIsAddedForEmptyDependency() {
        doQuickFixTest("Add PsiModificationTracker.MODIFICATION_COUNT as dependency", "ModificationCountIsAddedForEmptyDependency.java",
            """
                import com.intellij.psi.util.CachedValueProvider;

                public class ModificationCountIsAddedForEmptyDependency {
                   public void method() {
                       CachedValueProvider.Result.cre<caret>ate(new Object[1]);
                   }
                }""",
            """
                import com.intellij.psi.util.CachedValueProvider;
                import com.intellij.psi.util.PsiModificationTracker;

                public class ModificationCountIsAddedForEmptyDependency {
                   public void method() {
                       CachedValueProvider.Result.create(new Object[1], PsiModificationTracker.MODIFICATION_COUNT);
                   }
                }""");
    }

    public void testNeverChangedIsAddedForEmptyCollectionDependency() {
        doQuickFixTest("Replace empty collection with ModificationTracker.NEVER_CHANGED", "NeverChangedIsAddedForEmptyCollectionDependency.java",
            """
                import com.intellij.psi.util.CachedValueProvider;
                import java.util.Collections;

                public class NeverChangedIsAddedForEmptyCollectionDependency {
                   public void method() {
                       CachedValueProvider.Result.cre<caret>ate(new Object[1], Collections.emptyList());
                   }
                }""",
            """
                import com.intellij.openapi.util.ModificationTracker;
                import com.intellij.psi.util.CachedValueProvider;
                import java.util.Collections;

                public class NeverChangedIsAddedForEmptyCollectionDependency {
                   public void method() {
                       CachedValueProvider.Result.create(new Object[1], ModificationTracker.NEVER_CHANGED);
                   }
                }""");
    }

    public void testModificationCountIsAddedForEmptyCollectionDependency() {
        doQuickFixTest("Replace empty collection with PsiModificationTracker.MODIFICATION_COUNT", "ModificationCountIsAddedForEmptyCollectionDependency.java",
            """
                import com.intellij.psi.util.CachedValueProvider;
                import java.util.Collections;

                public class ModificationCountIsAddedForEmptyCollectionDependency {
                   public void method() {
                       CachedValueProvider.Result.cre<caret>ate(new Object[1], Collections.emptyList());
                   }
                }""",
            """
                import com.intellij.psi.util.CachedValueProvider;
                import com.intellij.psi.util.PsiModificationTracker;

                import java.util.Collections;

                public class ModificationCountIsAddedForEmptyCollectionDependency {
                   public void method() {
                       CachedValueProvider.Result.create(new Object[1], PsiModificationTracker.MODIFICATION_COUNT);
                   }
                }""");
    }

    public void testNoHighlightForNonEmptyDependencies() {
        doJavaTest("NoHighlightForNonEmptyDependencies.java",
            """
                import com.intellij.psi.util.CachedValueProvider;
                import com.intellij.psi.PsiClass;

                public class NoHighlightForNonEmptyDependencies {
                   public void method(PsiClass psiClass) {
                       CachedValueProvider.Result.create(new Object[1], psiClass);
                   }
                }""");
    }

    public void testNoHighlightForModificationTrackerNeverChangedDependency() {
        doJavaTest("NoHighlightForModificationTrackerNeverChangedDependency.java",
            """
                import com.intellij.psi.util.CachedValueProvider;
                import com.intellij.openapi.util.ModificationTracker;
                import com.intellij.psi.PsiClass;

                public class NoHighlightForModificationTrackerNeverChangedDependency {
                   public void method(PsiClass psiClass) {
                       CachedValueProvider.Result.create(new Object[1], ModificationTracker.NEVER_CHANGED);
                   }
                }""");
    }

    public void testNoHighlightForEmptyResultCreateParameterList() {
        doJavaTest("NoHighlightForEmptyResultCreateParameterList.java",
            """
                import com.intellij.psi.util.CachedValueProvider;

                public class NoHighlightForEmptyResultCreateParameterList {
                   public void method() {
                       CachedValueProvider.Result.create<error descr="Cannot resolve method 'create()'">()</error>;
                   }
                }""");
    }

    public void testNoHighlightForMoreThanTwoDependencies() {
        doJavaTest("NoHighlightForMoreThanTwoDependencies.java",
            """
                import com.intellij.psi.util.CachedValueProvider;
                import com.intellij.psi.PsiMethod;
                import com.intellij.psi.PsiClass;

                public class NoHighlightForMoreThanTwoDependencies {
                   public void method(PsiClass psiClass, PsiMethod psiMethod) {
                       CachedValueProvider.Result.create(new Object[1], psiClass, psiMethod);
                   }
                }""");
    }

    //new Result()

    public void testNeverChangedIsAddedForEmptyDependencyNewResult() {
        doQuickFixTest("Add ModificationTracker.NEVER_CHANGED as dependency", "NeverChangedIsAddedForEmptyDependency.java",
            """
                import com.intellij.psi.util.CachedValueProvider;

                public class NeverChangedIsAddedForEmptyDependency {
                   public void method() {
                       new CachedValueProvider.Res<caret>ult<>(new Object[1]);
                   }
                }""",
            """
                import com.intellij.openapi.util.ModificationTracker;
                import com.intellij.psi.util.CachedValueProvider;

                public class NeverChangedIsAddedForEmptyDependency {
                   public void method() {
                       new CachedValueProvider.Result<>(new Object[1], ModificationTracker.NEVER_CHANGED);
                   }
                }""");
    }

    public void testNeverChangedIsAddedForEmptyCollectionDependencyNewResult() {
        doQuickFixTest("Replace empty collection with ModificationTracker.NEVER_CHANGED", "NeverChangedIsAddedForEmptyCollectionDependency.java",
            """
                import com.intellij.psi.util.CachedValueProvider;
                import java.util.Collections;

                public class NeverChangedIsAddedForEmptyCollectionDependency {
                   public void method() {
                       new CachedValueProvider.Res<caret>ult<>(new Object[1], Collections.emptyList());
                   }
                }""",
            """
                import com.intellij.openapi.util.ModificationTracker;
                import com.intellij.psi.util.CachedValueProvider;
                import java.util.Collections;

                public class NeverChangedIsAddedForEmptyCollectionDependency {
                   public void method() {
                       new CachedValueProvider.Res<caret>ult<>(new Object[1], ModificationTracker.NEVER_CHANGED);
                   }
                }""");
    }

    public void testNoHighlightForNonEmptyDependenciesNewResult() {
        doJavaTest("NoHighlightForNonEmptyDependencies.java",
            """
                import com.intellij.psi.util.CachedValueProvider;
                import com.intellij.psi.PsiClass;

                public class NoHighlightForNonEmptyDependencies {
                   public void method(PsiClass psiClass) {
                       new CachedValueProvider.Result<>(new Object[1], psiClass);
                   }
                }""");
    }

    public void testNoHighlightForModificationTrackerNeverChangedDependencyNewResult() {
        doJavaTest("NoHighlightForModificationTrackerNeverChangedDependency.java",
            """
                import com.intellij.psi.util.CachedValueProvider;
                import com.intellij.openapi.util.ModificationTracker;
                import com.intellij.psi.PsiClass;

                public class NoHighlightForModificationTrackerNeverChangedDependency {
                   public void method(PsiClass psiClass) {
                       new CachedValueProvider.Result<>(new Object[1], ModificationTracker.NEVER_CHANGED);
                   }
                }""");
    }

    public void testNoHighlightForEmptyResultCreateParameterListNewResult() {
        doJavaTest("NoHighlightForEmptyResultCreateParameterList.java",
            """
                import com.intellij.psi.util.CachedValueProvider;

                public class NoHighlightForEmptyResultCreateParameterList {
                   public void method() {
                       new CachedValueProvider.Result<><error descr="Cannot resolve constructor 'Result()'">()</error>;
                   }
                }""");
    }

    public void testNoHighlightForMoreThanTwoDependenciesNewResult() {
        doJavaTest("NoHighlightForMoreThanTwoDependencies.java",
            """
                import com.intellij.psi.util.CachedValueProvider;
                import com.intellij.psi.PsiMethod;
                import com.intellij.psi.PsiClass;

                public class NoHighlightForMoreThanTwoDependencies {
                   public void method(PsiClass psiClass, PsiMethod psiMethod) {
                       new CachedValueProvider.Result<>(new Object[1], psiClass, psiMethod);
                   }
                }""");
    }
}
