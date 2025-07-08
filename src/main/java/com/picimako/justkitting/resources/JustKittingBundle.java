//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.resources;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

/**
 * Resource bundle for all messages in this plugin.
 */
public class JustKittingBundle extends DynamicBundle {

    @NonNls
    public static final String JUST_KITTING_BUNDLE = "messages.JustKittingBundle";
    private static final JustKittingBundle INSTANCE = new JustKittingBundle();

    private JustKittingBundle() {
        super(JUST_KITTING_BUNDLE);
    }

    public static @Nls String message(@NotNull @PropertyKey(resourceBundle = JUST_KITTING_BUNDLE) String key, Object @NotNull ... params) {
        return INSTANCE.getMessage(key, params);
    }
}
