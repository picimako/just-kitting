//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.codefolding.plugindescriptor;

import com.intellij.testFramework.TestDataPath;
import com.picimako.justkitting.codefolding.ContentRootsJustKittingCodeFoldingTestBase;
import com.picimako.justkitting.codefolding.JustKittingCodeFoldingSettings;
import com.picimako.justkitting.codefolding.JustKittingCodeFoldingTestBase;

/**
 * Integration test for {@link PluginDescriptorTagsFoldingBuilder}.
 */
@TestDataPath("$CONTENT_ROOT/testData/codefolding/plugindescriptor/resourcebundle")
public class PluginDescriptorTagsFoldingBuilderResourceBundleTest extends ContentRootsJustKittingCodeFoldingTestBase {

    @Override
    protected String getTestDataPath() {
        return "src/test/testData/codefolding/plugindescriptor/resourcebundle";
    }

    //Folding - inspections

    public void testPlugin() {
        JustKittingCodeFoldingSettings.getInstance().setCollapsePluginDescriptorTags(true);
        myFixture.copyFileToProject("src/main/resources/messages/LowerLevelBundle.properties");
        doXmlTestFolding("src/main/resources/META-INF/plugin.xml");
    }

    public void testPluginWithTopLevelResourceBundle() {
        JustKittingCodeFoldingSettings.getInstance().setCollapsePluginDescriptorTags(true);
        myFixture.copyFileToProject("src/main/resources/messages/LowerLevelBundle.properties");
        myFixture.copyFileToProject("src/main/resources/messages/TopLevelBundle.properties");
        doXmlTestFolding("src/main/resources/META-INF/topLevelResourceBundlePlugin.xml");
    }
}
