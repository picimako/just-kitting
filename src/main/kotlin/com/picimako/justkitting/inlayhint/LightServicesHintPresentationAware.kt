//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.inlayhint

import com.intellij.codeInsight.hints.InlayPresentationFactory
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.ide.util.PsiClassListCellRenderer
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlToken
import com.intellij.refactoring.suggested.startOffset
import com.picimako.justkitting.resources.JustKittingBundle
import java.util.function.Supplier

/**
 * Provides `InlayPresentation`s for displaying inlay hints.
 */
@Suppress("UnstableApiUsage")
abstract class LightServicesHintPresentationAware(open var factory: PresentationFactory, open var editor: Editor, open var file: PsiFile) {

    /**
     * A simple presentation with a label.
     */
    fun basePresentation(label: String, padding: Int = 1): InlayPresentation {
        return factory.container(factory.smallText(label), padding = InlayPresentationFactory.Padding(padding, padding, padding, padding))
    }

    /**
     * Clickable hint showing the referenced class name. It navigates to the PsiClass when clicked.
     */
    fun classReferencePresentation(psiClass: PsiClass): InlayPresentation {
        return factory.referenceOnHover(factory.smallText(psiClass.name ?: "")) { _, _ -> psiClass.navigate(true) }
    }

    /**
     * Hint for showing all light services available in the project.
     * On click, it brings up a popup with the list of light service classes from where users can navigate to the corresponding classes.
     * 
     * @param classes the list of PsiClasses to populate the popup list with
     * @param startOffset the start offset of the `<extensions>` xml tag
     */
    fun viewAllServicesPresentation(classes: Supplier<List<PsiClass>>, startOffset: Int): InlayPresentation {
        return factory.referenceOnHover(factory.smallText(JustKittingBundle.inlayHints("light.services.view.all.light.services"))) { _, _ ->
            val step: BaseListPopupStep<PsiClass> = object : BaseListPopupStep<PsiClass>(JustKittingBundle.inlayHints("light.services.view.all.popup.title"), classes.get()) {
                override fun onChosen(selectedValue: PsiClass, finalChoice: Boolean): PopupStep<*>? {
                    selectedValue.navigate(true)
                    return null
                }
            }
            //Moving the caret to the beginning of the <extensions> tag, so that the popup list is displayed right at the element's inlay hint.
            editor.caretModel.moveToOffset(startOffset)
            JBPopupFactory.getInstance().createListPopup(file.project, step) { PsiClassListCellRenderer() }.showInBestPositionFor(editor)
        }
    }

    protected fun calculateBlockInlayStartOffset(element: XmlToken): Int {
        val width = EditorUtil.getPlainSpaceWidth(editor)
        val document = PsiDocumentManager.getInstance(file.project).getDocument(file)
        val line = document!!.getLineNumber(element.parent.startOffset)
        val startOffset = document.getLineStartOffset(line)
        return (element.parent.startOffset - startOffset) * width
    }
}