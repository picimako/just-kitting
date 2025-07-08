//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase5;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base class for functional tests.
 */
public abstract class JustKittingTestBase extends LightJavaCodeInsightFixtureTestCase5 {

    protected JustKittingTestBase() {
        super(new DefaultLightProjectDescriptor(() -> JavaSdk.getInstance().createJdk("Real JDK", System.getenv("JAVA_HOME"), false)));
    }

    protected JustKittingTestBase(LightProjectDescriptor projectDescriptor) {
        super(projectDescriptor);
    }

    @Override
    protected @Nullable String getTestDataPath() {
        return "src/test/testData/";
    }

    @BeforeEach
    protected void setUp() throws Exception {
        ThirdPartyLibraryLoader.loadUtil8(getFixture());
    }

    protected Project getProject() {
        return getFixture().getProject();
    }
}
