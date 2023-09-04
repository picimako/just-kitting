//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.codefolding.plugindescriptor;

import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.lang.properties.IProperty;
import com.intellij.lang.properties.ResourceBundleReference;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceService;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.SmartList;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.devkit.util.DescriptorUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;

import static com.intellij.openapi.util.text.StringUtil.defaultIfEmpty;
import static com.intellij.openapi.util.text.StringUtil.isEmpty;

/**
 * Handles the folding and placeholder text creation for the {@code localInspection} and {@code globalInspection} extensions.
 *
 * @since 0.5.0
 */
@RequiredArgsConstructor
public final class InspectionFolder implements PluginDescriptorTagFolder {
    /**
     * Folding happens only when at least one these attributes is specified. Otherwise, there is nothing to fold.
     */
    private static final Set<String> FOLDABLE_LOCAL_INSPECTION_ATTRIBUTES = Set.of(
        "language", "groupPath", "groupPathKey", "groupName", "groupKey", "displayName", "key"
    );

    /**
     * {@code localInspection} or {@code globalInspection}.
     */
    private final String inspectionEPName;

    @Override
    public void createFolding(XmlTag extensions, @NotNull List<FoldingDescriptor> descriptors) {
        for (var localInspection : extensions.findSubTags(inspectionEPName)) {
            var attributes = localInspection.getAttributes();

            //If no attribute or no foldable attribute, continue processing the rest of the localInspection tags
            if (attributes.length == 0 || !hasAtLeastOneFoldableAttribute(attributes))
                continue;

            descriptors.add(new FoldingDescriptor(
                localInspection.getNode(),
                /*
                 * Folding starts at the first attribute's start offset, and ends at before the tag's closing > symbol.
                 * This handles the both cases when the first attribute is on the same line as the tag name,
                 * and also when it is in the next line.
                 */
                TextRange.create(attributes[0].getNameElement().getTextOffset(), localInspection.getTextRange().getEndOffset() - 1),
                FoldingGroup.newGroup(inspectionEPName)));
        }
    }

    @Override
    public boolean hasAtLeastOneFoldableAttribute(XmlAttribute[] attributes) {
        return Arrays.stream(attributes).anyMatch(attribute -> FOLDABLE_LOCAL_INSPECTION_ATTRIBUTES.contains(attribute.getName()));
    }

    @Override
    public boolean isTagFolderFor(XmlTag tag) {
        return inspectionEPName.equals(tag.getName());
    }

    @Override
    public String getPlaceholderText(XmlTag inspection) {
        var language = getOrEmpty(inspection.getAttributeValue("language"));
        String placeholderLanguage = buildLanguage(language);
        String placeholderPath = buildPath(inspection);

        return placeholderLanguage + (!placeholderLanguage.isEmpty() && !placeholderPath.isEmpty() ? " " + placeholderPath : placeholderPath);
    }

    /**
     * Builds the placeholder text for the {@code language} attribute. For example for the JAVA language,
     * the placeholder text will be {@code for JAVA}
     *
     * @return the placeholder text, or empty string if there is no language attribute, or its value is empty
     */
    private static String buildLanguage(String language) {
        return !language.isEmpty() ? "for " + language : "";
    }

    /**
     * Group and name attributes can be specified as explicit string literals or as message bundle keys.
     * <p>
     * The attributes are in pair as per the following:
     * <ul>
     *     <li>groupPath - groupPathKey</li>
     *     <li>groupName - groupKey</li>
     *     <li>displayName - key</li>
     * </ul>
     * <p>
     * If both attributes of a pair are specified, the non-key ones take precedence.
     * <p>
     * The placeholder text may be in the following formats:
     * <ul>
     *     <li>{@code for [language] at [path]}, when both language and path attributes are specified</li>
     *     <li>{@code for [language]}, when only the language is specified</li>
     *     <li>{@code at [path]}, when only at least one path attribute is specified/li>
     * </ul>
     *
     * @param localInspection the {@code localInspection} XML that is folded
     * @return the placeholder text for the path attributes of the local inspection tag
     */
    private static String buildPath(XmlTag localInspection) {
        var pathElements = new SmartList<String>();

        /*
         * For the attribute [groupPath="Path"], the placeholder text will be 'Path'.
         * For the attribute [groupPath="Some,Path"], the placeholder text will be 'Some / Path'.
         * For the attribute [groupPathKey="some.bundle.key"], the placeholder text will be '{some.bundle.key}'.
         * For the attributes [groupPath="Some,Path"] and [groupPathKey="some.bundle.key"], the placeholder text will be 'Some / Path'.
         */
        pathElements.add(formatAttributeValue(localInspection,
            "groupPath", "groupPathKey",
            Bundle.GROUP_BUNDLE, groupPath -> groupPath.replace(",", " / ")));

        /*
         * For the attribute [groupName="Group name"], the placeholder text will be 'Group name'.
         * For the attribute [groupKey="some.bundle.key"], the placeholder text will be '{some.bundle.key}'.
         * For the attributes [groupName="Group name"] and [groupPathKey="some.bundle.key"], the placeholder text will be 'Group name'.
         */
        pathElements.add(formatAttributeValue(localInspection,
            "groupName", "groupKey",
            Bundle.GROUP_BUNDLE, groupName -> groupName));

        /*
         * For the attribute [displayName="Some inspection title"], the placeholder text will be 'Some inspection title'.
         * For the attribute [groupPathKey="some.bundle.key"], the placeholder text will be '{some.bundle.key}'.
         * For the attributes [displayName="Some inspection title"] and [key="some.bundle.key"], the placeholder text will be 'Some inspection title'.
         */
        pathElements.add(formatAttributeValue(localInspection,
            "displayName", "key",
            Bundle.BUNDLE, displayName -> "'" + displayName + "'"));

        /*
         * Path elements are joined together with a forward-slash with spaces around it.
         * It filters out blank items, so that delimiting will be correct.
         */
        String path = String.join(" / ", pathElements.stream().filter(element -> !element.isBlank()).toList());
        return !path.isBlank() ? "at " + path : "";
    }

    /**
     * Returns the argument value if it is non-null and non-empty, otherwise, returns empty string.
     */
    private static String getOrEmpty(@Nullable String value) {
        return defaultIfEmpty(value, "");
    }

    /**
     * Returns a formatted version of the specified attributes' values.
     *
     * @param localInspection       the {@code localInspection} XML tag
     * @param literalAttrName       the attribute name for literal string version of the attribute
     * @param keyAttrName           the attribute name of the key-based counterpart of {@code literalAttrName}
     * @param primaryBundle         the bundle attribute name from which the fallback of message resolution starts
     * @param literalValueFormatter if the value of the literal string attribute is used, it applies this formatting to it before returning
     */
    private static String formatAttributeValue(XmlTag localInspection, String literalAttrName, String keyAttrName,
                                               Bundle primaryBundle,
                                               UnaryOperator<String> literalValueFormatter) {
        String displayName = localInspection.getAttributeValue(literalAttrName);
        return !isEmpty(displayName)
            ? literalValueFormatter.apply(displayName)
            : resolveMessageFromBundle(localInspection, keyAttrName, primaryBundle);
    }

    private static String resolveMessageFromBundle(XmlTag localInspection, String keyAttrName, @NotNull InspectionFolder.Bundle primaryBundle) {
        var bundle = primaryBundle;
        while (bundle != Bundle.NONE) {
            //GROUP_BUNDLE or BUNDLE
            if (!bundle.attributeName.isEmpty()) {
                var bundleAttr = localInspection.getAttribute(bundle.attributeName);
                if (bundleAttr != null) {
                    var references = getReferences(bundleAttr.getValueElement());
                    if (references.isEmpty()) return asKey(localInspection.getAttributeValue(keyAttrName));

                    //For now, it always takes the first ResourceBundleReference, regardless if there are e.g. localizations for more languages
                    var resolved = findFirstBundleReference(references);
                    if (resolved.isPresent() && resolved.get() instanceof PropertiesFile propertiesFile) {
                        return findMessageInPropertiesOrDefaultToKey(propertiesFile, localInspection.getAttributeValue(keyAttrName));
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
                var resourceBundleTag = DescriptorUtil.getIdeaPlugin((XmlFile) localInspection.getContainingFile()).getResourceBundle().getXmlTag();
                if (resourceBundleTag == null) return asKey(localInspection.getAttributeValue(keyAttrName));

                //For now, it always takes the first ResourceBundleReference, regardless if there are e.g. localizations for more languages
                return findFirstBundleReference(getReferences(resourceBundleTag))
                    .map(resolved -> resolved instanceof PropertiesFile propertiesFile
                        ? findMessageInPropertiesOrDefaultToKey(propertiesFile, localInspection.getAttributeValue(keyAttrName))
                        : null)
                    .orElseGet(() -> asKey(localInspection.getAttributeValue(keyAttrName)));
            }
        }
        return asKey(localInspection.getAttributeValue(keyAttrName));
    }

    @NotNull
    private static Optional<PsiElement> findFirstBundleReference(List<PsiReference> references) {
        return references.stream()
            .filter(ResourceBundleReference.class::isInstance)
            .findFirst()
            .map(PsiReference::resolve);
    }

    @NotNull
    private static List<PsiReference> getReferences(XmlElement element) {
        return PsiReferenceService.getService().getReferences(element, PsiReferenceService.Hints.NO_HINTS);
    }

    private static String findMessageInPropertiesOrDefaultToKey(PropertiesFile propertiesFile, @Nullable String messageKey) {
        return Optional.ofNullable(messageKey)
            .map(propertiesFile::findPropertyByKey)
            .map(IProperty::getValue)
            .orElseGet(() -> asKey(messageKey));
    }

    /**
     * Encloses the argument attribute value in curly braces. For example: {@code some.key} becomes {@code {some.key}}.
     */
    private static String asKey(@Nullable String attributeValue) {
        return getOrEmpty(attributeValue != null && !attributeValue.isBlank() ? "{" + attributeValue + "}" : "");
    }

    /**
     * Resource bundle locations used by inspection EPs.
     */
    @RequiredArgsConstructor
    private enum Bundle {
        /**
         * Used when none of the EP XML tag attribute, nor the {@code <resource-bundle>} tag is specified.
         */
        NONE("", null),
        /**
         * <pre>
         * &lt;idea-plugin>
         *   &lt;resource-bundle>...&lt;/resource-bundle>
         * &lt;/idea-plugin>
         * </pre>
         */
        PLUGIN_DESCRIPTOR("", NONE),
        /**
         * <pre>
         * &lt;localInspection bundle="message.JustKittingBundle" />
         * </pre>
         */
        BUNDLE("bundle", PLUGIN_DESCRIPTOR),
        /**
         * <pre>
         * &lt;localInspection groupBundle="message.JustKittingBundle" />
         * </pre>
         */
        GROUP_BUNDLE("groupBundle", BUNDLE);

        /**
         * The XML tag attribute name associated with this bundle location. Empty string, if the location
         * is not represented by an XML tag attribute.
         */
        @NotNull
        final String attributeName;
        /**
         * The location this bundle definition will fall back to, in case it is not specified.
         */
        @Nullable
        final InspectionFolder.Bundle fallbackTo;
    }
}
