//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.picimako.justkitting.intention.state

import com.picimako.justkitting.ThirdPartyLibraryLoader
import com.picimako.justkitting.action.JustKittingActionTestBase

/**
 * Integration test for [JavaConversionActions].
 */
class ConversionActionsKotlinTest : JustKittingActionTestBase() {

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        ThirdPartyLibraryLoader.loadUtil(myFixture)
    }

    fun testConvertsClassWithStandaloneStateObject() {
        checkAction("SomeComponent.kt", { KotlinConversionActions.WithStandaloneStateObject() },
                """
                    class SomeCom<caret>ponent {
                    }
                    """.trimIndent(),
                """
                import com.intellij.openapi.components.State
                import com.intellij.openapi.components.Storage
                import com.intellij.openapi.components.PersistentStateComponent
                
                @State(name = "SomeComponent", storages = [Storage("<storage name>")])
                class SomeComponent : PersistentStateComponent<SomeComponent.State> {
                    private var myState: State = State()
                    override fun getState(): State {
                        return myState
                    }
                
                    override fun loadState(state: State) {
                        myState = state
                    }
                
                    class State {
                    }
                }
                """.trimIndent())
    }

    fun testConvertsClassWithSelfAsState() {
        checkAction("SomeComponent.kt", { KotlinConversionActions.WithSelfAsState() },
                """
                    class SomeC<caret>omponent {
                    }
                    """.trimIndent(),
                """
                import com.intellij.openapi.components.State
                import com.intellij.openapi.components.Storage
                import com.intellij.openapi.components.PersistentStateComponent
                import com.intellij.util.xmlb.XmlSerializerUtil
                
                @State(name = "SomeComponent", storages = [Storage("<storage name>")])
                class SomeComponent : PersistentStateComponent<SomeComponent> {
                    override fun getState(): SomeComponent {
                        return this
                    }
                
                    override fun loadState(state: SomeComponent) {
                        XmlSerializerUtil.copyBean(state, this)
                    }
                }
                """.trimIndent()
        )
    }
}
