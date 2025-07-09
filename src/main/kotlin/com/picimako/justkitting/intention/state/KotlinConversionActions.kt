//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.intention.state

import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleManager
import com.picimako.justkitting.importIfNotAlreadyAdded
import com.picimako.justkitting.resources.JustKittingBundle
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.startOffset

/**
 * Provides intention actions for the [MakeJavaClassPersistentStateComponentIntention] what users can choose from.
 *
 * Code generation is based on the [
 * Plugin SDK > Persisting State of Components > Implementing the PersistentStateComponent Interface](https://plugins.jetbrains.com/docs/intellij/persisting-state-of-components.html#implementing-the-persistentstatecomponent-interface) document.
 */
class KotlinConversionActions private constructor() {

    /**
     * Converts the class using a standalone inner class (`State`) as the state object.
     * All existing fields, methods, etc. within the class remain untouched.
     *
     * ### From:
     * ```
     * class SomeComponent {
     * }
     * ```
     * <h3>To:</h3>
     * ```
     * import com.intellij.openapi.components.State
     * import com.intellij.openapi.components.Storage
     * import com.intellij.openapi.components.PersistentStateComponent
     *
     * @State(name = "SomeComponent", storages = [Storage("<storage name>")])
     * class SomeComponent : PersistentStateComponent<SomeComponent.State> {
     *
     *     private var myState: State = new State()
     *
     *     override fun getState(): State {
     *         return myState
     *     }
     *
     *     override fun loadState(state: State) {
     *         myState = state
     *     }
     *
     *     class State {
     *     }
     * }
     * ```
     *
     * @since 0.4.0
     */
    class WithStandaloneStateObject : BaseKotlinPersistentStateComponentConversionIntention() {
        override fun update(presentation: Presentation, project: Project, editor: Editor, file: PsiFile) {
            super.update(presentation, project, editor, file)
            presentation.text = JustKittingBundle.message("intention.persistent.state.use.standalone.state.object")
        }

        override fun getHandler(): CodeInsightActionHandler = CodeInsightActionHandler { project: Project?, editor: Editor?, file: PsiFile? ->
            with(createContext(project!!, editor!!, file!!)) {
                runWriteCommandAction(project) {
                    addStateAnnotation(this)
                    addPersistentStateComponentToImplementsList(this, targetClass!!.name + ".State")
                    addStandaloneStateClass(this)
                    with(targetClass.body) {
                        val myStateField = this?.addAfter(factory.createProperty("private var myState: State = State()"), this.lBrace)
                        val getStateFunction = this?.addAfter(factory.createFunction("override fun getState(): State {return myState}"), myStateField)
                        val loadStateFunction = this?.addAfter(factory.createFunction("override fun loadState(state: State) {myState = state}"), getStateFunction)
                        CodeStyleManager.getInstance(project).reformatRange(this!!.psiOrParent, myStateField!!.startOffset, loadStateFunction!!.endOffset, true)
                    }
                }
            }
        }
    }

    /**
     * Converts the class using the class itself as the state object.
     * All existing fields, methods, etc. within the class remain untouched.
     *
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
     * import com.intellij.openapi.components.PersistentStateComponent
     * import com.intellij.util.xmlb.XmlSerializerUtil
     *
     * @State(name = "SomeComponent", storages = [Storage("<storage name>")])
     * class SomeComponent : PersistentStateComponent<SomeComponent> {
     *
     *     override fun getState(): SomeComponent {
     *         return this
     *     }
     *
     *     override fun loadState(state: SomeComponent) {
     *         XmlSerializerUtil.copyBean(state, this)
     *     }
     * }
     * ```
     *
     * @since 0.4.0
     */
    class WithSelfAsState : BaseKotlinPersistentStateComponentConversionIntention() {
        override fun update(presentation: Presentation, project: Project, editor: Editor, file: PsiFile) {
            super.update(presentation, project, editor, file)
            presentation.text = JustKittingBundle.message("intention.persistent.state.use.self.as.state")
        }

        override fun getHandler(): CodeInsightActionHandler = CodeInsightActionHandler { project: Project?, editor: Editor?, file: PsiFile? ->
            with(createContext(project!!, editor!!, file!!)) {
                runWriteCommandAction(project) {
                    addStateAnnotation(this)
                    val className = targetClass!!.name!!
                    addPersistentStateComponentToImplementsList(this, className)

                    //Add getState() and loadState() methods
                    with(targetClass.body) {
                        val getStateFunction = this?.addAfter(factory.createFunction("override fun getState(): $className {return this}"), this.lBrace)
                        val loadStateFunction = this?.addAfter(factory.createFunction("override fun loadState(state: $className) {XmlSerializerUtil.copyBean(state, this)}"), getStateFunction)
                        importIfNotAlreadyAdded(file as KtFile, "com.intellij.util.xmlb.XmlSerializerUtil", factory)

                        CodeStyleManager.getInstance(project).reformatRange(this!!.psiOrParent, getStateFunction!!.startOffset, loadStateFunction!!.endOffset, true)
                    }
                }
            }
        }
    }
}
