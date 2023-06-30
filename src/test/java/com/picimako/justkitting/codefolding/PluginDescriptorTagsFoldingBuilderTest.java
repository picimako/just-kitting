//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.codefolding;

import com.intellij.testFramework.TestDataPath;

/**
 * Integration test for {@link PluginDescriptorTagsFoldingBuilder}.
 */
@TestDataPath("$CONTENT_ROOT/testData/codefolding/plugindescriptor")
public class PluginDescriptorTagsFoldingBuilderTest extends JustKittingCodeFoldingTestBase {

    @Override
    protected String getTestDataPath() {
        return "src/test/testData/codefolding/plugindescriptor/";
    }

    //No folding

    public void testNoFoldingPlugin() {
        JustKittingCodeFoldingSettings.getInstance().setCollapsePluginDescriptorTags(false);
        doXmlTestFolding();
    }

    //Folding

    public void testPlugin() {
        JustKittingCodeFoldingSettings.getInstance().setCollapsePluginDescriptorTags(true);
        doXmlTestFolding();
    }

    public void testOtherPlugin() {
        JustKittingCodeFoldingSettings.getInstance().setCollapsePluginDescriptorTags(true);
        doXmlTestFolding();
    }
}
