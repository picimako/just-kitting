//Copyright 2026 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.codefolding.plugindescriptor;

import static com.intellij.openapi.util.text.StringUtil.defaultIfEmpty;

import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.lang.properties.ResourceBundleReference;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceService;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.devkit.util.DescriptorUtil;

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

    @NotNull
    protected static String findMessageInPropertiesOrDefaultToKey(PropertiesFile propertiesFile, @Nullable String messageKey, boolean wrapInSingleQuotes) {
        return Optional.ofNullable(messageKey)
            .map(propertiesFile::findPropertyByKey)
            .map(property -> wrapInSingleQuotes ? "'" + property.getValue() + "'" : property.getValue())
            .orElseGet(() -> asKey(messageKey));
    }

    /**
     * Encloses the argument attribute value in curly braces. For example: {@code some.key} becomes {@code {some.key}}.
     */
    @NotNull
    protected static String asKey(@Nullable String attributeValue) {
        return getOrEmpty(attributeValue != null && !attributeValue.isBlank() ? "{" + attributeValue + "}" : "");
    }

    /**
     * Returns the argument value if it is non-null and non-empty, otherwise, returns empty string.
     */
    @NotNull
    protected static String getOrEmpty(@Nullable String value) {
        return defaultIfEmpty(value, "");
    }

    /**
     * Builds the placeholder text for the {@code language} attribute. For example, for the JAVA language,
     * the placeholder text will be {@code for JAVA}.
     *
     * @return the placeholder text, or empty string if there is no language attribute, or its value is empty
     */
    @NotNull
    protected static String buildLanguage(String language) {
        return !language.isEmpty() ? "for " + language : "";
    }

    @NotNull
    protected static String resolveMessageFromBundle(XmlTag extensionTag,
                                                  String keyAttrName,
                                                  @NotNull Bundle primaryBundle,
                                                  boolean wrapInSingleQuotes) {
        var bundle = primaryBundle;
        while (bundle != Bundle.NONE) {
            //GROUP_BUNDLE or BUNDLE
            if (!bundle.attributeName.isEmpty()) {
                var bundleAttr = extensionTag.getAttribute(bundle.attributeName);
                if (bundleAttr != null) {
                    var references = getReferences(bundleAttr.getValueElement());
                    if (references.isEmpty()) return asKey(extensionTag.getAttributeValue(keyAttrName));

                    //For now, it always takes the first ResourceBundleReference, regardless if there are e.g. localizations for more languages
                    var resolved = findFirstBundleReference(references);
                    if (resolved.isPresent() && resolved.get() instanceof PropertiesFile propertiesFile) {
                        return findMessageInPropertiesOrDefaultToKey(propertiesFile, extensionTag.getAttributeValue(keyAttrName), wrapInSingleQuotes);
                    }
                }
                bundle = bundle.fallbackTo;
            }
            //PLUGIN_DESCRIPTOR: <resource-bundle>
            else {
                /*
                 * <idea-plugin>
                 *   <resource-bundle>...</resource-bundle>
                 * </idea-plugin>
                 */
                var resourceBundleTag = DescriptorUtil.getIdeaPlugin((XmlFile) extensionTag.getContainingFile()).getResourceBundle().getXmlTag();
                if (resourceBundleTag == null) return asKey(extensionTag.getAttributeValue(keyAttrName));

                //For now, it always takes the first ResourceBundleReference, regardless if there are e.g. localizations for more languages
                return findFirstBundleReference(getReferences(resourceBundleTag))
                    .map(resolved -> resolved instanceof PropertiesFile propertiesFile
                        ? findMessageInPropertiesOrDefaultToKey(propertiesFile, extensionTag.getAttributeValue(keyAttrName), wrapInSingleQuotes)
                        : null)
                    .orElseGet(() -> asKey(extensionTag.getAttributeValue(keyAttrName)));
            }
        }
        return asKey(extensionTag.getAttributeValue(keyAttrName));
    }
}
