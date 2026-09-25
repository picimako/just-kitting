//Copyright 2026 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.codefolding.plugindescriptor;

import static com.intellij.util.containers.ContainerUtil.exists;

import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/**
 * Handles the folding and placeholder text creation for the {@code codeInsight.declarativeInlayProvider} extension.
 * <p>
 * An extension like
 * <pre>{@code
 * <codeInsight.declarativeInlayProvider
 *   language="XML"
 *   group="OTHER_GROUP"
 *   isEnabledByDefault="true"
 *   implementationClass="com.picimako.justkitting.inlayhint.buildfile.ModuleBuildFileInlayHintsProvider"
 *   nameKey="inlay.hints.module.build.file"
 *   providerId="just.kitting.module.build.file"/>
 * }</pre>
 * will be folded as
 * <pre>{@code
 * <codeInsight.declarativeInlayProvider> for XML at OTHER_GROUP / <the message resolved from the key 'inlay.hints.module.build.file'>
 * }</pre>
 * <p>
 * NOTE: it folds {@code <codeInsight.declarativeInlayProvider>} tags with child {@code <option>} tags too,
 * but those child tags are not incorporated into the placeholder text.
 *
 * @since 1.6.0
 */
public final class CodeInsightDeclarativeInlayProviderFolder extends PluginDescriptorTagFolder {
    /**
     * Folding happens only when at least one of these attributes is specified. Otherwise, there is nothing to fold.
     */
    private static final Set<String> FOLDABLE_ATTRIBUTES = Set.of("language", "group", "nameKey");
    private static final String epTagName = "codeInsight.declarativeInlayProvider";

    @Override
    void createFolding(XmlTag extensions, @NotNull List<FoldingDescriptor> descriptors) {
        for (var inlayProvider : extensions.findSubTags(epTagName)) {
            var attributes = inlayProvider.getAttributes();

            //If no attribute or no foldable attribute, continue processing the rest of the declarativeInlayProvider tags
            if (!isEligibleForFolding(inlayProvider)) continue;

            descriptors.add(new FoldingDescriptor(
                inlayProvider.getNode(),
                /*
                 * Folding starts at the first attribute's start offset, and ends at before the tag's closing > symbol.
                 * This handles both cases when the first attribute is on the same line as the tag name,
                 * and also when it is in the next line.
                 */
                TextRange.create(attributes[0].getNameElement().getTextOffset(), inlayProvider.getTextRange().getEndOffset() - 1),
                FoldingGroup.newGroup(epTagName)));
        }
    }

    @Override
    boolean isEligibleForFolding(XmlTag inlayProvider) {
        return exists(inlayProvider.getAttributes(), attribute -> FOLDABLE_ATTRIBUTES.contains(attribute.getName()));
    }

    @Override
    boolean isTagFolderFor(XmlTag tag) {
        return epTagName.equals(tag.getName());
    }

    @Override
    String getPlaceholderText(XmlTag inlayProvider) {
        var language = getOrEmpty(inlayProvider.getAttributeValue("language"));
        String placeholderLanguage = buildLanguage(language);
        String placeholderPath = buildPath(inlayProvider);

        return placeholderLanguage + (!placeholderLanguage.isEmpty() && !placeholderPath.isEmpty() ? " " + placeholderPath : placeholderPath);
    }

    /**
     * Returns {@code at [group] / [nameKey]}.
     * <p>
     * The name key may be resolved from the {@code bundle} attribute or from a {@code <resource-bundle>} tag.
     */
    private static String buildPath(XmlTag inlayProvider) {
        String nameKey = inlayProvider.getAttributeValue("nameKey");
        String group = getGroup(inlayProvider);
        String name = nameKey != null ? resolveMessageFromBundle(inlayProvider, "nameKey", Bundle.BUNDLE, true) : "[missing nameKey]";

        return "at " + group + " / " + (!name.isEmpty() ? name : "[unresolved nameKey]");
    }

    private static @NotNull String getGroup(XmlTag inlayProvider) {
        String group = inlayProvider.getAttributeValue("group");
        return group != null ? group : "[missing group]";
    }
}
