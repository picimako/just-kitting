//Copyright 2026 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.codefolding.plugindescriptor;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Resource bundle locations used by inspection EPs.
 */
@RequiredArgsConstructor
public enum Bundle {
    /**
     * Used when none of the EP XML tag attributes, nor the {@code <resource-bundle>} tag is specified.
     */
    NONE("", null),
    /**
     * <pre>{@code
     * <idea-plugin>
     *   <resource-bundle>...</resource-bundle>
     * </idea-plugin>
     * }</pre>
     */
    PLUGIN_DESCRIPTOR("", NONE),
    /**
     * <pre>{@code
     * <localInspection bundle="message.JustKittingBundle" />
     * }</pre>
     */
    BUNDLE("bundle", PLUGIN_DESCRIPTOR),
    /**
     * <pre>{@code
     * <localInspection groupBundle="message.JustKittingBundle" />
     * }</pre>
     */
    GROUP_BUNDLE("groupBundle", BUNDLE);

    /**
     * The XML tag attribute name associated with this bundle location. Empty string, if the location
     * is not represented by an XML tag attribute.
     */
    @NotNull
    public final String attributeName;
    /**
     * The location this bundle definition will fall back to, in case it is not specified.
     */
    @Nullable
    public final Bundle fallbackTo;
}
