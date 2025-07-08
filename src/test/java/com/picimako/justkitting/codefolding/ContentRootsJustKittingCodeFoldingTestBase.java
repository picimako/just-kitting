// Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.codefolding;

import com.picimako.justkitting.ContentRootsBasedJustKittingTestBase;

/**
 * Base class for testing code folding.
 */
public abstract class ContentRootsJustKittingCodeFoldingTestBase extends ContentRootsBasedJustKittingTestBase {

    protected void doXmlTestFolding(String filePath) {
        getFixture().configureByFile(filePath);
        getFixture().testFoldingWithCollapseStatus(getTestDataPath() + "/" + filePath);
    }
}
