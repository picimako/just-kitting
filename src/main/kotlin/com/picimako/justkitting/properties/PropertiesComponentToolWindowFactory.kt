//Copyright 2026 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.properties

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

/**
 * Tool window for interacting with [com.intellij.ide.util.PropertiesComponent].
 */
class PropertiesComponentToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentManager = toolWindow.contentManager

        val panel = contentManager.factory.createContent(PropertiesComponentPanel.create(project), null, true)
        panel.isCloseable = true

        contentManager.addContent(panel)
    }
}