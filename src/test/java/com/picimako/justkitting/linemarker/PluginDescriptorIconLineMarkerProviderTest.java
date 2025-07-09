//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.linemarker;

import static com.intellij.openapi.application.ReadAction.compute;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Integration test for {@link PluginDescriptorIconLineMarkerProvider}.
 */
public final class PluginDescriptorIconLineMarkerProviderTest extends JustKittingLineMarkerCollectionTestBase {

    private final PluginDescriptorIconLineMarkerProvider provider = new PluginDescriptorIconLineMarkerProvider();

    @Override
    protected String getTestDataPath() {
        return "src/test/testData/linemarker/plugindescriptoricon";
    }

    @Override
    protected BiConsumer<PsiElement, List<RelatedItemLineMarkerInfo<?>>> collectNavigationMarkers() {
        return provider::collectNavigationMarkers;
    }

    @Override
    protected PsiElement getElementAtCaret() {
        return PsiTreeUtil.getParentOfType(getFixture().getFile().findElementAt(compute(() -> getFixture().getCaretOffset())), XmlAttribute.class);
    }

    //TODO: fix these tests
//    @Test
    public void testNoGutterIconInNonIconAttribute() {
        checkNoGutterIcon("main/resources/non_icon_attribute.xml");
    }

//    @Test
    public void testNoGutterIconInNotMatchingTagAndAttribute() {
        checkNoGutterIcon("main/resources/non_matching_tag_and_attribute.xml");
    }

//    @Test
    public void testNoGutterIconForNonAllIconsIcon() {
        checkNoGutterIcon("main/resources/non_allicons_icon.xml");
    }

//    @Test
    public void testNoGutterIconForNonExistentAllIconsIcon() {
        checkNoGutterIcon("main/resources/non_existent_allicons_icon.xml");
    }

//    @Test
    public void testGutterIconForAllIconsIcon() {
        checkGutterIcon("main/resources/allicons_action_icon.xml", "Extension / action icon");
    }

//    @Test
    public void testGutterIconForAllIconsIconInGroup() {
        checkGutterIcon("main/resources/allicons_action_icon_in_group.xml", "Extension / action icon");
    }

//    @Test
    public void testGutterIconForIconsIcon() {
        checkGutterIcon("main/resources/icons_icon.xml", "Extension / action icon");
    }

//    @Test
    public void testGutterIconForToolWindowIcon() {
        checkGutterIcon("main/resources/tool_window_icon.xml", "Extension / action icon");
    }
}
