//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.picimako.justkitting.action.getinstance

import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import com.intellij.psi.util.PsiTreeUtil.getParentOfType
import com.intellij.psi.util.PsiUtil.getTopLevelClass
import java.text.MessageFormat

/**
 * Action for generating `getInstance()` methods for services, components,
 * and classes in Java files, that can benefit from such method.
 */
internal class JavaGetInstanceGenerationAction(serviceLevel: Service.Level) : GetInstanceGenerationAction<PsiMethod, PsiClass>(serviceLevel) {

    public override fun getHandler(): CodeInsightActionHandler {
        return CodeInsightActionHandler { project: Project, editor: Editor, file: PsiFile ->
            PsiDocumentManager.getInstance(project).commitDocument(editor.document)
            val staticOrTopLevelClass = getStaticOrTopLevelClass(file, editor)
            val getInstance = createMethod(staticOrTopLevelClass, project)
            //Add the method right after the list of fields, or the list of constructor if there is any
            WriteCommandAction.runWriteCommandAction(project) {
                JavaCodeStyleManager.getInstance(project).shortenClassReferences(staticOrTopLevelClass!!.add(getInstance))
            }
        }
    }

    override fun createMethod(psiClass: PsiClass?, project: Project?): PsiMethod {
        return PsiElementFactory.getInstance(project).createMethodFromText(MessageFormat.format(
            if (serviceLevel == Service.Level.PROJECT) PROJECT_GET_INSTANCE_PATTERN else APP_GET_INSTANCE_PATTERN,
            psiClass?.name),
            psiClass)
    }

    companion object {
        //The code block brace, {, is enclosed in apostrophes because otherwise MessageFormat would handle it as a placeholder opener
        private const val PROJECT_GET_INSTANCE_PATTERN = "public static {0} getInstance(com.intellij.openapi.project.Project project) '{'return project.getService({0}.class);}"
        private const val APP_GET_INSTANCE_PATTERN = "public static {0} getInstance() '{'return com.intellij.openapi.application.ApplicationManager.getApplication().getService({0}.class);}"

        /**
         * Returns either the top level class in the file, or the immediate static class inside which the caret is place,
         * and where the action would be invoked.
         */
        fun getStaticOrTopLevelClass(file: PsiFile, editor: Editor): PsiClass? {
            val element = file.findElementAt(editor.caretModel.offset)
            return getParentOfType(element, PsiClass::class.java).let {
                //If there is a parent class, and 'it' is either static, or it is the same class as the top level class in the current file
                if (it != null && (isStatic(it) || it.manager.areElementsEquivalent(it, getTopLevelClass(element!!))))
                    return it
                else null
            }
        }

        fun isStatic(psiModifierListOwner: PsiModifierListOwner): Boolean {
            return psiModifierListOwner.hasModifierProperty(PsiModifier.STATIC)
        }
    }
}
