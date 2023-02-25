//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiField
import com.intellij.psi.PsiSubstitutor
import com.intellij.psi.search.ProjectScope
import com.intellij.psi.util.MethodSignature

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
    val createOptionsPanelMethod: MethodSignature by lazy { findClass(PlatformNames.INSPECTION_PROFILE_ENTRY)!!.findMethodsByName("createOptionsPanel", false)[0].getSignature(PsiSubstitutor.EMPTY) }
    val multipleCheckboxOptionsPanel: PsiClass? by lazy { findClass(PlatformNames.MULTIPLE_CHECKBOX_OPTIONS_PANEL) }
    val singleCheckboxOptionsPanel: PsiClass? by lazy { findClass(PlatformNames.SINGLE_CHECKBOX_OPTIONS_PANEL) }
    val singleIntegerFieldOptionsPanel: PsiClass? by lazy { findClass(PlatformNames.SINGLE_INTEGER_FIELD_OPTIONS_PANEL) }
    val conventionOptionsPanel: PsiClass? by lazy { findClass(PlatformNames.CONVENTION_OPTIONS_PANEL) }

    private fun findClass(name: String): PsiClass? = JavaPsiFacade.getInstance(project).findClass(name, ProjectScope.getLibrariesScope(project)) 

    companion object {
        @JvmStatic
        fun getInstance(project: Project): PlatformPsiCache = project.service()
    }
}
