//Copyright 2021 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.devkitplus.resources;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

/**
 * Resource bundle for all messages in this plugin.
 */
public class DevKitPlusBundle extends DynamicBundle {

    @NonNls
    private static final String DEVKITPLUS_BUNDLE = "messages.DevKitPlusBundle";
    private static final DevKitPlusBundle INSTANCE = new DevKitPlusBundle();

    private DevKitPlusBundle() {
        super(DEVKITPLUS_BUNDLE);
    }

    public static @Nls String message(@NotNull @PropertyKey(resourceBundle = DEVKITPLUS_BUNDLE) String key, Object @NotNull ... params) {
        return INSTANCE.getMessage(key, params);
    }

    /**
     * Retrieves an inspection specific message for the provided id.
     *
     * @param id the suffix of the message key
     * @return the actual message
     */
    public static String inspection(@NonNls String id, Object @NotNull ... params) {
        return message("inspection." + id, params);
    }

    /**
     * Retrieves an inlay hints specific message for the provided id.
     *
     * @param id the suffix of the message key
     * @return the actual message
     */
    public static String inlayHints(@NonNls String id, Object @NotNull ... params) {
        return message("inlay.hints." + id, params);
    }
}
