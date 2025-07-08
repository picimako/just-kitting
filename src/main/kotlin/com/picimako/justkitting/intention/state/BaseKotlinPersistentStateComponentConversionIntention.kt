//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.intention.state

import com.intellij.codeInsight.actions.BaseCodeInsightAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.picimako.justkitting.PlatformNames
import com.picimako.justkitting.importIfNotAlreadyAdded
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtPsiFactory

/**
 * Base class for the child intention actions for converting Kotlin classes to `PersistentStateComponent`s.
 *
 * These intentions are available whenever [MakeJavaClassPersistentStateComponentIntention] is available,
 * and not context-dependent individually.
 *
 * @see MakeJavaClassPersistentStateComponentIntention
 * @see KotlinConversionActions
 */
abstract class BaseKotlinPersistentStateComponentConversionIntention : BaseCodeInsightAction() {
    data class ConversionContext(@JvmField val factory: KtPsiFactory, @JvmField val targetClass: KtClass?, @JvmField val project: Project?)

    companion object {
        /**
         * Adds the [com.intellij.openapi.components.State] annotation to the target class with some default values.
         *
         * The `name` property is set to the target class' name, while the `storages` property is set to a single
         * [com.intellij.openapi.components.Storage] annotation with a dummy text.
         *
         * ### From:
         * ```
         * class SomeComponent {
         * }
         * ```
         * ### To:
         * ```
         * import com.intellij.openapi.components.State
         * import com.intellij.openapi.components.Storage
         *
         * @State(name = "SomeComponent", storages = [Storage("<storage name>")])
         * class SomeComponent {
         * }
         * ```
         */
        @JvmStatic
        protected fun addStateAnnotation(context: ConversionContext) {
            val annotationEntry = context.factory.createAnnotationEntry("""
                @State(name = "${context.targetClass?.name}", storages = [Storage("<storage name>")])
            """.trimIndent())

            context.targetClass?.addAnnotationEntry(annotationEntry)
            val containingFile = context.targetClass!!.containingKtFile
            importIfNotAlreadyAdded(containingFile, PlatformNames.STATE_ANNOTATION, context.factory)
            importIfNotAlreadyAdded(containingFile, PlatformNames.STORAGE_ANNOTATION, context.factory)
        }

        /**
         * Adds [com.intellij.openapi.components.PersistentStateComponent] to the class' super type list with the
         * state class name provided in the `stateClassName` argument.
         *
         * ### From:
         * ```
         * class SomeComponent {
         * }
         * ```
         * ### To (given stateClassName is 'SomeComponent.State'):
         * ```
         * import com.intellij.openapi.components.PersistentStateComponent
         *
         * class SomeComponent : PersistentStateComponent<SomeComponent.State> {
         * }
         * ```
         *
         * @param stateClassName the name of the class that holds the state of this component
         */
        @JvmStatic
        protected fun addPersistentStateComponentToImplementsList(context: ConversionContext, stateClassName: String) {
            context.targetClass?.addSuperTypeListEntry(context.factory.createSuperTypeEntry("PersistentStateComponent<$stateClassName>"))
            importIfNotAlreadyAdded(context.targetClass!!.containingKtFile, PlatformNames.PERSISTENT_STATE_COMPONENT, context.factory)
        }

        /**
         * Adds an inner class named `State` within the target component class.
         *
         * ### From:
         * ```
         * class SomeComponent {
         * }
         * ```
         * ### To:
         * ```
         * class SomeComponent {
         *     class State {
         *     }
         * }
         * ```
         */
        @JvmStatic
        protected fun addStandaloneStateClass(context: ConversionContext) {
            with(context.targetClass?.body) {
                this?.addAfter(context.factory.createClass("class State {\n}"), this.lBrace)
            }
        }

        @JvmStatic
        protected fun createContext(project: Project, editor: Editor, file: PsiFile): ConversionContext {
            return ConversionContext(
                    KtPsiFactory(project, false),
                    file.findElementAt(editor.caretModel.offset)!!.parent as KtClass,
                    project)
        }
    }
}
