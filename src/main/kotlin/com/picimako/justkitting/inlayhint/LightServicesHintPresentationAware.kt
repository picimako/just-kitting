//Copyright 2026 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

@file:Suppress("UnstableApiUsage")

package com.picimako.justkitting.inlayhint

import com.intellij.codeInsight.hints.InlayPresentationFactory
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.ide.util.DelegatingPsiElementCellRenderer
import com.intellij.ide.util.PsiElementRenderingInfo
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.util.NlsSafe
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.xml.XmlToken
import com.picimako.justkitting.resources.JustKittingBundle.message
import org.jetbrains.kotlin.psi.KtClass
import java.util.function.Function

abstract class LightServicesHintPresentationAware(
    val presentationFactory: PresentationFactory,
    val editor: Editor,
    val file: PsiFile
) {
    fun basePresentation(label: String): InlayPresentation = basePresentation(label, 1)

    /**
     * A simple presentation with a label.
     */
    fun basePresentation(label: String, padding: Int): InlayPresentation {
        return presentationFactory.container(
            presentationFactory.smallText(label),
            InlayPresentationFactory.Padding(padding, padding, padding, padding),
            null,
            null,
            0f
        )
    }

    /**
     * Clickable hint showing the referenced class name. It navigates to the PsiClass when clicked.
     */
    fun <T : PsiNameIdentifierOwner> classReferencePresentation(psiClass: T): InlayPresentation {
        return presentationFactory.referenceOnHover(presentationFactory.smallText(psiClass.name ?: "")) { _, _ ->
            (psiClass as Navigatable).navigate(true)
        }
    }

    /**
     * Hint for showing all light services available in the project.
     * On click, it brings up a popup with the list of light service classes from where users can navigate to the corresponding classes.
     *
     * @param classes     the list of PsiClasses to populate the popup list with
     * @param startOffset the start offset of the `<extensions>` xml tag
     */
    fun <T : PsiNameIdentifierOwner> viewAllServicesPresentation(
        classes: () -> List<T>,
        startOffset: Int
    ): InlayPresentation {
        return presentationFactory.referenceOnHover(
            presentationFactory.smallText(message("inlay.hints.light.services.view.all.light.services"))
        ) { _, _ ->
            val step = object : BaseListPopupStep<T?>(
                message("inlay.hints.light.services.view.all.popup.title"),
                //Sorts the list items alphabetically by class names
                classes().sortedBy { it.name }
            ) {
                override fun onChosen(selectedValue: T?, finalChoice: Boolean): PopupStep<*>? {
                    if (selectedValue is PsiClass || selectedValue is KtClass) {
                        (selectedValue as Navigatable).navigate(true)
                    }

                    return null
                }
            }

            //Moving the caret to the beginning of the <extensions> tag, so that the popup list is displayed right at the element's inlay hint.
            editor.caretModel.moveToOffset(startOffset)
            JBPopupFactory.getInstance()
                //DelegatingPsiElementCellRenderer and ClassRenderingInfo replaces PsiClassListCellRenderer,
                // so that both PsiClasses and KtClasses can be rendered.
                .createListPopup(
                    file.project,
                    step,
                    Function { DelegatingPsiElementCellRenderer(ClassRenderingInfo.INSTANCE) })
                .showInBestPositionFor(editor)
        }
    }

    protected fun calculateBlockInlayStartOffset(element: XmlToken): Int {
        val width = EditorUtil.getPlainSpaceWidth(editor)
        val document = PsiDocumentManager.getInstance(file.project).getDocument(file)
        val line = document!!.getLineNumber(element.parent.textRange.startOffset)
        val startOffset = document.getLineStartOffset(line)
        return (element.parent.textRange.startOffset - startOffset) * width
    }

    /**
     * Rendering info for various classes.
     */
    private class ClassRenderingInfo private constructor() : PsiElementRenderingInfo<PsiNameIdentifierOwner?> {
        @NlsSafe
        override fun getPresentableText(element: PsiNameIdentifierOwner): @NlsSafe String = element.name!!

        companion object {
            val INSTANCE: ClassRenderingInfo = ClassRenderingInfo()
        }
    }
}