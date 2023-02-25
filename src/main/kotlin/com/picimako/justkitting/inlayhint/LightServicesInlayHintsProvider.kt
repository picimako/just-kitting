//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.inlayhint

import com.picimako.justkitting.inlayhint.Settings.Companion.MAX_NO_OF_SERVICES
import com.picimako.justkitting.resources.JustKittingBundle
import com.intellij.codeInsight.hints.*
import com.intellij.lang.Language
import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorKind
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.ui.ComponentValidator
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.XmlPatterns.xmlTag
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlToken
import com.intellij.psi.xml.XmlTokenType
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.PropertyBinding
import com.intellij.ui.layout.panel
import com.intellij.ui.layout.withTextBinding
import com.intellij.util.ui.JBUI
import org.jetbrains.idea.devkit.util.DescriptorUtil
import java.util.function.Supplier
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent
import javax.swing.event.DocumentEvent

/**
 * Provides inlay hints for the `<extensions>` starting tag in a plugin's main plugin.xml file.
 *
 * The `<extensions>` tag is selected because it usually contains the majority of plugin functionality declarations.
 *
 * Both the display mode and the number of services to display are configurable within `Settings > Editor > Inlay Hints`
 *
 * @since 0.1.0
 */
@Suppress("UnstableApiUsage", "HardCodedStringLiteral")
class LightServicesInlayHintsProvider : InlayHintsProvider<Settings> {
    override val key: SettingsKey<Settings>
        get() = SettingsKey("light.services")

    override val name: String
        get() = JustKittingBundle.inlayHints("light.services.settings.type.title")

    override val previewText: String
        get() = """
                <idea-plugin>
                    <extensions defaultExtensionNs="com.intellij">
                    </extensions>
                </idea-plugin>
            """.trimIndent()

    override fun createConfigurable(settings: Settings): ImmediateConfigurable {
        return object : ImmediateConfigurable {
            val lightServicesDisplayModeModel = DefaultComboBoxModel(InlayDisplayMode.values())
            val propBinding = PropertyBinding(
                { settings.maxNumberOfServicesToDisplay.toString() },
                { value -> settings.maxNumberOfServicesToDisplay = value.toIntOrNull() ?: Settings.DEFAULT_MAX_NO_OF_SERVICES })
            val maxNoOfServicesTextField = JBTextField(propBinding.get(), 2)

            override val mainCheckboxText: String
                get() = JustKittingBundle.inlayHints("light.services.settings.show.hints.option")

            override fun createComponent(listener: ChangeListener): JComponent {
                val panel = panel {
                    row {
                        label(JustKittingBundle.inlayHints("light.services.display.mode.label"))
                        //Add combobox to select display mode
                        val lightServicesDisplayMode = comboBox<InlayDisplayMode>(
                            lightServicesDisplayModeModel,
                            { settings.lightServicesDisplayMode },
                            { settings.lightServicesDisplayMode = it ?: InlayDisplayMode.Disabled },
                            SimpleListCellRenderer.create("") { it.displayName }
                        ).component
                        //Update Settings properties and related UI controls
                        lightServicesDisplayMode.addActionListener {
                            settings.lightServicesDisplayMode = lightServicesDisplayMode.selectedItem as InlayDisplayMode
                            maxNoOfServicesTextField.isEnabled = settings.lightServicesDisplayMode == InlayDisplayMode.ListOfLightServices
                            listener.settingsChanged()
                        }
                    }
                    row {
                        label(JustKittingBundle.inlayHints("light.services.settings.max.no.of.services.label"))
                        component(maxNoOfServicesTextField).withTextBinding(propBinding)
                        //https://jetbrains.design/intellij/principles/validation_errors/
                        //ComponentValidator is used since CellBuilder.intTextField with its validation mechanism won't work for some reason
                        ComponentValidator(ApplicationManager.getApplication()).withValidator(Supplier {
                            maxNoOfServicesTextField.let {
                                val maxServices: String = it.text
                                if (maxServices.isNotBlank()) {
                                    try {
                                        if (maxServices.toInt() !in 1..MAX_NO_OF_SERVICES) {
                                            ValidationInfo(
                                                JustKittingBundle.inlayHints(
                                                    "light.services.settings.value.must.be.between.x.and.y",
                                                    1,
                                                    MAX_NO_OF_SERVICES
                                                ), maxNoOfServicesTextField
                                            )
                                        } else {
                                            settings.maxNumberOfServicesToDisplay = maxServices.toInt()
                                            null
                                        }
                                    } catch (nfe: NumberFormatException) {
                                        ValidationInfo(
                                            JustKittingBundle.inlayHints("light.services.settings.value.must.be.a.number"),
                                            maxNoOfServicesTextField
                                        )
                                    }
                                } else {
                                    null
                                }
                            }
                        }).installOn(maxNoOfServicesTextField)

                        maxNoOfServicesTextField.document.addDocumentListener(object : DocumentAdapter() {
                            override fun textChanged(e: DocumentEvent) {
                                ComponentValidator.getInstance(maxNoOfServicesTextField).ifPresent { v: ComponentValidator -> v.revalidate() }
                                listener.settingsChanged()
                            }
                        })
                    }
                }
                panel.border = JBUI.Borders.empty(2)
                return panel
            }

            override fun reset() {
                lightServicesDisplayModeModel.selectedItem = settings.lightServicesDisplayMode
            }
        }
    }

    override fun getCollectorFor(file: PsiFile, editor: Editor, settings: Settings, sink: InlayHintsSink): InlayHintsCollector {
        return object : FactoryInlayHintsCollector(editor) {
            val hintAdder = LightServicesModeBasedHintAdder(settings, sink, factory, editor, file)

            override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
                if (settings.lightServicesDisplayMode != InlayDisplayMode.Disabled) {
                    //For the preview in Inlay Hints settings, there is no need to query the project for actual light services, hence the distinction
                    val isSettingsPreview = isInSettingsPreview()
                    //limit the hint to the plugin's main config file. Exclude optional dependencies' configurations
                    if (!file.project.service<DumbService>().isDumb && isExtensionsPluginXmlTagToken(isSettingsPreview, element)) {
                        if (isSettingsPreview) hintAdder.addPreviewHints(element as XmlToken)
                        else hintAdder.addRealHints(element as XmlToken)
                        return false //don't traverse child elements, found the <extensions> tag
                    }
                }
                return true
            }

            private fun isExtensionsPluginXmlTagToken(isSettingsPreview: Boolean, element: PsiElement) =
                if (isSettingsPreview) isElementToShowHintFor(element) else file.name == "plugin.xml" && isElementToShowHintFor(element)

            private fun isInSettingsPreview() = editor.editorKind == EditorKind.UNTYPED && file.name == "Dummy.xml"

            /**
             * Validates whether `file` and `element` is the `<extensions>` XML tag with `defaultExtensionNs="com.intellij"`
             * attribute within a plugin.xml file.
             *
             * Since there is a separate inspection reporting that the `<extensions>` tag doesn't have `defaultExtensionNs="com.intellij"` specified,
             * that construct is ignored in this hints provider.
             *
             * @see org.jetbrains.idea.devkit.inspections.PluginXmlDomInspection
             */
            private fun isElementToShowHintFor(element: PsiElement): Boolean {
                return DescriptorUtil.isPluginXml(file) &&
                        psiElement(XmlToken::class.java)
                            .withElementType(XmlTokenType.XML_START_TAG_START)
                            .withParent(
                                xmlTag()
                                    .withName("extensions")
                                    .withAttributeValue("defaultExtensionNs", "com.intellij")
                            )
                            .accepts(element)
            }
        }
    }

    override fun createSettings(): Settings = Settings()

    override fun isLanguageSupported(language: Language): Boolean = language is XMLLanguage
}
