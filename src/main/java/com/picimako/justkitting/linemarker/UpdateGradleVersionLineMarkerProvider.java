//Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.picimako.justkitting.linemarker;

import static com.intellij.execution.ProgramRunnerUtil.executeConfiguration;
import static com.picimako.justkitting.resources.JustKittingBundle.message;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor;
import com.intellij.codeInsight.daemon.MergeableLineMarkerInfo;
import com.intellij.execution.ExecutorRegistry;
import com.intellij.execution.RunManager;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.icons.AllIcons;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.lang.properties.psi.Property;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.gradle.service.execution.GradleExternalTaskConfigurationType;
import org.jetbrains.plugins.gradle.service.execution.GradleRunConfiguration;

import javax.swing.*;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This line marker is shown in {@code gradle.properties} files on the {@code gradleVersion} property
 * (coming from the intellij-platform-plugin-template), when the Gradle wrapper version there has been changed
 * compared to what is contained by the {@code /gradle/wrapper/gradle-wrapper.properties} file.
 * <p>
 * Upon clicking the icon, it creates a new Gradle run configuration with the following Gradle update command, and then runs it:
 * {@code wrapper --gradle-version=<version from gradle.properties> --distribution-type=<type, i.e. bin or all, from gradle-wrapper.properties>}.
 * <p>
 * NOTE: This line marker does not take into account multi-module plugin projects, thus potential multi-version Gradle wrappers.
 *
 * @since 1.0.0
 */
final class UpdateGradleVersionLineMarkerProvider extends LineMarkerProviderDescriptor {
    private static final Pattern WRAPPER_DISTRIBUTION_URL_PATTERN = Pattern.compile("https(\\\\)?://services.gradle.org/distributions/gradle-(?<version>\\d+\\.\\d+)-(?<type>bin|all).zip");
    private static final @NonNls String WRAPPER_UPDATE_COMMAND = "wrapper --gradle-version=%s --distribution-type=%s";

    @Override
    public String getName() {
        return message("line.marker.update.gradle.wrapper.version");
    }

    @Override
    public @Nullable Icon getIcon() {
        return AllIcons.Javaee.UpdateRunningApplication;
    }

    @Override
    public @Nullable LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        if (element instanceof Property property && isGradleVersionInGradleProperties(property)) {
            var project = element.getProject();
            var projectDir = ProjectUtil.guessProjectDir(project);
            if (projectDir != null && findGradleWrapperProperties(projectDir, project) instanceof PropertiesFile propertiesFile) {
                var distributionUrl = propertiesFile.findPropertyByKey("distributionUrl");
                if (distributionUrl == null) return null;

                String url = distributionUrl.getValue();
                if (url == null) return null;

                var matcher = WRAPPER_DISTRIBUTION_URL_PATTERN.matcher(url);
                if (matcher.matches()) {
                    //If the current version in gradle-wrapper.properties doesn't match with the new version in gradle.properties,
                    // meaning the wrapper should be updated
                    if (!matcher.group("version").equals(property.getValue())) {
                        String currentType = matcher.group("type");
                        return new UpdateGradleVersionLineMarkerInfo(property, property.getValue(), currentType);
                    }
                }
            }
        }

        return null;
    }

    /**
     *
     */
    private static boolean isGradleVersionInGradleProperties(Property property) {
        return "gradleVersion".equals(property.getName()) && "gradle.properties".equals(property.getContainingFile().getName());
    }

    /**
     *
     */
    @Nullable("When gradle-wrapper.properties cannot be found.")
    private static PsiFile findGradleWrapperProperties(VirtualFile projectDir, Project project) {
        var gradleWrapper = projectDir.findFileByRelativePath("/gradle/wrapper/gradle-wrapper.properties");
        return gradleWrapper != null ? PsiManager.getInstance(project).findFile(gradleWrapper) : null;
    }

    private static final class UpdateGradleVersionLineMarkerInfo extends MergeableLineMarkerInfo<PsiElement> {
        private final String newGradleVersion;
        private final String wrapperType;

        public UpdateGradleVersionLineMarkerInfo(Property property, String newGradleVersion, String wrapperType) {
            super(property.getFirstChild(),
                property.getFirstChild().getTextRange(),
                AllIcons.Javaee.UpdateRunningApplication,
                __ -> message("line.marker.update.gradle.wrapper.version"),
                null,
                GutterIconRenderer.Alignment.LEFT,
                () -> message("line.marker.update.gradle.wrapper.version"));
            this.newGradleVersion = newGradleVersion;
            this.wrapperType = wrapperType;
        }

        @Override
        public boolean canMergeWith(@NotNull MergeableLineMarkerInfo<?> info) {
            return info instanceof UpdateGradleVersionLineMarkerInfo && info.getIcon() == super.getIcon();
        }

        @Override
        public Icon getCommonIcon(@NotNull List<? extends MergeableLineMarkerInfo<?>> infos) {
            return super.getIcon();
        }

        @Override
        public GutterIconRenderer createGutterRenderer() {
            return new LineMarkerInfo.LineMarkerGutterIconRenderer<>(this) {
                @Override
                public AnAction getClickAction() {
                    return new AnAction() {
                        @Override
                        public void actionPerformed(@NotNull AnActionEvent e) {
                            var runManager = RunManager.getInstance(e.getProject());

                            //Create a Gradle run configuration, and set its command with the wrapper update one
                            var runConfig = runManager.createConfiguration(message("line.marker.update.gradle.wrapper.version"), new GradleExternalTaskConfigurationType().getFactory());
                            var gradleRunConfiguration = (GradleRunConfiguration) runConfig.getConfiguration();
                            gradleRunConfiguration.setRawCommandLine(WRAPPER_UPDATE_COMMAND.formatted(newGradleVersion, wrapperType));

                            //Add the run configuration to the list of configurations, and make it the selected one
                            runManager.addConfiguration(runConfig);
                            runManager.setSelectedConfiguration(runConfig);

                            //Execute the configuration in Run mode
                            executeConfiguration(runConfig, ExecutorRegistry.getInstance().getExecutorById(DefaultRunExecutor.EXECUTOR_ID));
                        }
                    };
                }

                @Override
                public boolean isNavigateAction() {
                    return true;
                }
            };
        }
    }
}
