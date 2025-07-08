//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.codefolding;

import com.intellij.application.options.editor.CodeFoldingOptionsProvider;
import com.intellij.openapi.options.BeanConfigurable;
import com.picimako.justkitting.resources.JustKittingBundle;

/**
 * Provides an options UI for the code folding options of this plugin.
 */
public class JustKittingCodeFoldingOptionsProvider extends BeanConfigurable<JustKittingCodeFoldingSettings> implements CodeFoldingOptionsProvider {

    public JustKittingCodeFoldingOptionsProvider() {
        super(JustKittingCodeFoldingSettings.getInstance(), JustKittingBundle.message("plugin.name"));
        JustKittingCodeFoldingSettings settings = getInstance();

        //See PluginDescriptorTagsFoldingBuilder
        checkBox(JustKittingBundle.message("code.folding.plugin.descriptor.tags"), settings::isCollapsePluginDescriptorTags, settings::setCollapsePluginDescriptorTags);
    }
}
