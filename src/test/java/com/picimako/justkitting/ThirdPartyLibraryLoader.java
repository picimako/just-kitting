//Copyright 2022 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting;

import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Loads third-party libraries for integration testing.
 */
public final class ThirdPartyLibraryLoader {

    private static final String THIRD_PARTY_LIB_DIRECTORY = "lib";

    //Libraries are from IC-2021.3

    public static void loadPlatformApi(@NotNull CodeInsightTestFixture fixture) {
        loadLibrary(fixture, "platform-api", "platform-api.jar");
    }

    public static void loadUtil(@NotNull CodeInsightTestFixture fixture) {
        loadLibrary(fixture, "util", "util.jar");
    }

    public static void loadJavaImpl(@NotNull CodeInsightTestFixture fixture) {
        loadLibrary(fixture, "java-impl", "java-impl.jar");
    }

    public static void loadJavaApi(@NotNull CodeInsightTestFixture fixture) {
        loadLibrary(fixture, "java-api", "java-api.jar");
    }

    /**
     * Loads the library with the given filename from the [PROJECT_ROOT]/lib folder.
     *
     * @param libraryName       the name of the library
     * @param libraryJarName    the filename to load
     */
    public static void loadLibrary(@NotNull CodeInsightTestFixture fixture, String libraryName, String libraryJarName) {
        String libPath = PathUtil.toSystemIndependentName(new File(THIRD_PARTY_LIB_DIRECTORY).getAbsolutePath());
        VfsRootAccess.allowRootAccess(fixture.getTestRootDisposable(), libPath);
        PsiTestUtil.addLibrary(fixture.getTestRootDisposable(), fixture.getModule(), libraryName, libPath, libraryJarName);
    }

    private ThirdPartyLibraryLoader() {
        //Utility class
    }
}
