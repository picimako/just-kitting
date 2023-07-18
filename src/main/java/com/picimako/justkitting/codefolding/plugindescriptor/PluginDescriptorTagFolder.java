//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.codefolding.plugindescriptor;

import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Base class for handling the folding and placeholder text creation for various tags in plugin descriptor files.
 */
public interface PluginDescriptorTagFolder {

    /**
     * Creates {@link FoldingDescriptor}s and adds them to the list of {@code descriptors}.
     *
     * @param parentTag   an XML tag under which the child XML tags are handled in bulk.
     *                    It can for example be {@code extensions} to process {@code localInspection} tags,
     *                    or {@code actions} to process {@code action} tags.
     * @param descriptors the list of folding descriptors which this folder extends with further descriptors
     */
    void createFolding(XmlTag parentTag, @NotNull List<FoldingDescriptor> descriptors);

    /**
     * Returns if the provided XML attributes contains at least one attribute that makes it possible to fold the tag
     * represented by the current tag folder.
     *
     * @param attributes the list of XML attributes specified on a given XML tag
     */
    boolean hasAtLeastOneFoldableAttribute(XmlAttribute[] attributes);

    /**
     * Returns if the current tag folder is able to fold the provided XML tag.
     *
     * @param tag the XML tag to check for folding
     */
    boolean isTagFolderFor(XmlTag tag);

    /**
     * Creates the placeholder text for the provided XML tag.
     *
     * @param tag the XML tag to create the placeholder text for
     */
    String getPlaceholderText(XmlTag tag);
}
