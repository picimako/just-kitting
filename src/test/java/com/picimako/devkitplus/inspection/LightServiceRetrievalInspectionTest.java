//Copyright 2021 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.devkitplus.inspection;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.picimako.devkitplus.ThirdPartyLibraryLoader;

/**
 * Functional test for {@link LightServiceRetrievalInspection}.
 */
public class LightServiceRetrievalInspectionTest extends DevKitPlusInspectionTestBase {

    @Override
    protected String getTestDataPath() {
        return super.getTestDataPath() + "inspection";
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ThirdPartyLibraryLoader.loadUtil(myFixture);
    }

    @Override
    protected InspectionProfileEntry getInspection() {
        return new LightServiceRetrievalInspection();
    }

    public void testSuspiciousLightServiceRetrieval() {
        doJavaTest();
    }
}
