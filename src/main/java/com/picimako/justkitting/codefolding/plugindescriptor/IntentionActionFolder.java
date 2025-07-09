//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.codefolding.plugindescriptor;

import static com.intellij.psi.util.PsiTreeUtil.findSiblingForward;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.IntentionManager;
import com.intellij.codeInsight.intention.impl.config.IntentionActionWrapper;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.util.QualifiedName;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.devkit.util.DescriptorUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Handles the folding and placeholder text creation for the {@code intentionAction} extensions.
 *
 * @since 0.6.0
 */
public class IntentionActionFolder extends PluginDescriptorTagFolder {

    private static final String INTENTION_EP_NAME = "intentionAction";

    @Override
    public void createFolding(XmlTag extensions, @NotNull List<FoldingDescriptor> descriptors) {
        for (var intentionAction : extensions.findSubTags(INTENTION_EP_NAME)) {
            var subTags = intentionAction.getSubTags();

            //If no subtag, continue processing the rest of the intentionAction tags
            if (subTags.length == 0) continue;

            //Because the opening and closing tag don't have specific XmlElement type,
            // and are instead identified by their token types.
            var openingTag = findSiblingForward(intentionAction.getFirstChild(), XmlTokenType.XML_NAME, __ -> {
            });
            var closingTag = findSiblingForward(openingTag, XmlTokenType.XML_NAME, __ -> {
            });

            var group = FoldingGroup.newGroup(INTENTION_EP_NAME);
            //Folds the inner part of the <intentionAction>...</intentionAction> tag at:
            // <intentionAction[>...]</intentionAction>
            descriptors.add(new FoldingDescriptor(
                intentionAction.getNode(),
                TextRange.create(openingTag.getTextRange().getEndOffset() + 1, closingTag.getTextRange().getStartOffset() - 2),
                group));

            //Folds the closing part of the <intentionAction>...</intentionAction> tag at:
            // <intentionAction>...[</intentionAction]>
            var closingTagNode = closingTag.getNode();
            closingTagNode.putUserData(UserDataKeys.CLOSING_TAG_NODE, INTENTION_EP_NAME);
            descriptors.add(new FoldingDescriptor(
                closingTagNode,
                TextRange.create(closingTag.getTextRange().getStartOffset() - 2, closingTag.getTextRange().getEndOffset()),
                group));
        }
    }

    @Override
    public boolean isEligibleForFolding(XmlTag intentionAction) {
        //Since at least the category/categoryKey and the className are mandatory, fold in every case
        return true;
    }

    @Override
    public boolean isTagFolderFor(XmlTag tag) {
        return INTENTION_EP_NAME.equals(tag.getName());
    }

    @Override
    public String getPlaceholderText(XmlTag tag) {
        if (INTENTION_EP_NAME.equals(tag.getNode().getUserData(UserDataKeys.CLOSING_TAG_NODE)))
            return "...";

        //Creates the string e.g.: ' for JAVA'
        var language = findFirstSubTagWithNameAndNonBlankValue(tag, "language").map(lang -> " for " + lang).orElse("");

        var path = new SmartList<String>();
        //Takes either the value of the 'category' subtag, if it is present,
        // or tries to resolve the resource bundle key specified in the 'categoryKey' subtag
        String category = findFirstSubTagWithName(tag, "category").orElseGet(() -> resolveCategoryKey(tag));
        for (String part : category.split("/")) {
            path.add(part.trim());
        }

        //Resolves the family name of the intention specified in the 'className' subtag,
        // or returns the simple name of that class
        path.add(resolveIntentionFamilyNameOrClassName(tag).trim());

        //Returns a placeholder text like:
        // for JAVA at Group / 'Intention for something'
        // for JAVA at SomethingIntentionAction
        // at Group / 'Intention for something'
        // at Group / Subgroup / 'Intention for something'
        // at SomethingIntentionAction
        // ...
        return language + " at " + String.join(" / ", path) + " ";
    }

    @NotNull
    private static Optional<String> findFirstSubTagWithName(XmlTag tag, String category) {
        return Optional.ofNullable(tag.findFirstSubTag(category)).map(cat -> cat.getValue().getText());
    }

    @NotNull
    private static Optional<String> findFirstSubTagWithNameAndNonBlankValue(XmlTag tag, String language) {
        return findFirstSubTagWithName(tag, language).filter(lang -> !lang.isBlank());
    }

    private static String resolveCategoryKey(XmlTag intentionAction) {
        return findFirstSubTagWithName(intentionAction, "categoryKey")
            .map(categoryBundleKey -> {
                var bundleName = Optional.ofNullable(intentionAction.findFirstSubTag("bundleName"));
                /*
                 * <intentionAction>
                 *   <bundleName></bundleName>
                 * </intentionAction>
                 */
                if (bundleName.isPresent()) {
                    var references = getReferences(bundleName.get());
                    if (references.isEmpty()) return asKey(categoryBundleKey);

                    //For now, it always takes the first ResourceBundleReference, regardless if there are e.g. localizations for more languages
                    var resolved = findFirstBundleReference(references);
                    if (resolved.isPresent() && resolved.get() instanceof PropertiesFile propertiesFile) {
                        return findMessageInPropertiesOrDefaultToKey(propertiesFile, categoryBundleKey, false);
                    }
                } else {
                    /*
                     * <idea-plugin>
                     *   <resource-bundle>...</resource-bundle>
                     * </idea-plugin>
                     */
                    var resourceBundleTag = DescriptorUtil.getIdeaPlugin((XmlFile) intentionAction.getContainingFile()).getResourceBundle().getXmlTag();
                    if (resourceBundleTag == null) return asKey(categoryBundleKey);

                    return findFirstBundleReference(getReferences(resourceBundleTag))
                        .map(resolved -> resolved instanceof PropertiesFile propertiesFile
                            ? findMessageInPropertiesOrDefaultToKey(propertiesFile, categoryBundleKey, false)
                            : null)
                        .orElseGet(() -> asKey(categoryBundleKey));
                }
                return asKey(categoryBundleKey);
            }).orElse("");
    }

    /**
     * This solution is for the case when the plugin, whose project is currently open, is also installed
     * in the IDE, thus it has the intention action classes registered, so that they are accessible
     * via IntentionManager.
     *
     * @param tag the XML tag for which the placeholder text is being assembled
     * @return the family name evaluated from the intention action class instance,
     * or if that was not possible, the short name of the intention action class
     */
    @NotNull
    private static String resolveIntentionFamilyNameOrClassName(XmlTag tag) {
        return findFirstSubTagWithNameAndNonBlankValue(tag, "className")
            .map(classNameValue ->
                Arrays.stream(IntentionManager.getInstance().getIntentionActions())
                    //Takes only the IntentionActionWrapper instances and finds the one intention with
                    // the fully qualified name specified in the 'className' subtag
                    .filter(IntentionActionWrapper.class::isInstance)
                    .map(IntentionActionWrapper.class::cast)
                    .filter(wrapper -> classNameValue.equals(wrapper.getImplementationClassName()))
                    .map(IntentionActionWrapper::getDelegate)
                    //The family name might not be resolved e.g. when it requests a parametrized string from a bundle
                    .map(IntentionAction::getFamilyName)
                    .findFirst()
                    //Put single quotes around only when the family name could be resolved
                    .map(familyName -> "'" + familyName + "'")
                    //If the family name could not be resolved, return the simple name of the intention class
                    .orElseGet(() -> QualifiedName.fromDottedString(classNameValue).getLastComponent()))
            .orElse("");
    }
}
