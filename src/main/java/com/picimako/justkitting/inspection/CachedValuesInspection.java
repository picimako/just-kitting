//Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.inspection;

import static com.picimako.justkitting.PlatformNames.CACHED_VALUE_PROVIDER_RESULT;
import static com.siyeh.ig.callMatcher.CallMatcher.staticCall;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.project.Project;
import com.intellij.psi.CommonClassNames;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiCall;
import com.intellij.psi.PsiCallExpression;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiExpressionList;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiNewExpression;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.picimako.justkitting.resources.JustKittingBundle;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.callMatcher.CallMatcher;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Reports calls to the {@code create()} methods and the constructor of {@link com.intellij.psi.util.CachedValueProvider.Result} in which there is no dependency
 * specified, or it is an empty collection.
 * <p>
 * In this case the {@code Result} class constructor would log an error level message.
 * <p>
 * The following empty collection factory methods are considered during validation:
 * <ul>
 *     <li>{@code List.of()}</li>
 *     <li>{@code Set.of()}</li>
 *     <li>{@code Collections.emptyList()}</li>
 *     <li>{@code Collections.emptySet()}</li>
 * </ul>
 *
 * @see com.intellij.psi.util.CachedValueProvider.Result
 * @since 0.1.0
 */
public class CachedValuesInspection extends LocalInspectionTool {

    private static final CallMatcher RESULT_CREATE_MATCHER = staticCall(CACHED_VALUE_PROVIDER_RESULT, "create");
    private static final CallMatcher EMPTY_COLLECTION_MATCHER = CallMatcher.anyOf(
        staticCall(CommonClassNames.JAVA_UTIL_LIST, "of"),
        staticCall(CommonClassNames.JAVA_UTIL_SET, "of"),
        staticCall(CommonClassNames.JAVA_UTIL_COLLECTIONS, "emptyList", "emptySet"));

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitMethodCallExpression(@NotNull PsiMethodCallExpression expression) {
                //If it is a static call to 'CachedValueProvider.Result.create()'
                if (expression.getMethodExpression().getReferenceNameElement() != null && RESULT_CREATE_MATCHER.matches(expression)) {
                    checkForMissingOrEmptyDependencies(expression.getArgumentList(), () -> expression.getMethodExpression().getReferenceNameElement(), PsiMethodCallExpression.class);
                }
            }

            @Override
            public void visitNewExpression(@NotNull PsiNewExpression expression) {
                //If it is a constructor call to 'new CachedValueProvider.Result()'
                if (Optional.ofNullable(expression.getClassOrAnonymousClassReference())
                    .filter(cls -> CACHED_VALUE_PROVIDER_RESULT.equals(cls.getQualifiedName()))
                    .isPresent()) {
                    checkForMissingOrEmptyDependencies(expression.getArgumentList(), () -> expression.getClassOrAnonymousClassReference().getReferenceNameElement(), PsiNewExpression.class);
                }
            }

            @Override
            public void visitCallExpression(@NotNull PsiCallExpression callExpression) {
                //Other types of calls are out of scope
            }

            private void checkForMissingOrEmptyDependencies(@Nullable PsiExpressionList arguments,
                                                            Supplier<PsiElement> problemElement,
                                                            Class<? extends PsiCall> expressionType) {
                if (arguments == null) return;

                //If only the 'value' parameter is specified, but no dependency
                if (arguments.getExpressionCount() == 1) {
                    holder.registerProblem(problemElement.get(),
                        JustKittingBundle.message("inspection.cached.value.provider.result.without.dependency"),
                        new AddDependencyQuickFix(expressionType, ModificationTracker.MODIFICATION_TRACKER_NEVER_CHANGED),
                        new AddDependencyQuickFix(expressionType, ModificationTracker.PSI_MODIFICATION_TRACKER_MODIFICATION_COUNT));
                }
                //If there is a dependency specified as an empty Collection, defined by EMPTY_COLLECTION_MATCHER
                else if (arguments.getExpressionCount() == 2
                    && arguments.getExpressions()[1] instanceof PsiMethodCallExpression
                    && EMPTY_COLLECTION_MATCHER.matches(arguments.getExpressions()[1])) {
                    holder.registerProblem(problemElement.get(),
                        JustKittingBundle.message("inspection.cached.value.provider.result.without.dependency"),
                        new ReplaceDependencyQuickFix(expressionType, ModificationTracker.MODIFICATION_TRACKER_NEVER_CHANGED),
                        new ReplaceDependencyQuickFix(expressionType, ModificationTracker.PSI_MODIFICATION_TRACKER_MODIFICATION_COUNT));
                }
            }
        };
    }

    // ---- Quick fixes ----

    /**
     * Adds a modification tracker as a dependency to the CachedValueProvider.Result creation.
     */
    private static final class AddDependencyQuickFix extends BaseCachingQuickFix {
        public AddDependencyQuickFix(@NotNull Class<? extends PsiCall> expressionType, ModificationTracker modificationTracker) {
            super(expressionType, modificationTracker, "inspection.cached.value.provider.add.dependency.quick.fix");
        }

        @Override
        protected void doFix(@NotNull Project project, ProblemDescriptor descriptor) {
            modificationTracker.addDependency(descriptor.getPsiElement(), expressionType, project);
        }
    }

    /**
     * Replaces the empty collection dependency with a modification tracker, in the CachedValueProvider.Result creation.
     */
    private static final class ReplaceDependencyQuickFix extends BaseCachingQuickFix {
        public ReplaceDependencyQuickFix(Class<? extends PsiCall> expressionType, ModificationTracker modificationTracker) {
            super(expressionType, modificationTracker, "inspection.cached.value.provider.replace.with.dependency.quick.fix");
        }

        @Override
        protected void doFix(@NotNull Project project, ProblemDescriptor descriptor) {
            modificationTracker.replaceEmptyDependency(descriptor.getPsiElement(), expressionType, project);
        }
    }

    private abstract static class BaseCachingQuickFix extends InspectionGadgetsFix {
        /**
         * PsiMethodCallExpression if it's a method call to 'CachedValueProvider.Result.create()',
         * or PsiNewExpression if it's a call to 'new CachedValueProvider.Result()'.
         */
        protected final Class<? extends PsiCall> expressionType;
        protected final ModificationTracker modificationTracker;
        private final String quickFixKey;

        protected BaseCachingQuickFix(@NotNull Class<? extends PsiCall> expressionType, ModificationTracker modificationTracker, String quickFixKey) {
            this.expressionType = expressionType;
            this.modificationTracker = modificationTracker;
            this.quickFixKey = quickFixKey;
        }

        @Override
        public @IntentionFamilyName @NotNull String getFamilyName() {
            return JustKittingBundle.message("inspection.cached.value.provider.add.never.changed.quick.fix.family");
        }

        @Override
        public @IntentionName @NotNull String getName() {
            return JustKittingBundle.message(quickFixKey, modificationTracker.name);
        }
    }

    //----  Modification trackers to introduce as dependency ----

    /**
     * Defines the modification tracker names and FQNs to add.
     */
    @RequiredArgsConstructor
    private enum ModificationTracker {
        MODIFICATION_TRACKER_NEVER_CHANGED("ModificationTracker.NEVER_CHANGED", "com.intellij.openapi.util.ModificationTracker.NEVER_CHANGED"),
        PSI_MODIFICATION_TRACKER_MODIFICATION_COUNT("PsiModificationTracker.MODIFICATION_COUNT", "com.intellij.psi.util.PsiModificationTracker.MODIFICATION_COUNT");

        private final String name;
        private final String fqn;

        /**
         * Adds this modification tracker to the argument list of the Result creation call.
         */
        void addDependency(PsiElement context, Class<? extends PsiCall> expressionType, Project project) {
            var resultCreate = PsiTreeUtil.getParentOfType(context, expressionType);
            resultCreate.getArgumentList().add(getElement(project, context));
        }

        /**
         * Replaces the empty collection dependency with this modification tracker in the argument list of the Result creation call.
         */
        void replaceEmptyDependency(PsiElement context, Class<? extends PsiCall> expressionType, Project project) {
            var resultCreate = PsiTreeUtil.getParentOfType(context, expressionType);
            resultCreate.getArgumentList().getExpressions()[1].replace(getElement(project, context));
        }

        private PsiElement getElement(Project project, PsiElement context) {
            return JavaCodeStyleManager.getInstance(project)
                .shortenClassReferences(JavaPsiFacade.getElementFactory(project)
                    .createExpressionFromText(fqn, context));
        }
    }
}
