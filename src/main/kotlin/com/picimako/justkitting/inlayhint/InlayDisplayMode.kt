//Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.inlayhint

import com.picimako.justkitting.resources.JustKittingBundle

/**
 * Represents how to display the Light Service inlay hints in the plugin.xml.
 */
enum class InlayDisplayMode(val displayName: String) {
    /**
     * No hint is displayed.
     */
    Disabled(JustKittingBundle.message("inlay.hints.light.services.settings.display.mode.disabled")),

    /**
     * Shows a user-defined max number of light services grouped by the service level.
     *
     * This option can display up to [Settings.MAX_NO_OF_SERVICES] services with an optional 'View All' hint that is displayed
     * when there is more light services in the project than [Settings.MAX_NO_OF_SERVICES].
     */
    ListOfLightServices(JustKittingBundle.message("inlay.hints.light.services.settings.display.mode.list.of.services")),

    /**
     * Displays only a View All... hint.
     */
    ViewAllOnly(JustKittingBundle.message("inlay.hints.light.services.settings.display.mode.view.all.only"))
}

data class Settings(
    var lightServicesDisplayMode: InlayDisplayMode = InlayDisplayMode.Disabled,
    /**
     * Applicable only in the case of [InlayDisplayMode.ListOfLightServices].
     */
    var maxNumberOfServicesToDisplay: Int = DEFAULT_MAX_NO_OF_SERVICES
) {
    companion object {
        const val DEFAULT_MAX_NO_OF_SERVICES: Int = 10
        const val MAX_NO_OF_SERVICES: Int = 50
    }
}
