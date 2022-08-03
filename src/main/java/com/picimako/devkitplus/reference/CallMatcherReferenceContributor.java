//Copyright 2021 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.devkitplus.reference;

import static com.intellij.json.psi.JsonPsiUtil.stripQuotes;
import static com.intellij.psi.util.PsiLiteralUtil.isUnsafeLiteral;
import static com.picimako.devkitplus.CallMatcherUtil.ARGUMENT_OF_CALL_MATCHER_PATTERN;
import static com.picimako.devkitplus.CallMatcherUtil.CALL_MATCHER_EXACT_INSTANCE_MATCHER;
import static com.picimako.devkitplus.CallMatcherUtil.CALL_MATCHER_STATIC_MATCHER;
import static com.picimako.devkitplus.CallMatcherUtil.filterByNonStatic;
import static com.picimako.devkitplus.CallMatcherUtil.filterByStatic;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.SmartList;
import com.siyeh.ig.callMatcher.CallMatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Adds references to the arguments of {@link CallMatcher} static factory methods: {@code staticCall}, {@code instanceCall}, {@code exactInstanceCall}.
 * The first one is the fully qualified name of the referenced class, while the rest are methods of said class.
 * <p>
 * Upon resolving the references, the IDE navigates to the resolved class definition.
 * <p>
 * In case of methods, if the call is {@link CallMatcher#exactInstanceCall(String, String...)},
 * then it provides references for methods exactly in the referenced class,
 * otherwise references are provided for methods in the super classes too.
 * <p>
 * If the call is {@link CallMatcher#staticCall(String, String...)}, it provides references only for static methods.
 *
 * @since 0.1.0
 */
public class CallMatcherReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
            ARGUMENT_OF_CALL_MATCHER_PATTERN,
            new PsiReferenceProvider() {
                @Override
                public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                    var parentCall = PsiTreeUtil.getParentOfType(element, PsiMethodCallExpression.class);
                    if (parentCall == null) return PsiReference.EMPTY_ARRAY;

                    var callMatcherArguments = parentCall.getArgumentList();
                    final var reference = new SmartList<PsiReference>();
                    //If the current literal is the first argument (the class FQN) of the CallMatcher call
                    if (element.getManager().areElementsEquivalent(element, callMatcherArguments.getExpressions()[0])) {
                        if (!isUnsafeLiteral((PsiLiteralExpression) element)) {
                            Optional.ofNullable(findClass(element))
                                .ifPresent(referencedClass -> reference.add(new CallMatcherArgReference(element, () -> new PsiElement[]{referencedClass})));
                        }
                    }
                    //If there is at least one method name specified
                    else if (callMatcherArguments.getExpressionCount() > 1 && !isUnsafeLiteral((PsiLiteralExpression) element)) {
                        var className = parentCall.getArgumentList().getExpressions()[0];
                        //If the classname is a String we can simplify find the class by it, otherwise first we have to evaluate the expression
                        var refClass = className instanceof PsiLiteralExpression
                            ? Optional.ofNullable(findClass(className))
                            : Optional.ofNullable(evaluate(className)).map(cls -> findClass(cls.toString(), element.getProject()));
                        refClass.ifPresent(referencedClass -> reference.add(new CallMatcherArgReference(element, () -> getMethodsByName(element, referencedClass, parentCall))));
                    }
                    return !reference.isEmpty() ? reference.toArray(PsiReference[]::new) : PsiReference.EMPTY_ARRAY;
                }
            });
    }

    private static Object evaluate(PsiExpression expression) {
        return JavaPsiFacade.getInstance(expression.getProject()).getConstantEvaluationHelper().computeConstantExpression(expression, true);
    }

    @Nullable
    public static PsiClass findClass(@NotNull PsiElement element) {
        return findClass(stripQuotes(element.getText()), element.getProject());
    }

    @Nullable
    private static PsiClass findClass(@NotNull String text, Project project) {
        return JavaPsiFacade.getInstance(project).findClass(text, ProjectScope.getAllScope(project));
    }

    @NotNull
    public static PsiMethod[] getMethodsByName(PsiElement element, @NotNull PsiClass referencedClass, PsiMethodCallExpression parentCall) {
        var methodsInClass = referencedClass.findMethodsByName(stripQuotes(element.getText()), !CALL_MATCHER_EXACT_INSTANCE_MATCHER.matches(parentCall));
        return CALL_MATCHER_STATIC_MATCHER.matches(parentCall) ? filterByStatic(methodsInClass) : filterByNonStatic(methodsInClass);
    }

    /**
     * Reference implementation to for class FQN and method name string literals in {@code CallMatcher} factory method arguments.
     */
    private static final class CallMatcherArgReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
        private final Supplier<PsiElement[]> elementsToResolveTo;

        public CallMatcherArgReference(@NotNull PsiElement element, Supplier<PsiElement[]> elementsToResolveTo) {
            super(element, TextRange.create(1, element.getTextRange().getLength() - 1), true);
            this.elementsToResolveTo = elementsToResolveTo;
        }

        @Override
        public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
            return !incompleteCode
                ? Arrays.stream(elementsToResolveTo.get()).map(PsiElementResolveResult::new).toArray(ResolveResult[]::new)
                : ResolveResult.EMPTY_ARRAY;
        }

        @Override
        public @Nullable PsiElement resolve() {
            ResolveResult[] resolveResults = multiResolve(false);
            return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
        }
    }
}
