//Copyright 2021 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.devkitplus;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.roots.LanguageLevelProjectExtension;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.PsiClass;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;
import com.intellij.util.PathUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.siyeh.ig.callMatcher.CallMatcher;

/**
 * Base class for functional tests.
 */
public abstract class DevKitPlusTestBase extends JavaCodeInsightFixtureTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        LanguageLevelProjectExtension.getInstance(getProject()).setLanguageLevel(LanguageLevel.JDK_11);
    }

    @Override
    protected void tuneFixture(JavaModuleFixtureBuilder<?> moduleBuilder) throws Exception {
        loadPlatformApiJar(moduleBuilder);
    }
    
    protected void loadPlatformApiJar(JavaModuleFixtureBuilder<?> moduleBuilder) {
        moduleBuilder.addLibrary("platform-api", PathUtil.getJarPathForClass(Service.class));
    }
    
    protected void loadUtilJar(JavaModuleFixtureBuilder<?> moduleBuilder) {
        moduleBuilder.addLibrary("util.jar", PathUtil.getJarPathForClass(XmlSerializerUtil.class));
    }

    protected void loadJavaImplJar(JavaModuleFixtureBuilder<?> moduleBuilder) {
        moduleBuilder.addLibrary("java-impl.jar", PathUtil.getJarPathForClass(CallMatcher.class));
    }

    protected void loadJavaApiJar(JavaModuleFixtureBuilder<?> moduleBuilder) {
        moduleBuilder.addLibrary("java-api.jar", PathUtil.getJarPathForClass(PsiClass.class));
    }
}
