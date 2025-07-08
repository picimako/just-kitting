// Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.codefolding;

import com.picimako.justkitting.JustKittingTestBase;

/**
 * Base class for testing code folding.
 */
public abstract class JustKittingCodeFoldingTestBase extends JustKittingTestBase {

    /**
     * Tests code folding in the XML file matching the test method's name without the 'test' prefix.
     * <p>
     * For example for a test method called {@code testSomeAwesomeFoldingCases()} the test file will be {@code someAwesomeFoldingCases.java}
     * within the test data path specified by {@link #getTestDataPath()}.
     */
    protected void doXmlTestFolding() {
        getFixture().configureByFile(getTestName(true) + ".xml");
        getFixture().testFoldingWithCollapseStatus(getTestDataPath() + "/" + getTestName(true) + ".xml");
    }
}
