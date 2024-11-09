//Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.picimako.justkitting.action.diff;

import static com.picimako.justkitting.resources.JustKittingBundle.message;

import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffManager;
import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Opens a two-sided diff view with a plugin configuration file and its version on GitHub
 * in the IntelliJ Platform Plugin Template.
 * <p>
 * The set of supported configuration files are listed in {@link #DIFFABLE_FILES}. For any other path,
 * this action is not displayed.
 *
 * @see <a href="https://github.com/JetBrains/intellij-platform-plugin-template">IntelliJ Platform Plugin Template</a>
 * @since 0.3.0
 */
public class CompareConfigFileWithPluginTemplateAction extends AnAction {
    private static final Logger LOG = Logger.getInstance(CompareConfigFileWithPluginTemplateAction.class);
    private static final String CHECK_LICENSE_RAW_URL = "https://raw.githubusercontent.com/JetBrains/marketplace-makemecoffee-plugin/refs/heads/master/src/main/java/com/company/license/CheckLicense.java";

    private static final Set<File> DIFFABLE_FILES = Set.of(
        new FileWithPath("build.gradle.kts", 1),
        new FileWithPath("gradle.properties", 1),
        new FileWithPath("qodana.yml", 1),
        new FileWithPath(".gitignore", 1),
        new FileWithPath(".github/dependabot.yml", 2),
        new FileWithPath(".github/workflows/build.yml", 3),
        new FileWithPath(".github/workflows/release.yml", 3),
        new FileWithPath(".github/workflows/run-ui-tests.yml", 3),
        new FileWithPath("gradle/libs.versions.toml", 2),
        new FileWithName("CheckLicense.java", CHECK_LICENSE_RAW_URL)
    );

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var project = e.getProject();
        if (project == null) return;

        //Finds the file that the action was invoked on. If there is no file, no diffing happens.
        var currentFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (currentFile == null) return;

        //Finds the project root directory
        var projectRoot = getProjectRootDir(e, currentFile);
        if (projectRoot == null) return;

        //Gets the target file's path relative to the current project root directory: e.g. ".github/workflows/build.yml"
        String relativeFilePath = getRelativeFilePath(currentFile, projectRoot);
        //If the relative path was not found, or it is not a file supported for diffing, then do nothing.
        if (relativeFilePath == null) return;

        var matchingFile = DIFFABLE_FILES.stream().filter(file -> file.isEligibleToDiff(relativeFilePath)).findFirst();
        if (matchingFile.isEmpty()) return;

        var localContent = createLocalDiffContent(project, projectRoot, relativeFilePath);
        if (localContent != null) {
            var targetFileType = FileTypeRegistry.getInstance().getFileTypeByFileName(currentFile.getName());
            var simpleDiffRequest = new SimpleDiffRequest(message("diff.editor.title"),
                createRemoteDiffContent(project, matchingFile.get(), targetFileType, e),
                localContent,
                message("diff.version.remote"),
                message("diff.version.local"));

            /*
             * During testing, showDiff() doesn't work properly.
             * Instead, the diff request object is made available for validation in the event
             *  object, since that is one common location between test and production code.
             */
            if (ApplicationManager.getApplication().isUnitTestMode())
                e.getData(DiffDataKeys.DIFF_REQUEST).set(simpleDiffRequest);
            else
                DiffManager.getInstance().showDiff(project, simpleDiffRequest);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        var file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (file == null) {
            e.getPresentation().setVisible(false);
            return;
        }

        //Finds the project root directory
        VirtualFile projectRootDir = getProjectRootDir(e, file);
        if (projectRootDir == null) return;

        e.getPresentation().setVisible(true);
    }

    /**
     * Determines the project root directory based on the current files relative path.
     * <p>
     * This is a replacement for {@link com.intellij.openapi.project.ProjectUtil#guessProjectDir(Project)}
     * to support workspaces in which case the guessing may not be accurate, and may find a different a project's root.
     */
    @Nullable("When the project root dir was not found.")
    private static VirtualFile getProjectRootDir(@NotNull AnActionEvent e, VirtualFile currentFile) {
        String filePath = currentFile.getPath();
        var matchingFile = DIFFABLE_FILES.stream()
            //Replacing back- and forward slashes to forward slashes because that's what we need in the GitHub URL
            .filter(file -> file.isEligibleToDiff(filePath))
            .findFirst();
        //If the current file is not among the diffable ones, the action becomes hidden
        if (matchingFile.isEmpty()) {
            e.getPresentation().setVisible(false);
            return null;
        }

        File file = matchingFile.get();
        int possibleMaxParentCount = 0;
        if (file instanceof FileWithPath fileWithPath)
            possibleMaxParentCount = fileWithPath.levelOfNesting;
        else if (file instanceof FileWithName)
            possibleMaxParentCount = StringUtil.countChars(normalizeSlashes(currentFile.getPath()), '/');

        if (possibleMaxParentCount != 0) {
            VirtualFile projectRoot = currentFile;
            for (int i = 1; i <= possibleMaxParentCount; i++) {
                projectRoot = projectRoot.getParent();
                //If any parent of the current file is not found, the action becomes hidden
                if (projectRoot == null) {
                    e.getPresentation().setVisible(false);
                    return null;
                }
            }
            return projectRoot;
        }

        return null;
    }

    @Nullable
    private String getRelativeFilePath(VirtualFile file, VirtualFile projectRoot) {
        String relativePath = VfsUtilCore.getRelativePath(file, projectRoot);
        if (relativePath == null) return null;

        //Replacing back- and forward slashes to forward slashes because that's what we need in the GitHub URL
        return normalizeSlashes(relativePath);
    }

    private static String normalizeSlashes(String path) {
        return path.replaceAll("\\|/", "/");
    }

    /**
     * Creates the diff content for the local version of the file at {@code filePath} under {@code projectRoot}.
     */
    @Nullable
    private DiffContent createLocalDiffContent(Project project, VirtualFile projectRoot, String filePath) {
        //Create the diff content for the file in the current project
        var localFile = projectRoot.findFileByRelativePath(filePath);
        if (localFile == null) return null;

        return DiffContentFactory.getInstance().create(project, localFile);
    }

    /**
     * Creates the diff content for the remote version of the file on GitHub at {@code filePath} under the
     * project's repository root.
     */
    @NotNull
    private DocumentContent createRemoteDiffContent(Project project, File file, FileType fileType, AnActionEvent event) {
        //Get file content from the intellij-platform-plugin-template GitHub repository, and create diff content
        String contentFromRemote = getContentFromPlatformPluginTemplateOnGitHub(file, event);
        return DiffContentFactory.getInstance().create(project, contentFromRemote, fileType, true);
    }

    /**
     * Sends a GET request to GitHub to fetch the text content of the file at {@code filePath}
     *
     * @param file the abstract file representing the diffable resource
     * @return the String text content of the file
     */
    private String getContentFromPlatformPluginTemplateOnGitHub(File file, AnActionEvent event) {
        //Using try-with-resources, the entity content conversion (or maybe this whole method) is executed twice,
        // and second time the content is an empty string, which is displayed in the diff view, instead of the actual
        // content received from the first invocation.
        if (ApplicationManager.getApplication().isUnitTestMode())
            return "This is a dummy content for testing.";

        var client = HttpClients.createDefault();
        try {
            var response = client.execute(new HttpGet(file.rawUrl()));
            if (response.getEntity() != null) {
                return IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            }
            client.close();
        } catch (IOException e) {
            LOG.warn(message("diff.version.remote.could.not.get.content"), e);
        }
        BalloonHelper.showBalloonForAction(event, "diff.version.remote.could.not.get.content");
        return null;
    }

    /**
     * A diffable file.
     */
    private interface File {
        /**
         * If this file is eligible to provide diffing information for the given relative file path.
         *
         * @param relativeFilePath relative file path of the file chosen by the user for diffing
         */
        boolean isEligibleToDiff(String relativeFilePath);

        /**
         * The raw GitHub URL of this file.
         */
        String rawUrl();
    }

    /**
     * A diffable file with a relative path and the known level of nesting.
     *
     * @param path           the path of the file relative to the project root directory
     * @param levelOfNesting the level of nesting of the file. 1 means the file is in the root directory.
     */
    private record FileWithPath(String path, int levelOfNesting) implements File {
        private static final String RAW_GH_USER_CONTENT_BASE_URL = "https://raw.githubusercontent.com/JetBrains/intellij-platform-plugin-template/main/";

        @Override
        public boolean isEligibleToDiff(String relativeFilePath) {
            return normalizeSlashes(relativeFilePath).endsWith(path);
        }

        @Override
        public String rawUrl() {
            return RAW_GH_USER_CONTENT_BASE_URL + path;
        }
    }

    /**
     * A diffable file with a name and its raw GitHub URL.
     *
     * @param name   the name of the file, including its extension
     * @param rawUrl the complete raw GitHub URL of the file in case it is not part of the IntelliJ Platform Plugin Template.
     */
    private record FileWithName(String name, String rawUrl) implements File {
        @Override
        public boolean isEligibleToDiff(String relativeFilePath) {
            return relativeFilePath.endsWith(name);
        }
    }
}
