//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.picimako.justkitting.intention.state

import com.picimako.justkitting.JustKittingTestBase
import org.assertj.core.api.Assertions.assertThat

/**
 * Integration test for [MakeClassPersistentStateComponentIntention].
 */
class MakeClassPersistentStateComponentIntentionKotlinTest : JustKittingTestBase() {
    //Not available
    fun testIntentionIsNotAvailableWhenAlreadyPersistentStateComponent() {
        val psiFile = myFixture.configureByText("SomeComponent.kt",
                """
                import com.intellij.openapi.components.PersistentStateComponent

                class SomeCo<caret>mponent : PersistentStateComponent<SomeComponent> {
                    override fun getState(): SomeComponent {
                        return this
                    }

                    override fun loadState(state: SomeComponent) {
                    }
                }
                """.trimIndent())
        assertThat(MakeClassPersistentStateComponentIntention().isAvailable(project, myFixture.editor, psiFile)).isFalse()
    }

    fun testIntentionIsNotAvailableForInterface() {
        val psiFile = myFixture.configureByText("SomeComponent.kt",
                """
                     interface SomeCo<caret>mponent {
                     }
                     """.trimIndent())
        assertThat(MakeClassPersistentStateComponentIntention().isAvailable(project, myFixture.editor, psiFile)).isFalse()
    }

    fun testIntentionIsNotAvailableForEnum() {
        val psiFile = myFixture.configureByText("SomeComponent.kt",
                """
                     enum SomeCo<caret>mponent {
                     }
                     """.trimIndent())
        assertThat(MakeClassPersistentStateComponentIntention().isAvailable(project, myFixture.editor, psiFile)).isFalse()
    }

    fun testIntentionIsNotAvailableForAbstractClass() {
        val psiFile = myFixture.configureByText("SomeComponent.kt",
                """
                     abstract class SomeCo<caret>mponent {
                     }
                     """.trimIndent())
        assertThat(MakeClassPersistentStateComponentIntention().isAvailable(project, myFixture.editor, psiFile)).isFalse()
    }

    fun testIntentionIsNotAvailableForInlineClass() {
        val psiFile = myFixture.configureByText("SomeComponent.kt",
                """
                     value class SomeCo<caret>mponent(val s: String) {
                     }
                     """.trimIndent())
        assertThat(MakeClassPersistentStateComponentIntention().isAvailable(project, myFixture.editor, psiFile)).isFalse()
    }

    fun testIntentionIsNotAvailableWhenInvokedOnANonPsiClassElement() {
        val psiFile = myFixture.configureByText("SomeComponent.kt",
                """
                class SomeComponent {
                    var fie<caret>ld: String
                }
                """.trimIndent())
        assertThat(MakeClassPersistentStateComponentIntention().isAvailable(project, myFixture.editor, psiFile)).isFalse()
    }

    //Available
    fun testIntentionIsAvailableForClass() {
        val psiFile = myFixture.configureByText("SomeComponent.kt",
                """
                class SomeComp<caret>onent {
                    var field: String
                }
                """.trimIndent())
        assertThat(MakeClassPersistentStateComponentIntention().isAvailable(project, myFixture.editor, psiFile)).isTrue()
    }
}
