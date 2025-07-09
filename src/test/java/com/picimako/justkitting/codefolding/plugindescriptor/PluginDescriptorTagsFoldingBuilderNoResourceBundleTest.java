//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.codefolding.plugindescriptor;

import com.intellij.testFramework.TestDataPath;
import com.picimako.justkitting.codefolding.JustKittingCodeFoldingSettings;
import com.picimako.justkitting.codefolding.JustKittingCodeFoldingTestBase;
import org.junit.jupiter.api.Test;

/**
 * Integration test for {@link PluginDescriptorTagsFoldingBuilder}.
 */
@TestDataPath("$CONTENT_ROOT/testData/codefolding/plugindescriptor/noresourcebundle")
public final class PluginDescriptorTagsFoldingBuilderNoResourceBundleTest extends JustKittingCodeFoldingTestBase {

    @Override
    protected String getTestDataPath() {
        return "src/test/testData/codefolding/plugindescriptor/noresourcebundle";
    }

    //No folding

    @Test
    public void testNoFoldingInspectionPlugin() {
        JustKittingCodeFoldingSettings.getInstance().setCollapsePluginDescriptorTags(false);
        doXmlTestFolding();
    }

    @Test
    public void testNoFoldingIntentionPlugin() {
        JustKittingCodeFoldingSettings.getInstance().setCollapsePluginDescriptorTags(false);
        doXmlTestFolding();
    }

    //Folding - all

//    FIXME: disabled but works in production
//    @Test
//    public void testPlugin() {
//        JustKittingCodeFoldingSettings.getInstance().setCollapsePluginDescriptorTags(true);
//        doXmlTestFolding();
//    }

    //Folding - inspections

//    FIXME: disabled but works in production
//    @Test
//    public void testOtherLocalInspectionPlugin() {
//        JustKittingCodeFoldingSettings.getInstance().setCollapsePluginDescriptorTags(true);
//        doXmlTestFolding();
//    }

//    FIXME: disabled but works in production
//    @Test
//    public void testOtherGlobalInspectionPlugin() {
//        JustKittingCodeFoldingSettings.getInstance().setCollapsePluginDescriptorTags(true);
//        doXmlTestFolding();
//    }

    //Folding - intention actions

    @Test
    public void testIntentionPlugin() {
        JustKittingCodeFoldingSettings.getInstance().setCollapsePluginDescriptorTags(true);
        doXmlTestFolding();
    }
}
