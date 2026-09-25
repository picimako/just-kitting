//Copyright 2026 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.codefolding.plugindescriptor;

import com.picimako.justkitting.codefolding.JustKittingCodeFoldingSettings;
import com.picimako.justkitting.codefolding.JustKittingCodeFoldingTestBase;
import org.junit.jupiter.api.Test;

/**
 * Integration test for {@link PluginDescriptorTagsFoldingBuilder}.
 */
public final class PluginDescriptorTagsFoldingBuilderNoResourceBundleTest extends JustKittingCodeFoldingTestBase {

    @Override
    protected String getTestDataPath() {
        return "src/test/testData/codefolding/plugindescriptor/noresourcebundle";
    }

    //No folding

    @Test
    public void testNoFoldingInspectionPlugin() {
        performTest(false);
    }

    @Test
    public void testNoFoldingIntentionPlugin() {
        performTest(false);
    }

    //All

    @Test
    public void testPlugin() {
        performTest(true);
    }

    //Inspections

    @Test
    public void testOtherLocalInspectionPlugin() {
        performTest(true);
    }

    @Test
    public void testOtherGlobalInspectionPlugin() {
        performTest(true);
    }

    //Intention actions

    @Test
    public void testIntentionPlugin() {
        performTest(true);
    }

    //Declarative inlay hints

    @Test
    public void testDeclarativeInlayHintPlugin() {
        performTest(true);
    }

    private void performTest(boolean collapsePluginDescriptorTags) {
        JustKittingCodeFoldingSettings.getInstance().setCollapsePluginDescriptorTags(collapsePluginDescriptorTags);
        doXmlTestFolding();
    }
}
