//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.inlayhint

import com.picimako.justkitting.ServiceLevelDecider
import com.picimako.justkitting.inlayhint.LightServiceLookup.lookupLightServiceClasses
import com.picimako.justkitting.resources.JustKittingBundle
import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.presentation.InsetPresentation
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.xml.XmlToken
import com.intellij.refactoring.suggested.startOffset
import org.apache.commons.lang3.mutable.MutableInt
import java.util.function.Supplier

/**
 * Adds various types of hints that are leveraged in a composite way in [LightServicesModeBasedHintAdder].
 */
@Suppress("UnstableApiUsage")
abstract class LightServicesHintItemAdder(open var settings: Settings,
                                          open var sink: InlayHintsSink,
                                          open var factory: PresentationFactory,
                                          open var editor: Editor,
                                          open var file: PsiFile) : LightServicesHintPresentationAware(factory, editor, file) {

    /**
     * Adds all the `services` PsiClasses as hints to the `extensionsTag` under the `serviceLevel` group.
     * Before addition, the PsiClasses are sorted alphabetically by their names.
     *
     * The hint addition process is interrupted once the number of class hints added reaches the user-defined max count.
     *
     * It will look something like this:
     * ```
     *      -- Project light services --
     *      SomeProjectService
     *      AnotherGreatService
     *      <extensions defaultExtensionNs="com.intellij">
     * ```
     *
     * @param services the list of PsiClasses to add hints for
     * @param extensionsTag the extensions plugin.xml tag to add the hint to
     * @param serviceLevel the level of service (project, app) of the PsiClasses
     * @param classCount stores the number of class hints added
     */
    fun <T: PsiNameIdentifierOwner> addClassReferenceHints(services: MutableList<T>?, extensionsTag: XmlToken, serviceLevel: String, classCount: MutableInt) {
        if (classCount.value < settings.maxNumberOfServicesToDisplay && services!!.isNotEmpty()) {
            addLabelHints(extensionsTag, JustKittingBundle.message("inlay.hints.light.services.list.display.mode.group.title", serviceLevel))
            for (service in services.sortedBy { it.name }) {
                addClassReferenceHint(extensionsTag, service)
                if (classCount.incrementAndGet() == settings.maxNumberOfServicesToDisplay) return
            }
        }
    }

    /**
     * Adds non-clickable hints for all provided `labels`.
     */
    fun addLabelHints(extensionsTagToken: XmlToken, vararg labels: String) {
        labels.forEach { addHintFor(extensionsTagToken, factory.inset(basePresentation(it), down = 3, top = 3)) }
    }

    /**
     * Adds the *View all light services...* hint for the `extensionsTag`.
     *
     * The argument map is flattened, so that all levels of services are expressed in a list format.
     *
     * @param extensionsTag the extensions plugin.xml tag to add the hint to
     * @param classes the service classes grouped by service levels.
     * The default empty map value is used for the [InlayDisplayMode.ViewAllOnly] display mode, in which case it will look up the light services in the current
     * project, instead of working with a pre-processed collection of them. Since, for [InlayDisplayMode.ListOfLightServices] the View All hint is displayed
     * only when there is definitely a light service in the project, the empty map will not interfere with that logic.
     */
    fun addViewAllServicesHint(extensionsTag: XmlToken, classes: Map<ServiceLevelDecider.ServiceLevel, MutableList<PsiNameIdentifierOwner>> = emptyMap()) {
        val classesSupplier =
            if (classes.isNotEmpty()) Supplier { classes.flatMap { (_, values) -> values } }
            else Supplier { lookupLightServiceClasses(extensionsTag.project).toList() }
        addHintFor(extensionsTag, factory.inset(viewAllServicesPresentation(classesSupplier, extensionsTag.startOffset), down = 1))
    }

    /**
     * Adds a formatted PsiClass reference hint to the given `element`.
     */
    private fun <T: PsiNameIdentifierOwner> addClassReferenceHint(element: XmlToken, psiClass: T) {
        addHintFor(element, factory.inset(classReferencePresentation(psiClass), down = 1))
    }

    private fun addHintFor(element: PsiElement, insetPres: InsetPresentation) {
        sink.addBlockElement(element.parent.startOffset, relatesToPrecedingText = true, showAbove = true, priority = 0,
                presentation = factory.inset(insetPres, left = calculateBlockInlayStartOffset(element as XmlToken)))
    }
}
