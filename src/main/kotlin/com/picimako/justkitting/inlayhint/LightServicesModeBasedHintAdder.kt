//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.inlayhint

import com.picimako.justkitting.ServiceLevelDecider
import com.picimako.justkitting.resources.JustKittingBundle
import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.xml.XmlToken
import org.apache.commons.lang3.mutable.MutableInt

/**
 * Provides methods to add hints in a composite way based on editor types and display modes. 
 */
@Suppress("UnstableApiUsage")
data class LightServicesModeBasedHintAdder(override var settings: Settings,
                                           override var sink: InlayHintsSink,
                                           override var factory: PresentationFactory,
                                           override var editor: Editor,
                                           override var file: PsiFile) : LightServicesHintItemAdder(settings, sink, factory, editor, file) {

    /**
     * Adds hints for the code snippet displayed in `Settings > Editor > Inlay Hints`.
     *
     * These are dummy hint labels to make sure that descriptive enough hints are shown even when there are no light services in a project.
     */
    fun addPreviewHints(element: XmlToken) {
        when (settings.lightServicesDisplayMode) {
            InlayDisplayMode.ListOfLightServices -> addLabelHints(element, *JustKittingBundle.inlayHints("light.services.settings.list.display.mode.preview.text").split(",").toTypedArray())
            InlayDisplayMode.ViewAllOnly -> addLabelHints(element, JustKittingBundle.inlayHints("light.services.view.all.light.services"))
            else -> {
            }
        }
    }

    /**
     * Adds hints for the `<extensions>` tag when it is in the project's actual plugin.xml.
     */
    fun addRealHints(element: XmlToken) {
        when (settings.lightServicesDisplayMode) {
            InlayDisplayMode.ListOfLightServices -> addHintsForLimitedList(element)
            InlayDisplayMode.ViewAllOnly -> addHintsForViewAllOnly(element)
            else -> {
            }
        }
    }

    /**
     * Adds all the necessary hints for the [InlayDisplayMode.ListOfLightServices] display mode.
     *
     * The resulting hints will look something like this:
     * ```
     *      -- Project light services --
     *      SomeProjectService
     *      AnotherGreatService
     *      -- Application light services --
     *      AwesomeApplicationService
     *      <extensions defaultExtensionNs="com.intellij">
     * ```
     *
     * If the overall number of light services is greater than the user-defined max number to display,
     * then an extra `View all light services...` hint is also added:
     * ```
     *      -- Project light services --
     *      SomeProjectService
     *      AnotherGreatService
     *      -- Application light services --
     *      AwesomeApplicationService
     *      View all light services...
     *      <extensions defaultExtensionNs="com.intellij">
     * ```
     *
     * @see ServiceLevelDecider.ServiceLevel
     */
    private fun addHintsForLimitedList(element: XmlToken) {
        val lightServices = LightServiceLookup.lookupLightServiceClasses(file.project)
        if (lightServices.isNotEmpty()) {
            //Collect and group the classes into collections based on their service level
            val services = linkedMapOf<ServiceLevelDecider.ServiceLevel, MutableList<PsiNameIdentifierOwner>>()
            ServiceLevelDecider.ServiceLevel.values().forEach { services[it] = mutableListOf() }
            lightServices.forEach { services[ServiceLevelDecider.getServiceLevel(it)]?.add(it) }

            //Add hints for all light service classes. The order of service level groups is now determined by the order in which
            //the ServiceLevelHelper.ServiceLevel entries are defined.
            val classCount = MutableInt(0)
            ServiceLevelDecider.ServiceLevel.values().forEach { addClassReferenceHints(services[it], element, it.displayName, classCount) }

            //If there are more light services classes than the user-defined max count to display, then add a 'View All' hint as well
            if (lightServices.size > settings.maxNumberOfServicesToDisplay && classCount.value == settings.maxNumberOfServicesToDisplay) {
                addViewAllServicesHint(element, services)
            }
        }
    }

    /**
     * Adds a single, `View all light services...` hint for the [InlayDisplayMode.ViewAllOnly] display mode.
     */
    private fun addHintsForViewAllOnly(element: XmlToken) {
        if (LightServiceLookup.isProjectHasLightService(file.project)) {
            addViewAllServicesHint(element)
        }
    }
}
