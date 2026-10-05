//Copyright 2026 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.properties

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.picimako.justkitting.resources.JustKittingBundle.message

/**
 * Does the actual invocations on the application- or project-level `PropertiesComponent` returned by
 * [getPropertiesComponent].
 */
internal abstract class PropertiesComponentCaller(protected val project: Project) {

    internal fun call(): String {
        val settings = project.service<PropertiesComponentSettings>()
        val propertiesComponent = getPropertiesComponent()

        try {
            when (settings.operation) {
                Operation.QUERY -> {
                    return when (settings.valueType) {
                        ValueType.STRING -> propertiesComponent.getValue(settings.key) ?: message("pc.execution.result.null")
                        ValueType.STRING_LIST -> {
                            val result = propertiesComponent.getList(settings.key) ?: return message("pc.execution.result.null")
                            if (result.isEmpty()) return message("pc.execution.result.empty.list")
                            result.joinToString(separator = "\n", prefix = "[", postfix = "]")
                        }
                    }
                }
                Operation.SET -> {
                    when (settings.valueType) {
                        ValueType.STRING -> propertiesComponent.setValue(settings.key, settings.value)
                        //If the input value is an empty string, 'settings.value.lines()' would return a one-item list
                        // with a single empty string element
                        ValueType.STRING_LIST -> {
                            val listValue = if (settings.value.isEmpty()) emptyList() else settings.value.lines()
                            propertiesComponent.setList(settings.key, listValue)
                        }
                    }
                }

                Operation.DELETE -> {
                    when (settings.valueType) {
                        ValueType.STRING -> propertiesComponent.setValue(settings.key, null)
                        ValueType.STRING_LIST -> propertiesComponent.setList(settings.key, null)
                    }
                }
            }
        } catch (e: Exception) {
            logger<PropertiesComponentCaller>().warn(message("pc.execution.failed.log"), e)
            return message("pc.execution.failed.result")
        }

        return ""
    }

    abstract fun getPropertiesComponent(): PropertiesComponent
}

internal class ProjectPropertiesComponent(project: Project) : PropertiesComponentCaller(project) {
    override fun getPropertiesComponent(): PropertiesComponent = PropertiesComponent.getInstance(project)
}

internal class ApplicationPropertiesComponent(project: Project) : PropertiesComponentCaller(project) {
    override fun getPropertiesComponent(): PropertiesComponent = PropertiesComponent.getInstance()
}