//Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.inspection;

import static com.intellij.json.psi.JsonPsiUtil.stripQuotes;
import static com.intellij.psi.util.PsiLiteralUtil.isUnsafeLiteral;
import static com.picimako.justkitting.CallMatcherUtil.ARGUMENT_OF_CALL_MATCHER_PATTERN;
import static com.picimako.justkitting.CallMatcherUtil.CALL_MATCHER_EXACT_INSTANCE_MATCHER;
import static com.picimako.justkitting.CallMatcherUtil.CALL_MATCHER_INSTANCE_MATCHER;
import static com.picimako.justkitting.CallMatcherUtil.CALL_MATCHER_STATIC_MATCHER;
import static com.picimako.justkitting.CallMatcherUtil.filterByNonStatic;
import static com.picimako.justkitting.CallMatcherUtil.filterByStatic;
import static com.picimako.justkitting.reference.CallMatcherReferenceContributor.findClass;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.util.PsiTreeUtil;
import com.picimako.justkitting.resources.JustKittingBundle;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Reports method name arguments of calls to {@link com.siyeh.ig.callMatcher.CallMatcher} if they don't exist in the current context of the call matcher.
 * <p>
 * If...
 * <ul>
 *     <li>the call is {@link com.siyeh.ig.callMatcher.CallMatcher#instanceCall(String, String...)}, problem is registered only when the method doesn't
 *     exist as an instance method in the class or any of its super classes.</li>
 *     <li>the call is {@link com.siyeh.ig.callMatcher.CallMatcher#exactInstanceCall(String, String...)}, problem is registered only when the method doesn't
 *     exist as an instance method in the class.</li>
 *     <li>the call is {@link com.siyeh.ig.callMatcher.CallMatcher#staticCall(String, String...)}, problem is registered only when the method doesn't
 *     exist as a static method in the class or any of its super classes.</li>
 * </ul>
 *
 * @since 0.1.0
 */
public class CallMatcherInspection extends LocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitLiteralExpression(PsiLiteralExpression literalExpr) {
                if (!ARGUMENT_OF_CALL_MATCHER_PATTERN.accepts(literalExpr)) return;

                var parentCall = PsiTreeUtil.getParentOfType(literalExpr, PsiMethodCallExpression.class);
                if (parentCall == null) return;

                var referencedClassFqn = parentCall.getArgumentList().getExpressions()[0];
                if (!literalExpr.getManager().areElementsEquivalent(literalExpr, referencedClassFqn) && !isUnsafeLiteral(literalExpr)) {
                    Optional.ofNullable(findClass(referencedClassFqn))
                        .ifPresent(referencedClass -> {
                            var methodCountAndMessage = getMethodCountAndMessage(literalExpr, referencedClass, parentCall);
                            if (!methodCountAndMessage.equals(Pair.empty()) && methodCountAndMessage.first == 0) {
                                holder.registerProblem(literalExpr, JustKittingBundle.message(methodCountAndMessage.second), ProblemHighlightType.LIKE_UNKNOWN_SYMBOL);
                            }
                        });
                }
            }

            /**
             * Returns the method count for the provided method name filtered by the CallMatcher type,
             * paired with the appropriate inspection message key.
             *
             * @param methodNameArg a method name String literal in the CallMatcher creation
             * @param referencedClass the PsiClass the CallMatcher references
             * @param parentCall the type of CallMatcher (instanceCall, exactInstanceCall, staticCall)
             */
            @NotNull
            private Pair<Integer, String> getMethodCountAndMessage(PsiElement methodNameArg, @NotNull PsiClass referencedClass, PsiMethodCallExpression parentCall) {
                boolean isExactInstance = CALL_MATCHER_EXACT_INSTANCE_MATCHER.matches(parentCall);
                PsiMethod[] methodsInClass = referencedClass.findMethodsByName(stripQuotes(methodNameArg.getText()), !isExactInstance);
                if (isExactInstance) {
                    return Pair.create(filterByNonStatic(methodsInClass).length, "inspection.call.matcher.no.exact.instance.method.with.name");
                } else if (CALL_MATCHER_STATIC_MATCHER.matches(parentCall)) {
                    return Pair.create(filterByStatic(methodsInClass).length, "inspection.call.matcher.no.static.method.with.name");
                } else if (CALL_MATCHER_INSTANCE_MATCHER.matches(parentCall)) {
                    return Pair.create(filterByNonStatic(methodsInClass).length, "inspection.call.matcher.no.instance.method.with.name");
                }
                return Pair.empty();
            }
        };
    }
}
