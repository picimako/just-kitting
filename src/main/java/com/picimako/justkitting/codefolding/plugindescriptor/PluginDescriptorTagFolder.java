//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.codefolding.plugindescriptor;

import static com.intellij.openapi.util.text.StringUtil.defaultIfEmpty;

import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.lang.properties.ResourceBundleReference;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceService;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Base class for handling the folding and placeholder text creation for various tags in plugin descriptor files.
 */
public abstract class PluginDescriptorTagFolder {

    /**
     * Creates {@link FoldingDescriptor}s and adds them to the list of {@code descriptors}.
     *
     * @param parentTag   an XML tag under which the child XML tags are handled in bulk.
     *                    It can for example be {@code extensions} to process {@code localInspection} tags,
     *                    or {@code actions} to process {@code action} tags.
     * @param descriptors the list of folding descriptors which this folder extends with further descriptors
     */
    abstract void createFolding(XmlTag parentTag, @NotNull List<FoldingDescriptor> descriptors);

    /**
     * Returns if the provided XML tag is eligible/possible to fold by the current tag folder.
     *
     * @param tag the XML tag to fold
     */
    abstract boolean isEligibleForFolding(XmlTag tag);

    /**
     * Returns if the current tag folder is able to fold the provided XML tag.
     *
     * @param tag the XML tag to check for folding
     */
    abstract boolean isTagFolderFor(XmlTag tag);

    /**
     * Creates the placeholder text for the provided XML tag.
     *
     * @param tag the XML tag to create the placeholder text for
     */
    abstract String getPlaceholderText(XmlTag tag);

    @NotNull
    protected static Optional<PsiElement> findFirstBundleReference(List<PsiReference> references) {
        return references.stream()
            .filter(ResourceBundleReference.class::isInstance)
            .findFirst()
            .map(PsiReference::resolve);
    }

    @NotNull
    protected static List<PsiReference> getReferences(XmlElement element) {
        return PsiReferenceService.getService().getReferences(element, PsiReferenceService.Hints.NO_HINTS);
    }

    protected static String findMessageInPropertiesOrDefaultToKey(PropertiesFile propertiesFile, @Nullable String messageKey, boolean wrapInSingleQuotes) {
        return Optional.ofNullable(messageKey)
            .map(propertiesFile::findPropertyByKey)
            .map(property -> wrapInSingleQuotes ? "'" + property.getValue() + "'" : property.getValue())
            .orElseGet(() -> asKey(messageKey));
    }

    /**
     * Encloses the argument attribute value in curly braces. For example: {@code some.key} becomes {@code {some.key}}.
     */
    protected static String asKey(@Nullable String attributeValue) {
        return getOrEmpty(attributeValue != null && !attributeValue.isBlank() ? "{" + attributeValue + "}" : "");
    }

    /**
     * Returns the argument value if it is non-null and non-empty, otherwise, returns empty string.
     */
    protected static String getOrEmpty(@Nullable String value) {
        return defaultIfEmpty(value, "");
    }
}
