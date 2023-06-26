//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.picimako.justkitting.action.getinstance

import com.intellij.codeInsight.actions.BaseCodeInsightAction
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.picimako.justkitting.resources.JustKittingBundle

/**
 * Base class for generating a static `getInstance()` method for service retrieval.
 *
 * @param <PSI_METHOD>
 * @param <PSI_CLASS>
 */
internal abstract class GetInstanceGenerationAction<PSI_METHOD, PSI_CLASS>
protected constructor(protected val serviceLevel: Service.Level) : BaseCodeInsightAction() {
    /**
     * Creates a [PSI_METHOD] for the `getInstance()` from the current action's pattern text.
     *
     * This method will be the one added to psiClass`.
     *
     * @param psiClass the class in which the method is generated and added into
     */
    protected abstract fun createMethod(psiClass: PSI_CLASS?, project: Project?): PSI_METHOD

    override fun update(presentation: Presentation, project: Project, editor: Editor, file: PsiFile) {
        super.update(presentation, project, editor, file)
        presentation.text = getText()
    }

    /**
     * Returns the text of the action. It appears in the action list in which users can choose which level of service they
     * are generating the method for.
     */
    private fun getText(): String = if (serviceLevel == Service.Level.PROJECT) JustKittingBundle.message("action.generate.getinstance.project.level") else JustKittingBundle.message("action.generate.getinstance.application.level")
}
