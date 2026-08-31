//Copyright 2026 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.properties

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.picimako.justkitting.ServiceLevelDecider.ServiceLevel

@Service(Service.Level.PROJECT)
@State(name = "JKPropertiesComponentSettings", storages = [Storage(value = "jk-properties-component.xml")])
class PropertiesComponentSettings(val project: Project) : PersistentStateComponent<JkPropertiesComponentState> {

    @Volatile
    private var state: JkPropertiesComponentState = JkPropertiesComponentState()

    var level: ServiceLevel
        get() = state.level
        set(value) {
            state.level = value
        }

    var operation: Operation
        get() = state.operation
        set(value) {
            state.operation = value
        }

    var valueType: ValueType
        get() = state.valueType
        set(value) {
            state.valueType = value
        }

    var key: String
        get() = state.key
        set(value) {
            state.key = value
        }

    var value: String
        get() = state.value
        set(value) {
            state.value = value
        }

    override fun getState(): JkPropertiesComponentState = state

    override fun loadState(state: JkPropertiesComponentState) {
        this.state = state
    }

    override fun noStateLoaded() {
        loadState(JkPropertiesComponentState())
    }
}

class JkPropertiesComponentState {
    var operation: Operation = Operation.QUERY
    var level: ServiceLevel = ServiceLevel.APP
    var valueType: ValueType = ValueType.STRING
    var key: String = ""
    var value: String = ""
}