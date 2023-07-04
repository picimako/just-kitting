//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.codefolding;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.picimako.justkitting.codefolding.plugindescriptor.PluginDescriptorTagsFoldingBuilder;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

/**
 * Stores and fetches the settings for code folding.
 */
@State(name = "JustKittingCodeFoldingSettings", storages = @Storage("editor.xml"))
public class JustKittingCodeFoldingSettingsImpl extends JustKittingCodeFoldingSettings implements PersistentStateComponent<JustKittingCodeFoldingSettingsImpl> {

    /**
     * Flag for folding tags in plugin descriptor files.
     *
     * @see PluginDescriptorTagsFoldingBuilder
     * @since 0.4.0
     */
    @Getter
    @Setter
    private boolean collapsePluginDescriptorTags = true;

    //---- Service state handling ----

    @Override
    public JustKittingCodeFoldingSettingsImpl getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull final JustKittingCodeFoldingSettingsImpl state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
