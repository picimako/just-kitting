//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.ReadAction.compute
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiField
import com.intellij.psi.search.ProjectScope

/**
 * Project service to store Psi elements referencing the [Service] annotation, and properties of [Service.Level].
 *
 * @since 0.1.0
 */
@Service(Service.Level.PROJECT)
class PlatformPsiCache(val project: Project) {

    val serviceAnnotation: PsiClass? by lazy { findClass(PlatformNames.SERVICE_ANNOTATION) }
    val serviceLevelProject: PsiField? by lazy { findClass(PlatformNames.SERVICE_LEVEL)!!.findFieldByName("PROJECT", false) }
    val serviceLevelApp: PsiField? by lazy { findClass(PlatformNames.SERVICE_LEVEL)!!.findFieldByName("APP", false) }
    val callMatcher: PsiClass? by lazy { findClass(PlatformNames.CALL_MATCHER) }

    private fun findClass(name: String): PsiClass? =
        compute<PsiClass?, Exception> { JavaPsiFacade.getInstance(project).findClass(name, ProjectScope.getLibrariesScope(project)) }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): PlatformPsiCache = project.service()
    }
}
