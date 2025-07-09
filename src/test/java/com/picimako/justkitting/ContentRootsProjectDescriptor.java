//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.java.JavaResourceRootType;
import org.jetbrains.jps.model.java.JavaSourceRootType;

/**
 * Project descriptor that loads main/ and test/ sources and resources folders.
 */
public class ContentRootsProjectDescriptor extends DefaultLightProjectDescriptor {

    @Override
    public Sdk getSdk() {
        return JavaSdk.getInstance().createJdk("Real JDK", System.getenv("JAVA_HOME"), false);
    }

    @Override
    public void configureModule(@NotNull Module module, @NotNull ModifiableRootModel model, @NotNull ContentEntry contentEntry) {
        super.configureModule(module, model, contentEntry);

        contentEntry.clearSourceFolders();

        String contentEntryUrl = contentEntry.getUrl();
        contentEntry.addSourceFolder(contentEntryUrl + "/main/java", JavaSourceRootType.SOURCE);
        contentEntry.addSourceFolder(contentEntryUrl + "/main/resources", JavaResourceRootType.RESOURCE);
//        contentEntry.addSourceFolder(contentEntryUrl + "/test/java", JavaSourceRootType.TEST_SOURCE);
//        contentEntry.addSourceFolder(contentEntryUrl + "/test/resources", JavaResourceRootType.TEST_RESOURCE);
    }
}
