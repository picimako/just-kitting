//Copyright 2021 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.devkitplus;

import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for functional tests.
 */
public abstract class DevKitPlusTestBase extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ThirdPartyLibraryLoader.loadPlatformApi(myFixture);
    }

    @Override
    protected @NotNull LightProjectDescriptor getProjectDescriptor() {
        return getJdkHome();
    }

    /**
     * Returns a descriptor with a real JDK defined by the JAVA_HOME environment variable.
     */
    public static LightProjectDescriptor getJdkHome() {
        return new ProjectDescriptor(LanguageLevel.JDK_11) {
            @Override
            public Sdk getSdk() {
                return JavaSdk.getInstance().createJdk("Real JDK", System.getenv("JAVA_HOME"), false);
            }
        };
    }
}
