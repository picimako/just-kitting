//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.picimako.justkitting.action.diff;

import com.intellij.diff.requests.DiffRequest;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.TestActionEvent;
import com.intellij.testFramework.TestDataPath;
import com.picimako.justkitting.action.JustKittingActionTestBase;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link CompareConfigFileWithPluginTemplateAction}.
 */
@TestDataPath("$CONTENT_ROOT/testData/diff")
public class CompareConfigFileWithPluginTemplateActionTest extends JustKittingActionTestBase {
    @Override
    protected String getTestDataPath() {
        return "src/test/testData/diff";
    }

    //Presentation/availability

    public void testDiffViewNotAvailableForNonExistentVirtualFile() {
        var e = wrapInTestActionEvent(null);

        new CompareConfigFileWithPluginTemplateAction().update(e);

        assertThat(e.getPresentation().isVisible()).isFalse();
    }

    public void testDiffViewNotAvailableForNonSupportedFile() {
        var e = wrapInTestActionEvent(myFixture.copyFileToProject("CHANGELOG.md"));

        new CompareConfigFileWithPluginTemplateAction().update(e);

        assertThat(e.getPresentation().isVisible()).isFalse();
    }

    public void testDiffViewAvailableForSupportedFileInRoot() {
        var e = wrapInTestActionEvent(myFixture.copyFileToProject("gradle.properties"));

        new CompareConfigFileWithPluginTemplateAction().update(e);

        assertThat(e.getPresentation().isVisible()).isTrue();
    }

    public void testDiffViewAvailableForSupportedFileInDirectory() {
        var e = wrapInTestActionEvent(myFixture.copyFileToProject(".github/dependabot.yml"));

        new CompareConfigFileWithPluginTemplateAction().update(e);

        assertThat(e.getPresentation().isVisible()).isTrue();
    }

    //Perform action

    public void testNoDiffViewForNonExistentVirtualFile() {
        var diffRequest = new Ref<DiffRequest>();
        var e = new TestActionEvent(dataId -> CommonDataKeys.PROJECT.is(dataId)
                ? getProject()
                : DiffDataKeys.DIFF_REQUEST.is(dataId) ? diffRequest : null);

        new CompareConfigFileWithPluginTemplateAction().actionPerformed(e);

        assertThat(diffRequest.isNull()).isTrue();
    }

    public void testDiffViewForFileInRoot() {
        var gradleProperties = myFixture.copyFileToProject("gradle.properties");
        var diffRequest = new Ref<DiffRequest>();
        var e = wrapInTestActionEvent(gradleProperties, diffRequest);

        //Perform action
        new CompareConfigFileWithPluginTemplateAction().actionPerformed(e);

        //Validate results
        assertThat(diffRequest.get()).isInstanceOf(SimpleDiffRequest.class);

        var simpleDiffRequest = (SimpleDiffRequest) diffRequest.get();
        assertThat(simpleDiffRequest.getContentTitles())
                .containsExactly("Platform Plugin Template", "Local");
        assertThat(simpleDiffRequest.toString())
                .matches("com\\.intellij\\.diff\\.requests\\.SimpleDiffRequest@[a-zA-Z0-9]+:\\[\\{}:DocumentImpl\\[null], \\{}:DocumentImpl\\[temp:///src/gradle\\.properties]]");
    }

    public void testDiffViewForFileInFolder() {
        var dependabot = myFixture.copyFileToProject(".github/dependabot.yml");
        var diffRequest = new Ref<DiffRequest>();
        var e = wrapInTestActionEvent(dependabot, diffRequest);

        //Perform action
        new CompareConfigFileWithPluginTemplateAction().actionPerformed(e);

        //Validate results
        assertThat(diffRequest.get()).isInstanceOf(SimpleDiffRequest.class);

        var simpleDiffRequest = (SimpleDiffRequest) diffRequest.get();
        assertThat(simpleDiffRequest.getContentTitles())
                .containsExactly("Platform Plugin Template", "Local");
        assertThat(simpleDiffRequest.toString())
                .matches("com\\.intellij\\.diff\\.requests\\.SimpleDiffRequest@[a-zA-Z0-9]+:\\[\\{}:DocumentImpl\\[null], \\{}:DocumentImpl\\[temp:///src/\\.github/dependabot\\.yml]]");
    }

//    public void testNoDiffViewWhenCouldNotFetchContentFromGitHub() {
//    }

    //Helpers

    private TestActionEvent wrapInTestActionEvent(VirtualFile file) {
        return new TestActionEvent(dataId -> {
            if (CommonDataKeys.VIRTUAL_FILE.is(dataId)) return file;
            if (CommonDataKeys.PROJECT.is(dataId)) return getProject();
            return null;
        });
    }

    private TestActionEvent wrapInTestActionEvent(VirtualFile file, Ref<DiffRequest> diffRequest) {
        return new TestActionEvent(dataId -> {
            if (CommonDataKeys.VIRTUAL_FILE.is(dataId)) return file;
            if (CommonDataKeys.PROJECT.is(dataId)) return getProject();
            if (DiffDataKeys.DIFF_REQUEST.is(dataId)) return diffRequest;
            return null;
        });
    }
}
