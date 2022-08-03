//Copyright 2021 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.devkitplus.inspection;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;

/**
 * Functional test for {@link LightServiceRetrievalInspection}.
 */
public class LightServiceRetrievalInspectionTest extends DevKitPlusInspectionTestBase {

    @Override
    protected String getTestDataPath() {
        return super.getTestDataPath() + "inspection";
    }

    @Override
    protected void tuneFixture(JavaModuleFixtureBuilder<?> moduleBuilder) throws Exception {
        super.tuneFixture(moduleBuilder);
        loadUtilJar(moduleBuilder);
    }

    @Override
    protected InspectionProfileEntry getInspection() {
        return new LightServiceRetrievalInspection();
    }

    public void testSuspiciousLightServiceRetrieval() {
        doJavaTest();
    }
}
