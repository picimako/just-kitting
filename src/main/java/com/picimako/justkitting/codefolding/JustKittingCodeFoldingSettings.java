//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.codefolding;

import com.intellij.openapi.application.ApplicationManager;

/**
 * Base class for storing the code folding settings for this project.
 */
public abstract class JustKittingCodeFoldingSettings {

    public abstract boolean isCollapsePluginDescriptorTags();
    public abstract void setCollapsePluginDescriptorTags(boolean value);

    public static JustKittingCodeFoldingSettings getInstance() {
        return ApplicationManager.getApplication().getService(JustKittingCodeFoldingSettings.class);
    }
}
