//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.intention.state

import com.picimako.justkitting.JustKittingTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Integration test for [MakeKotlinClassPersistentStateComponentIntention].
 */
class MakeKotlinPersistentStateComponentIntentionTest : JustKittingTestBase() {
    //Not available

    
    @Test
    fun testIntentionIsNotAvailableWhenAlreadyPersistentStateComponent() {
        val psiFile = fixture.configureByText("SomeComponent.kt",
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
        assertThat(
            MakeKotlinClassPersistentStateComponentIntention()
                .isAvailable(project, fixture.editor, psiFile)).isFalse()
    }

    @Test
    fun testIntentionIsNotAvailableForInterface() {
        val psiFile = fixture.configureByText("SomeComponent.kt",
                """
                     interface SomeCo<caret>mponent {
                     }
                     """.trimIndent())
        assertThat(
            MakeKotlinClassPersistentStateComponentIntention()
                .isAvailable(project, fixture.editor, psiFile)).isFalse()
    }

    @Test
    fun testIntentionIsNotAvailableForEnum() {
        val psiFile = fixture.configureByText("SomeComponent.kt",
                """
                     enum SomeCo<caret>mponent {
                     }
                     """.trimIndent())
        assertThat(
            MakeKotlinClassPersistentStateComponentIntention()
                .isAvailable(project, fixture.editor, psiFile)).isFalse()
    }

    @Test
    fun testIntentionIsNotAvailableForAbstractClass() {
        val psiFile = fixture.configureByText("SomeComponent.kt",
                """
                     abstract class SomeCo<caret>mponent {
                     }
                     """.trimIndent())
        assertThat(
            MakeKotlinClassPersistentStateComponentIntention()
                .isAvailable(project, fixture.editor, psiFile)).isFalse()
    }

    @Test
    fun testIntentionIsNotAvailableForInlineClass() {
        val psiFile = fixture.configureByText("SomeComponent.kt",
                """
                     value class SomeCo<caret>mponent(val s: String) {
                     }
                     """.trimIndent())
        assertThat(
            MakeKotlinClassPersistentStateComponentIntention()
                .isAvailable(project, fixture.editor, psiFile)).isFalse()
    }

    @Test
    fun testIntentionIsNotAvailableWhenInvokedOnANonPsiClassElement() {
        val psiFile = fixture.configureByText("SomeComponent.kt",
                """
                class SomeComponent {
                    var fie<caret>ld: String
                }
                """.trimIndent())
        assertThat(
            MakeKotlinClassPersistentStateComponentIntention()
                .isAvailable(project, fixture.editor, psiFile)).isFalse()
    }

    //Available

    @Test
    fun testIntentionIsAvailableForClass() {
        val psiFile = fixture.configureByText("SomeComponent.kt",
                """
                class SomeComp<caret>onent {
                    var field: String
                }
                """.trimIndent())
        assertThat(
            MakeKotlinClassPersistentStateComponentIntention()
                .isAvailable(project, fixture.editor, psiFile)).isTrue()
    }
}
