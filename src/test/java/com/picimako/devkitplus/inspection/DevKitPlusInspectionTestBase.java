//Copyright 2021 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.devkitplus.inspection;

import com.intellij.codeInspection.InspectionProfileEntry;

import com.picimako.devkitplus.DevKitPlusTestBase;

/**
 * Based test class for DevKit Plus inspection functional testing.
 */
public abstract class DevKitPlusInspectionTestBase extends DevKitPlusTestBase {

    @Override
    protected String getTestDataPath() {
        return "src/test/testData/";
    }

    /**
     * Override this to configure the inspection to be tested.
     */
    protected abstract InspectionProfileEntry getInspection();

    /**
     * Tests highlighting for the pre-configured inspection against the java file matching the test method's name
     * without the 'test' prefix.
     */
    protected void doJavaTest() {
        doJavaTest(getInspection());
    }

    /**
     * Tests highlighting for the argument inspection against the java file matching the test method's name
     * without the 'test' prefix.
     */
    protected void doJavaTest(InspectionProfileEntry inspection) {
        myFixture.enableInspections(inspection);
        myFixture.configureByFile(getTestName(false) + ".java");
        myFixture.testHighlighting(true, false, true);
    }

    protected void doJavaTest(String filename, String text) {
        myFixture.configureByText(filename, text);
        myFixture.enableInspections(getInspection());
        myFixture.testHighlighting(true, false, true);
    }

    /**
     * Tests highlighting and quick fix for the pre-configured inspection, applying the argument quick fix against the
     * provided beforeText, and the after state against the argument afterText.
     *
     * @param quickFixName the name/text of the quick fix
     * @param filename     the filename in which the before text will be configured
     * @param beforeText   the code before applying the quick fix
     * @param afterText    the code after applying the quick fix
     */
    protected void doQuickFixTest(String quickFixName, String filename, String beforeText, String afterText) {
        myFixture.configureByText(filename, beforeText);
        myFixture.enableInspections(getInspection());
        myFixture.doHighlighting();
        myFixture.launchAction(myFixture.findSingleIntention(quickFixName));
        myFixture.checkResult(afterText);
    }
}
