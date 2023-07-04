//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.codefolding.plugindescriptor;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.CustomFoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.XmlRecursiveElementVisitor;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.SmartList;
import com.picimako.justkitting.codefolding.JustKittingCodeFoldingSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.devkit.util.DescriptorUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;

import static com.intellij.openapi.util.text.StringUtil.defaultIfEmpty;
import static com.intellij.openapi.util.text.StringUtil.isEmpty;

/**
 * Provides code folding for various XML tags within plugin descriptor files.
 * <p>
 * Descriptor files other than {@code plugin.xml} are also supported.
 *
 * @since 0.4.0
 */
public class PluginDescriptorTagsFoldingBuilder extends CustomFoldingBuilder {

    /**
     * Folding happens only when at least one these attributes is specified. Otherwise, there is nothing to fold.
     */
    private static final Set<String> FOLDABLE_LOCAL_INSPECTION_ATTRIBUTES = Set.of(
        "language", "groupPath", "groupPathKey", "groupName", "groupKey", "displayName", "key"
    );

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
                        for (var localInspection : tag.findSubTags("localInspection")) {
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
                                FoldingGroup.newGroup("localInspection")));
                        }
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

    private static boolean hasAtLeastOneFoldableAttribute(XmlAttribute[] attributes) {
        return Arrays.stream(attributes).anyMatch(attribute -> FOLDABLE_LOCAL_INSPECTION_ATTRIBUTES.contains(attribute.getName()));
    }

    //Placeholder text

    @Override
    protected String getLanguagePlaceholderText(@NotNull ASTNode node, @NotNull TextRange range) {
        var psi = node.getPsi();
        if (psi instanceof XmlTag localInspection) {
            var language = getOrEmpty(localInspection.getAttributeValue("language"));
            String placeholderLanguage = buildLanguage(language);
            String placeholderPath = buildPath(localInspection);

            return placeholderLanguage + (!placeholderLanguage.isEmpty() && !placeholderPath.isEmpty() ? " " + placeholderPath : placeholderPath);
        }

        return "...";
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
        pathElements.add(formatAttributeValue(localInspection, "groupPath", "groupPathKey", groupPath -> groupPath.replace(",", " / ")));

        /*
         * For the attribute [groupName="Group name"], the placeholder text will be 'Group name'.
         * For the attribute [groupKey="some.bundle.key"], the placeholder text will be '{some.bundle.key}'.
         * For the attributes [groupName="Group name"] and [groupPathKey="some.bundle.key"], the placeholder text will be 'Group name'.
         */
        pathElements.add(formatAttributeValue(localInspection, "groupName", "groupKey", groupName -> groupName));

        /*
         * For the attribute [displayName="Some inspection title"], the placeholder text will be 'Some inspection title'.
         * For the attribute [groupPathKey="some.bundle.key"], the placeholder text will be '{some.bundle.key}'.
         * For the attributes [displayName="Some inspection title"] and [key="some.bundle.key"], the placeholder text will be 'Some inspection title'.
         */
        pathElements.add(formatAttributeValue(localInspection, "displayName", "key", displayName -> "'" + displayName + "'"));

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
     * @param literalValueFormatter if the value of the literal string attribute is used, it applies this formatting to it before returning
     */
    private static String formatAttributeValue(XmlTag localInspection, String literalAttrName, String keyAttrName, UnaryOperator<String> literalValueFormatter) {
        String displayName = localInspection.getAttributeValue(literalAttrName);
        return !isEmpty(displayName)
            ? literalValueFormatter.apply(displayName)
            : getOrEmpty(asKey(localInspection.getAttributeValue(keyAttrName)));
    }

    /**
     * Encloses the argument attribute value in curly braces. For example: {@code some.key} becomes {@code {some.key}}.
     */
    private static String asKey(@Nullable String attributeValue) {
        return attributeValue != null && !attributeValue.isBlank() ? "{" + attributeValue + "}" : "";
    }

    @Override
    protected boolean isRegionCollapsedByDefault(@NotNull ASTNode node) {
        return true;
    }
}
