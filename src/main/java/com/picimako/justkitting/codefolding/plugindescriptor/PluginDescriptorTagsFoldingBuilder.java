//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.codefolding.plugindescriptor;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.CustomFoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.XmlRecursiveElementVisitor;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.picimako.justkitting.codefolding.JustKittingCodeFoldingSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.devkit.util.DescriptorUtil;

import java.util.List;
import java.util.Set;

/**
 * Provides code folding for various XML tags within plugin descriptor files.
 * <p>
 * Descriptor files other than {@code plugin.xml} are also supported.
 *
 * @since 0.4.0
 */
public class PluginDescriptorTagsFoldingBuilder extends CustomFoldingBuilder {

    private static final Set<PluginDescriptorTagFolder> TAG_FOLDERS = Set.of(
        new InspectionFolder("localInspection"),
        new InspectionFolder("globalInspection"),
        new IntentionActionFolder());

    @Override
    protected void buildLanguageFoldRegions(@NotNull List<FoldingDescriptor> descriptors, @NotNull PsiElement root, @NotNull Document document, boolean quick) {
        //All plugin descriptor files are supported, not just plugin.xml
        if (root instanceof XmlFile xmlFile
            && isPluginDescriptor(xmlFile)
            && JustKittingCodeFoldingSettings.getInstance().isCollapsePluginDescriptorTags()) {
            root.accept(new XmlRecursiveElementVisitor() {
                @Override
                public void visitXmlTag(XmlTag tag) {
                    if ("extensions".equals(tag.getName()) && "com.intellij".equals(tag.getAttributeValue("defaultExtensionNs"))) {
                        TAG_FOLDERS.forEach(folder -> folder.createFolding(tag, descriptors));
                    } else {
                        //Essential to visit children nodes, and to only go to children nodes
                        // when we haven't found the tag that we want to fold. Otherwise, the folding won't work for the desired tag.
                        super.visitXmlTag(tag);
                    }
                }
            });
        }
    }

    /**
     * This is a workaround because {@link DescriptorUtil#isPluginXml} doesn't seem to work in unit test mode.
     */
    private static boolean isPluginDescriptor(XmlFile xmlFile) {
        return ApplicationManager.getApplication().isUnitTestMode()
            ? xmlFile.getName().toLowerCase().endsWith("plugin.xml")
            : DescriptorUtil.isPluginXml(xmlFile);
    }

    //Placeholder text

    @Override
    protected String getLanguagePlaceholderText(@NotNull ASTNode node, @NotNull TextRange range) {
        return node.getPsi() instanceof XmlTag tag
            ? TAG_FOLDERS.stream()
            .filter(folder -> folder.isTagFolderFor(tag))
            .map(folder -> folder.getPlaceholderText(tag))
            .findFirst()
            .orElse("...")
            : "...";
    }

    @Override
    protected boolean isRegionCollapsedByDefault(@NotNull ASTNode node) {
        return true;
    }
}
