//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting

import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiExpression
import com.intellij.psi.search.ProjectScope

internal class PsiClassFinder {
    companion object {
        @JvmStatic
        fun findClass(expression: PsiExpression): PsiClass? {
            val evaluated = evaluate(expression)
            return if (evaluated != null) findClass(
                evaluated.toString(),
                expression.project
            ) else null
        }

        internal fun evaluate(expression: PsiExpression): Any? {
            return JavaPsiFacade.getInstance(expression.project).constantEvaluationHelper.computeConstantExpression(expression, true)
        }

        internal fun findClass(text: String, project: Project): PsiClass? {
            return JavaPsiFacade.getInstance(project).findClass(text, ProjectScope.getAllScope(project))
        }
    }
}