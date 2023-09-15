//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.codefolding.plugindescriptor;

import com.intellij.testFramework.TestDataPath;
import com.picimako.justkitting.codefolding.JustKittingCodeFoldingSettings;
import com.picimako.justkitting.codefolding.JustKittingCodeFoldingTestBase;

/**
 * Integration test for {@link PluginDescriptorTagsFoldingBuilder}.
 */
@TestDataPath("$CONTENT_ROOT/testData/codefolding/plugindescriptor/noresourcebundle")
public class PluginDescriptorTagsFoldingBuilderNoResourceBundleTest extends JustKittingCodeFoldingTestBase {

    @Override
    protected String getTestDataPath() {
        return "src/test/testData/codefolding/plugindescriptor/noresourcebundle";
    }

    //No folding

    public void testNoFoldingInspectionPlugin() {
        JustKittingCodeFoldingSettings.getInstance().setCollapsePluginDescriptorTags(false);
        doXmlTestFolding();
    }

    public void testNoFoldingIntentionPlugin() {
        JustKittingCodeFoldingSettings.getInstance().setCollapsePluginDescriptorTags(false);
        doXmlTestFolding();
    }

    //Folding - all

    public void testPlugin() {
        JustKittingCodeFoldingSettings.getInstance().setCollapsePluginDescriptorTags(true);
        doXmlTestFolding();
    }

    //Folding - inspections

    public void testOtherLocalInspectionPlugin() {
        JustKittingCodeFoldingSettings.getInstance().setCollapsePluginDescriptorTags(true);
        doXmlTestFolding();
    }

    public void testOtherGlobalInspectionPlugin() {
        JustKittingCodeFoldingSettings.getInstance().setCollapsePluginDescriptorTags(true);
        doXmlTestFolding();
    }

    //Folding - intention actions

    public void testIntentionPlugin() {
        JustKittingCodeFoldingSettings.getInstance().setCollapsePluginDescriptorTags(true);
        doXmlTestFolding();
    }
}
