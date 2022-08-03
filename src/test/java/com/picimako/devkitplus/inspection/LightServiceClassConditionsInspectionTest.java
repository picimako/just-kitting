//Copyright 2021 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.devkitplus.inspection;

import com.intellij.codeInspection.InspectionProfileEntry;

/**
 * Functional test for {@link LightServiceClassConditionsInspection}.
 */
public class LightServiceClassConditionsInspectionTest extends DevKitPlusInspectionTestBase {

    @Override
    protected InspectionProfileEntry getInspection() {
        return new LightServiceClassConditionsInspection();
    }

    @Override
    protected String getTestDataPath() {
        return super.getTestDataPath() + "lightservice";
    }

    //Non-final light service

    public void testReportsNonFinalLightServiceClass() {
        myFixture.copyFileToProject("plugin.xml");
        doJavaTest("LightService.java",
            "import com.intellij.openapi.components.Service;\n" +
                "\n" +
                "@Service\n" +
                "public final class LightService {\n" +
                "}\n");
    }

    public void testDoesntReportsFinalLightServiceClass() {
        myFixture.copyFileToProject("plugin.xml");
        doJavaTest("LightService.java",
            "import com.intellij.openapi.components.Service;\n" +
                "\n" +
                "@Service\n" +
                "public class <error descr=\"Light Service class must be final.\">LightService</error> {\n" +
                "}\n");
    }

    public void testDoesntReportsNonFinalNonLightServiceClass() {
        myFixture.copyFileToProject("plugin.xml");
        doJavaTest("LightService.java",
            "public final class LightService {\n" +
                "}\n");
    }

    //service registered in plugin xml

    public void testServiceNotRegistered() {
        myFixture.copyFileToProject("plugin.xml");
        doJavaTest();
    }

    public void testServiceRegisteredInMainPluginXml() {
        myFixture.copyFileToProject("plugin.xml");
        doJavaTest();
    }

    public void testServiceRegisteredInDependencyPluginXml() {
        myFixture.copyFileToProject("plugin.xml");
        myFixture.copyFileToProject("dependency.xml");
        doJavaTest();
    }
}
