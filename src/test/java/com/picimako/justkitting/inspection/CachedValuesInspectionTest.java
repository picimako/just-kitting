//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.inspection;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.picimako.justkitting.ThirdPartyLibraryLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Functional test for {@link CachedValuesInspection}.
 */
public final class CachedValuesInspectionTest extends JustKittingInspectionTestBase {

    @Override
    protected String getTestDataPath() {
        return super.getTestDataPath() + "inspection";
    }

    @BeforeEach
    protected void setUp() {
        ThirdPartyLibraryLoader.loadUtil8(getFixture());
        ThirdPartyLibraryLoader.loadJavaImpl(getFixture());
    }

    @Override
    protected InspectionProfileEntry getInspection() {
        return new CachedValuesInspection();
    }

    //Result.create()

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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
