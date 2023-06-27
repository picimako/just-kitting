//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.picimako.justkitting.reference

import com.intellij.json.psi.JsonPsiUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.search.ProjectScope
import com.intellij.psi.util.PsiLiteralUtil.isUnsafeLiteral
import com.intellij.psi.util.PsiTreeUtil.getParentOfType
import com.intellij.util.ProcessingContext
import com.intellij.util.SmartList
import com.picimako.justkitting.CallMatcherUtil
import java.util.function.Supplier

/**
 * Adds references to the arguments of [com.siyeh.ig.callMatcher.CallMatcher] static factory methods: `staticCall`, `instanceCall`, `exactInstanceCall`.
 * The first one is the fully qualified name of the referenced class, while the rest are methods of said class.
 *
 * Upon resolving the references, the IDE navigates to the resolved class definition.
 *
 * In case of methods, if the call is [com.siyeh.ig.callMatcher.CallMatcher.exactInstanceCall],
 * then it provides references for methods exactly in the referenced class,
 * otherwise references are provided for methods in the super classes too.
 *
 * If the call is [com.siyeh.ig.callMatcher.CallMatcher.staticCall], it provides references only for static methods.
 *
 * @since 0.1.0
 */
class CallMatcherReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            CallMatcherUtil.ARGUMENT_OF_CALL_MATCHER_PATTERN,
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
                    val parentCall = getParentOfType(element, PsiMethodCallExpression::class.java)
                        ?: return PsiReference.EMPTY_ARRAY

                    val callMatcherArguments = parentCall.argumentList
                    val reference = SmartList<PsiReference>()

                    //If the current literal is the first argument (the class FQN) of the CallMatcher call
                    if (element.manager.areElementsEquivalent(element, callMatcherArguments.expressions[0])) {
                        if (!isUnsafeLiteral(element as PsiLiteralExpression)) {
                            findClass(element)?.let {
                                reference.add(CallMatcherArgReference(element) { arrayOf(it) })
                            }
                        }
                    } else if (callMatcherArguments.expressionCount > 1 && !isUnsafeLiteral(element as PsiLiteralExpression)) {
                        val className = parentCall.argumentList.expressions[0]
                        //If the classname is a String we can simply find the class by it, otherwise first we have to evaluate the expression
                        val referencedClass: PsiClass? =
                            if (className is PsiLiteralExpression) findClass(className)
                            else evaluate(className)?.let { findClass(it.toString(), element.getProject()) }

                        //Mapping the PsiMethods to 'it', so that they are passed as PsiElements
                        referencedClass?.let { reference.add(CallMatcherArgReference(element) { getMethodsByName(element, it, parentCall).map { it }.toTypedArray() }) }
                    }
                    return if (reference.isNotEmpty()) reference.toTypedArray() else PsiReference.EMPTY_ARRAY
                }
            })
    }

    /**
     * Reference implementation to for class FQN and method name string literals in `CallMatcher` factory method arguments.
     */
    private class CallMatcherArgReference(element: PsiElement, private val elementsToResolveTo: Supplier<Array<PsiElement>>)
        : PsiReferenceBase<PsiElement?>(element, TextRange.create(1, element.textRange.length - 1), true), PsiPolyVariantReference {

        override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
            return if (!incompleteCode)
                elementsToResolveTo.get()
                    .map { element: PsiElement -> PsiElementResolveResult(element) }
                    .toTypedArray()
            else ResolveResult.EMPTY_ARRAY
        }

        override fun resolve(): PsiElement? {
            val resolveResults = multiResolve(false)
            return if (resolveResults.size == 1) resolveResults[0].element else null
        }
    }

    companion object {
        @JvmStatic
        fun findClass(element: PsiElement): PsiClass? {
            return findClass(JsonPsiUtil.stripQuotes(element.text), element.project)
        }

        private fun evaluate(expression: PsiExpression): Any? {
            return JavaPsiFacade.getInstance(expression.project).constantEvaluationHelper.computeConstantExpression(expression, true)
        }

        private fun findClass(text: String, project: Project): PsiClass? {
            return JavaPsiFacade.getInstance(project).findClass(text, ProjectScope.getAllScope(project))
        }

        private fun getMethodsByName(element: PsiElement, referencedClass: PsiClass, parentCall: PsiMethodCallExpression?): Array<PsiMethod> {
            val methodsInClass = referencedClass.findMethodsByName(
                JsonPsiUtil.stripQuotes(element.text),
                !CallMatcherUtil.CALL_MATCHER_EXACT_INSTANCE_MATCHER.matches(parentCall))

            return if (CallMatcherUtil.CALL_MATCHER_STATIC_MATCHER.matches(parentCall))
                CallMatcherUtil.filterByStatic(methodsInClass)
            else
                CallMatcherUtil.filterByNonStatic(methodsInClass)
        }
    }
}
