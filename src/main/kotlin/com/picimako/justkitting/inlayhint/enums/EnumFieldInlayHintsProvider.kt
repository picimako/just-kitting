//Copyright 2026 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.inlayhint.enums

import com.intellij.codeInsight.hints.declarative.HintFormat
import com.intellij.codeInsight.hints.declarative.InlayHintsCollector
import com.intellij.codeInsight.hints.declarative.InlayHintsProvider
import com.intellij.codeInsight.hints.declarative.InlayTreeSink
import com.intellij.codeInsight.hints.declarative.InlineInlayPosition
import com.intellij.codeInsight.hints.declarative.OwnBypassCollector
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.text.Strings
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaEnumEntrySymbol
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

/**
 * FQN of an enum class -> a field name inside that enum.
 */
private val supportedEnums: Map<String, String> = mapOf(
    "com.intellij.microservices.http.HttpCode" to "statusCode",
    "com.intellij.util.net.ProxyConfiguration.ProxyProtocol" to "defaultPort"
)

/**
 * Displays inlay hints for usages of enum constants for pre-configured enums and field names.
 *
 * The displayed inlay text is the value of a field of each enum.
 *
 * It
 * - doesn't support enums implemented in Java,
 * - requires the enum classes to be on the classpath to resolve,
 * - and is not configurable yet
 */
internal class EnumFieldInlayHintsProvider : InlayHintsProvider {
    override fun createCollector(file: PsiFile, editor: Editor): InlayHintsCollector? {
        return if (file is KtFile) EnumFieldInlayHintsCollector() else null
    }

    private class EnumFieldInlayHintsCollector : OwnBypassCollector {
        override fun collectHintsForFile(file: PsiFile, sink: InlayTreeSink) {
            val ktFile = file as? KtFile ?: return

            analyze(ktFile) {
                file.accept(object : KtTreeVisitorVoid() {
                    override fun visitSimpleNameExpression(expression: KtSimpleNameExpression) {
                        val enumEntrySymbol = expression.mainReference.resolveToSymbol() as? KaEnumEntrySymbol

                        enumEntrySymbol?.callableId?.classId?.asSingleFqName()?.asString()?.let { enumFqn ->
                            supportedEnums[enumFqn]?.let { fieldName ->
                                val fqn = enumFqn.normalize() ?: return@let
                                val entryName = enumEntrySymbol.name.asString()
                                getFieldValue(fqn, entryName, fieldName)?.let { fieldValue ->
                                    sink.addInlayHint(expression, fieldValue)
                                }
                            }
                        }

                        super.visitSimpleNameExpression(expression)
                    }
                })
            }
        }

        /**
         * Turns inner class FQNs like `com.intellij.util.net.ProxyConfiguration.ProxyProtocol` into
         * `com.intellij.util.net.ProxyConfiguration$ProxyProtocol` by replacing the inner class separator (`.`) with `$`.
         *
         * It leaves top-level class FQNs like `com.intellij.microservices.http.HttpCode` unchanged.
         */
        private fun String.normalize(): String? {
            val indexOfFirstUppercase = indexOfFirstUppercase(this).takeIf { it != -1 } ?: return null

            val numberOfDots = Strings.countChars(substring(indexOfFirstUppercase), '.')
            //Top-level class
            return if (numberOfDots == 0) this
            //Inner class
            else lastIndexOf('.').let { replaceRange(it, it + 1, "$") }
        }

        private fun indexOfFirstUppercase(s: CharSequence): Int {
            for ((i, element) in s.withIndex()) {
                if (Character.isUpperCase(element)) return i
            }
            return -1
        }

        /**
         * Returns the value of the [field] on the enum [enumFqn]'s [entry].
         */
        private fun getFieldValue(enumFqn: String, entry: String, field: String): String? {
            return runCatchingOrNull {
                val clazz = Class.forName(enumFqn).takeIf { it.isEnum } ?: return@runCatchingOrNull null

                //Find the enum constant by name on the enum class
                val enumConstant =
                    clazz.enumConstants?.firstOrNull { (it as Enum<*>).name == entry } ?: return@runCatchingOrNull null

                //Try reading the field directly
                runCatchingOrNull { clazz.getDeclaredField(field).apply { isAccessible = true } }
                    ?.let { field -> return@runCatchingOrNull field.get(enumConstant)?.toString() }

                //Try calling a getter method (e.g. getStatusCode())
                val getterName = "get" + field.replaceFirstChar { it.uppercase() }
                runCatchingOrNull { clazz.getMethod(getterName).apply { isAccessible = true } }
                    ?.invoke(enumConstant)?.toString()
            }
        }

        private fun <T, R> T.runCatchingOrNull(block: T.() -> R): R? = runCatching(block).getOrNull()

        /**
         * Adds an inlay hint with [inlayText] at the end / after the [targetElement].
         */
        private fun InlayTreeSink.addInlayHint(targetElement: PsiElement, inlayText: String) {
            addPresentation(
                position = InlineInlayPosition(targetElement.textRange.endOffset, true),
                hintFormat = HintFormat.default,
            ) {
                text(inlayText)
            }
        }
    }
}