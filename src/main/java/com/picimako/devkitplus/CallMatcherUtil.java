//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.devkitplus;

import static com.picimako.devkitplus.PlatformNames.CALL_MATCHER;
import static com.intellij.patterns.PlatformPatterns.psiElement;
import static com.siyeh.ig.callMatcher.CallMatcher.staticCall;

import java.util.Arrays;

import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiModifier;
import com.intellij.util.ProcessingContext;
import com.siyeh.ig.callMatcher.CallMatcher;
import org.jetbrains.annotations.NotNull;

/**
 * Utility to help work with {@link CallMatcher} calls.
 */
public final class CallMatcherUtil {

    public static final PsiElementPattern.Capture<PsiLiteralExpression> ARGUMENT_OF_CALL_MATCHER_PATTERN = psiElement(PsiLiteralExpression.class)
        //If the string literal has a CallMatcher factory method as parent with at least one argument specified
        .withSuperParent(2, psiElement(PsiMethodCallExpression.class)
            .with(new PatternCondition<>("") {
                @Override
                public boolean accepts(@NotNull PsiMethodCallExpression expression, ProcessingContext context) {
                    return CALL_MATCHER_MATCHER.matches(expression) && !expression.getArgumentList().isEmpty();
                }
            }));

    public static final CallMatcher CALL_MATCHER_MATCHER = staticCall(CALL_MATCHER, "staticCall", "instanceCall", "exactInstanceCall");
    public static final CallMatcher CALL_MATCHER_EXACT_INSTANCE_MATCHER = staticCall(CALL_MATCHER, "exactInstanceCall");
    public static final CallMatcher CALL_MATCHER_STATIC_MATCHER = staticCall(CALL_MATCHER, "staticCall");
    public static final CallMatcher CALL_MATCHER_INSTANCE_MATCHER = staticCall(CALL_MATCHER, "instanceCall");

    /**
     * Returns the non-static PSI methods from the argument collection.
     *
     * @param methods the methods to filter
     */
    @NotNull
    public static PsiMethod[] filterByNonStatic(PsiMethod[] methods) {
        return methods.length == 0 ? methods : Arrays.stream(methods).filter(method -> !method.hasModifierProperty(PsiModifier.STATIC)).toArray(PsiMethod[]::new);
    }

    /**
     * Returns the static PSI methods from the argument collection.
     *
     * @param methods the methods to filter
     */
    @NotNull
    public static PsiMethod[] filterByStatic(PsiMethod[] methods) {
        return methods.length == 0 ? methods : Arrays.stream(methods).filter(method -> method.hasModifierProperty(PsiModifier.STATIC)).toArray(PsiMethod[]::new);
    }

    private CallMatcherUtil() {
        //Utility class
    }
}
