//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.inspection;

import com.intellij.codeInspection.InspectionProfileEntry;

import com.picimako.justkitting.JustKittingTestBase;

/**
 * Base test class for Just Kitting inspection integration testing.
 */
public abstract class JustKittingInspectionTestBase extends JustKittingTestBase {

    @Override
    protected String getTestDataPath() {
        return "src/test/testData/";
    }

    /**
     * Override this to configure the inspection to be tested.
     */
    protected abstract InspectionProfileEntry getInspection();

    /**
     * Tests highlighting for the argument inspection against the java file matching the test method's name
     * without the 'test' prefix.
     */
    protected void doJavaTest(String filename, String text) {
        getFixture().configureByText(filename, text);
        getFixture().enableInspections(getInspection());
        getFixture().testHighlighting(true, false, true);
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
        getFixture().configureByText(filename, beforeText);
        getFixture().enableInspections(getInspection());
        getFixture().doHighlighting();
        getFixture().launchAction(getFixture().findSingleIntention(quickFixName));
        getFixture().checkResult(afterText);
    }
}
