//Copyright 2021 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.action.getinstance

import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.SlowOperations
import com.picimako.justkitting.PlatformNames
import org.jetbrains.kotlin.idea.core.getOrCreateCompanionObject
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.ImportPath
import java.text.MessageFormat

/**
 * Action for generating `getInstance()` functions for services, components,
 * and classes in Kotlin files, that can benefit from such method.
 */
internal class KotlinGetInstanceGenerationAction(serviceLevel: Service.Level) : GetInstanceGenerationAction<KtFunction, KtClass>(serviceLevel) {

    public override fun getHandler(): CodeInsightActionHandler {
        return CodeInsightActionHandler { project: Project?, editor: Editor, file: PsiFile ->
            PsiDocumentManager.getInstance(project!!).commitDocument(editor.document)
            val parentClass = getParentClass(file, editor) as KtClass

            runWriteCommandAction(project) {
                /*
                 * If there is a companion object, returns is, otherwise creates a new one
                 * and adds it to the parent class.
                 *
                 * class SomeClass {
                 *   companion object {
                 *   }
                 * }
                 */
                val companionObject = getCompanionObject(parentClass) ?: parentClass.getOrCreateCompanionObject()

                //Create the 'getInstance()' function
                val getInstanceFunction = createMethod(parentClass, project)

                /*
                 * Add 'getInstance()' to the companion object, right after the opening brace
                 *
                 * class SomeService {
                 *   companion object {
                 *     fun getInstance(project: Project): SomeService = project.service()
                 *     //or
                 *     //fun getInstance(): SomeService = service()
                 *   }
                 * }
                 *
                 */
                companionObject.body?.addAfter(getInstanceFunction, companionObject.body?.lBrace)

                //Import service and Project if they are not yet imported for other functionality
                //Project must be imported only when we are generating the getInstance() for a project service
                importIfNotAlready(file as KtFile, "com.intellij.openapi.components.service")
                if (serviceLevel == Service.Level.PROJECT)
                    importIfNotAlready(file, PlatformNames.PROJECT)
            }
        }
    }

    /**
     * Creates the `getInstance()` function with its return type being the current class.
     *
     * The service level, for which the function is generated, is determined by [serviceLevel].
     */
    override fun createMethod(psiClass: KtClass?, project: Project?): KtFunction {
        return KtPsiFactory(project, false).createFunction(MessageFormat.format(
            if (serviceLevel == Service.Level.PROJECT) PROJECT_GET_INSTANCE_PATTERN else APP_GET_INSTANCE_PATTERN,
            psiClass?.name))
    }

    /**
     * Imports the provided fully qualified name, if it is not already imported in the given Kotlin file.
     *
     * NOTE: it doesn't take into account existing star imports.
     *
     * @param file the Kotlin file in which the import would happen
     * @param fqNameToImport the FQN of the class, function, etc. to import
     */
    private fun importIfNotAlready(file: KtFile, fqNameToImport: String) {
        val fqName = FqName(fqNameToImport)
        val isAlreadyImported = file.importDirectives
            .mapNotNull { directive -> directive.importPath }
            .any { importPath -> importPath.fqName == fqName }

        if (!isAlreadyImported) {
            file.importList?.add(KtPsiFactory(file.project, false).createImportDirective(ImportPath.fromString(fqNameToImport)))
        }
    }

    companion object {
        private const val PROJECT_GET_INSTANCE_PATTERN = "fun getInstance(project: Project): {0} = project.service()"
        private const val APP_GET_INSTANCE_PATTERN = "fun getInstance(): {0} = service()"

        /**
         * Returns the parent Kotlin class of the element at where the caret is currently placed.
         */
        fun getParentClass(file: PsiFile, editor: Editor): KtClass? {
            return file.findElementAt(editor.caretModel.offset)?.let {
                SlowOperations.allowSlowOperations<KtClass, Exception> {
                    PsiTreeUtil.getParentOfType(it, KtClass::class.java)
                }
            }
        }

        /**
         * Returns the companion object in the provided Kotlin class, if there is any.
         */
        fun getCompanionObject(ktClass: KtClass): KtObjectDeclaration? {
            return ktClass.companionObjects.firstOrNull()
        }
    }
}
