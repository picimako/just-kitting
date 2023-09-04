//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.picimako.justkitting;

import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for tests that need to leverage main/ and test/ sources and resources folders in test data.
 */
public abstract class ContentRootsBasedJustKittingTestBase extends JustKittingTestBase {

    @NotNull
    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return new ContentRootsProjectDescriptor();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.copyDirectoryToProject("src", "");
    }
}
