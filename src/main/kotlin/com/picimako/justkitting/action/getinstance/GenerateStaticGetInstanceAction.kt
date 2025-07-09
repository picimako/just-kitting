//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.action.getinstance

import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.actions.BaseCodeInsightAction
import com.intellij.lang.LanguageCodeInsightActionHandler
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction.compute
import com.intellij.openapi.components.Service.Level.APP
import com.intellij.openapi.components.Service.Level.PROJECT
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile
import com.intellij.util.containers.ContainerUtil
import com.picimako.justkitting.ListPopupHelper
import com.picimako.justkitting.ServiceLevelDecider
import com.picimako.justkitting.ServiceLevelDecider.ServiceLevel
import com.picimako.justkitting.resources.JustKittingBundle
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtObjectDeclaration

/**
 * Generates a static `getInstance()` method in service and other eligible Java and Kotlin classes.
 *
 * ### The action is available when
 *
 *  * the class is not anonymous, not an enum
 *  * the class doesn't have a static/companion object `getInstance()` method/function defined
 *
 * ### Generation logic
 *
 *  * If [com.intellij.openapi.components.Service.Level.PROJECT] is specified, then project-level getter
 * is generated.
 *  * If [com.intellij.openapi.components.Service.Level.APP] is specified, then application-level getter
 * is generated.
 *  * If no [com.intellij.openapi.components.Service.Level] is specified and the class name ends with
 *    * ProjectService, ProjectSettings, or ProjectState, then project-level getter is generated,
 *    * ApplicationService, ApplicationSettings or ApplicationState, then application-level getter is generated.
 *  * Otherwise, users can choose between generating application- and project-level method.
 *
 * @see org.jetbrains.idea.devkit.inspections.NonDefaultConstructorInspection
 * @see [Retrieving a Service](https://plugins.jetbrains.com/docs/intellij/plugin-services.html.retrieving-a-service)
 * @since 0.1.0
 */
class GenerateStaticGetInstanceAction : BaseCodeInsightAction() {
    private lateinit var handler: CodeInsightActionHandler

    public override fun getHandler(): CodeInsightActionHandler {
        return handler
    }

    public override fun isValidForFile(project: Project, editor: Editor, file: PsiFile): Boolean {
        if (file is PsiJavaFile) {
            handler = JavaGetInstanceHandler()
            return (handler as JavaGetInstanceHandler).isValidFor(editor, file)
        }

        if (file is KtFile) {
            handler = KotlinGetInstanceHandler()
            return (handler as KotlinGetInstanceHandler).isValidFor(editor, file)
        }

        return false
    }

    /**
     * Base class for handling `getInstance()` generation.
     */
    internal abstract class GetInstanceHandler : LanguageCodeInsightActionHandler {
        companion object {
            const val GET_INSTANCE = "getInstance"
        }

        protected fun chooseAppOrProjectLevelFromList(actions: List<GetInstanceGenerationAction<*, *>>, editor: Editor) {
            ApplicationManager.getApplication().invokeLater {
                ListPopupHelper.showActionsInListPopup(
                    JustKittingBundle.message("action.generate.getinstance.level.list.title"), actions, editor)
            }
        }
    }

    /**
     * Handles the validation and generation of a `getInstance()` method in Java files.
     */
    internal class JavaGetInstanceHandler : GetInstanceHandler() {
        private val actions by lazy { listOf(JavaGetInstanceGenerationAction(PROJECT), JavaGetInstanceGenerationAction(APP)) }

        override fun isValidFor(editor: Editor?, file: PsiFile?): Boolean {
            return getStaticOrTopLevelClass(file!!, editor!!)?.let {
                compute<Boolean, Exception> { it.nameIdentifier != null
                    && !it.isEnum
                    //There is no static 'getInstance()' method
                    && !ContainerUtil.exists(it.methods) { method -> GET_INSTANCE == method.name && isStatic(method) }
                }
            } ?: false
        }

        override fun invoke(project: Project, editor: Editor, file: PsiFile) {
            //Null check for 'staticOrTopLevelClass' is not required due to it being checked in 'isValidFor()'
            when (ServiceLevelDecider.getServiceLevel(getStaticOrTopLevelClass(file, editor))) {
                ServiceLevel.PROJECT -> JavaGetInstanceGenerationAction(PROJECT).invokeHandler(project, editor, file)
                ServiceLevel.APP -> JavaGetInstanceGenerationAction(APP).invokeHandler(project, editor, file)
                else -> chooseAppOrProjectLevelFromList(actions, editor)
            }
        }
    }

    /**
     * Handles the validation and generation of a `getInstance()` function in Kotlin files.
     */
    internal class KotlinGetInstanceHandler : GetInstanceHandler() {
        private val actions by lazy { listOf(KotlinGetInstanceGenerationAction(PROJECT), KotlinGetInstanceGenerationAction(APP)) }

        override fun isValidFor(editor: Editor?, file: PsiFile?): Boolean {
            return getParentClass(file!!, editor!!)?.let {
                compute<Boolean, Exception> {
                    it.name != null
                        && !it.isEnum()
                        && !hasGetInstanceFunction(getCompanionObject(it))
                }
            } ?: false
        }

        override fun invoke(project: Project, editor: Editor, file: PsiFile) {
            //Null check for 'getParentClass' is not required due to it being checked in 'isValidFor()'
            when (ServiceLevelDecider.getServiceLevel(getParentClass(file, editor))) {
                ServiceLevel.PROJECT -> KotlinGetInstanceGenerationAction(PROJECT).invokeHandler(project, editor, file)
                ServiceLevel.APP -> KotlinGetInstanceGenerationAction(APP).invokeHandler(project, editor, file)
                else -> chooseAppOrProjectLevelFromList(actions, editor)
            }
        }

        private fun hasGetInstanceFunction(companionObject: KtObjectDeclaration?): Boolean {
            //If there is no companion object or it doesn't have a body, consider the class
            // as having no getInstance() function.
            if (companionObject?.body == null) return false

            val functionsInCompanion = companionObject.body?.functions
            return functionsInCompanion?.any { func -> GET_INSTANCE == func.name } ?: true
        }
    }
}
