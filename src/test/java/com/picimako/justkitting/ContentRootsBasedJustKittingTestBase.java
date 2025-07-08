//Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.picimako.justkitting;

import org.junit.jupiter.api.BeforeEach;

/**
 * Base class for tests that need to leverage main/ and test/ sources and resources folders in test data.
 */
public abstract class ContentRootsBasedJustKittingTestBase extends JustKittingTestBase {

    public ContentRootsBasedJustKittingTestBase() {
        super(new ContentRootsProjectDescriptor());
    }

    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        getFixture().copyDirectoryToProject("src", "");
    }
}
