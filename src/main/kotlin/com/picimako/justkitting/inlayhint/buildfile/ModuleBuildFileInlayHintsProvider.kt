//Copyright 2026 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.inlayhint.buildfile

import com.intellij.codeInsight.hints.declarative.HintFormat
import com.intellij.codeInsight.hints.declarative.InlayActionData
import com.intellij.codeInsight.hints.declarative.InlayHintsCollector
import com.intellij.codeInsight.hints.declarative.InlayHintsProvider
import com.intellij.codeInsight.hints.declarative.InlayPayload
import com.intellij.codeInsight.hints.declarative.InlayTreeSink
import com.intellij.codeInsight.hints.declarative.InlineInlayPosition
import com.intellij.codeInsight.hints.declarative.OwnBypassCollector
import com.intellij.codeInsight.hints.declarative.PsiPointerInlayActionNavigationHandler
import com.intellij.codeInsight.hints.declarative.PsiPointerInlayActionPayload
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.BaseProjectDirectories.Companion.getBaseDirectories
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReferenceService
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.xml.impl.GenericDomValueReference

private const val BUILD_GRADLE_KTS = "build.gradle.kts"
private const val BUILD_BAZEL = "BUILD.bazel"

/**
 * Display an inlay hint in `plugin.xml` files after each `idea-plugin.content.module` tag.
 *
 * The inlay hint is clickable and navigates to the `build.gradle.kts` or `BUILD.bazel` file of the
 * linked module descriptor file's parent module.
 *
 * NOTE: this is not yet covered with integration tests
 *
 * @since 1.6.0
 */
@Suppress("HardCodedStringLiteral")
class ModuleBuildFileInlayHintsProvider : InlayHintsProvider {

    override fun createCollector(file: PsiFile, editor: Editor): InlayHintsCollector? {
        if (file !is XmlFile || file.name != "plugin.xml") return null
        return ModuleBuildGradleInlayHintsCollector()
    }

    private class ModuleBuildGradleInlayHintsCollector : OwnBypassCollector {
        override fun collectHintsForFile(file: PsiFile, sink: InlayTreeSink) {
            val xmlFile = file as? XmlFile ?: return

            //Multi-projects workspaces are not supported
            file.project.getBaseDirectories().takeIf { it.size == 1 } ?: return
            //plugin.xml files without a <content> tag are not eligible for this inlay hint
            val contentTag = findContentTag(xmlFile) ?: return

            for (moduleTag in collectModuleTags(contentTag)) {
                val moduleTagEnd = moduleTag.lastChild ?: continue

                //Resolve the reference on the value of the <module> tag's name attribute. It points to the module descriptor file.
                val nameValueElement = moduleTag.getAttribute("name")!!.valueElement ?: continue
                val reference = PsiReferenceService.getService()
                    .getReferences(nameValueElement, PsiReferenceService.Hints.NO_HINTS)
                    .filterIsInstance<GenericDomValueReference<*>>()
                    .firstOrNull() ?: continue
                val resolvedModuleDescriptorFile = reference.resolve() as? XmlFile ?: continue

                //Look up the module descriptor file's parent module directory, and the BUILD.bazel/build.gradle.kts file in it
                val mainOrModuleFolder = resolvedModuleDescriptorFile.parent /*resources*/ ?.parent
                mainOrModuleFolder?.findFile(BUILD_BAZEL)?.let {
                    sink.addInlayHint(file, it, moduleTagEnd, mainOrModuleFolder)
                    continue
                }

                val moduleFolder = mainOrModuleFolder?.parent /*src*/ ?.parent
                moduleFolder?.findFile(BUILD_GRADLE_KTS)?.let {
                    sink.addInlayHint(file, it, moduleTagEnd, moduleFolder)
                }
            }
        }

        private fun InlayTreeSink.addInlayHint(
            file: XmlFile,
            targetFile: PsiFile,
            moduleTagEnd: PsiElement,
            moduleFolder: PsiDirectory
        ) {
            //Assemble the inlay hint data and handler, then register the inlay:
            // - it is displayed after each <module> tag
            // - on Ctlr/Cmd + click, it navigates to the resolved build.gradle.kts
            val buildGradleKtsFilePointer = SmartPointerManager.getInstance(file.project)
                .createSmartPsiElementPointer(targetFile)
            val payload = PsiPointerInlayActionPayload(buildGradleKtsFilePointer)
            val actionData = InlayActionData(payload, PsiPointerInlayActionNavigationHandler.HANDLER_ID)
            addPresentation(
                position = InlineInlayPosition(moduleTagEnd.textRange.endOffset, true),
                payloads = listOf(InlayPayload("navigation", payload)),
                hintFormat = HintFormat.default,
            ) {
                text("${moduleFolder.name}/${targetFile.name}", actionData)
            }
        }

        /**
         * Finds the `<content>` tag inside the `<idea-plugin>` root tag. Returns `null` if it is not present.
         *
         * Namespaces on the `<content>` tag are not handled yet.
         */
        private fun findContentTag(xmlFile: XmlFile): XmlTag? {
            val rootTag = xmlFile.rootTag ?: return null
            return rootTag.subTags.find { it.name == "content" }
        }

        /**
         * Collects all `<module>` tags inside the `<content>` tag.
         */
        private fun collectModuleTags(contentTag: XmlTag): List<XmlTag> {
            return contentTag.subTags
                .filter { it.name == "module" }
                .filter { it.getAttribute("name")?.value != null }
        }
    }
}
