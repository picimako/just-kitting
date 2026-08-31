//Copyright 2026 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.linemarker;

import static com.intellij.openapi.application.ReadAction.computeBlocking;
import static com.intellij.patterns.XmlPatterns.xmlAttribute;
import static com.intellij.util.ReflectionUtil.getStaticFieldValue;
import static com.picimako.justkitting.resources.JustKittingBundle.message;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.text.Strings;
import com.intellij.patterns.XmlNamedElementPattern.XmlAttributePattern;
import com.intellij.patterns.XmlPatterns;
import com.intellij.patterns.XmlTagPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * This provider displays the actual referenced icons of {@code AnAction}s and eligible extensions in
 * plugin/module descriptor files in the following XML attributes:
 * <ul>
 *     <li>{@code idea-plugin.actions.action@icon}</li>
 *     <li>{@code idea-plugin.actions.group.action@icon} at any level of nesting of {@code <group>} tags</li>
 *     <li>{@code idea-plugin.extensions.toolWindow@icon}</li>
 * </ul>
 * <p>
 * This line marker doesn't support resolving icons when the icon path is a relative path within the project.
 *
 * @since 1.0.0
 */
final class PluginDescriptorIconLineMarkerProvider extends RelatedItemLineMarkerProvider {

    private static final Pattern ICONS_CLASS_PATTERN = Pattern.compile("^[A-Z][a-z]+Icons\\..+$");

    //Actions


    private static final XmlAttributePattern ACTIONS_ACTION_ICON_ATTRIBUTE_PATTERN =
        iconAttribute()
            .withParent(xmlTag("action")
                .withParent(xmlTag("actions")
                    .withParent(xmlTag("idea-plugin"))));

    private static final XmlAttributePattern GROUP_ACTION_ICON_ATTRIBUTE_PATTERN =
        iconAttribute()
            .withParent(xmlTag("action")
                .withParent(xmlTag("group")));

    //Tool Window

    private static final XmlAttributePattern TOOL_WINDOW_ICON_ATTRIBUTE_PATTERN =
        iconAttribute()
            .withParent(xmlTag("toolWindow")
                .withParent(xmlTag("extensions")
                    .withParent(xmlTag("idea-plugin"))));

    //Common

    private static XmlAttributePattern iconAttribute() {
        return xmlAttribute().withLocalName("icon");
    }

    private static XmlTagPattern.Capture xmlTag(@NonNls String localName) {
        return XmlPatterns.xmlTag().withLocalName(localName);
    }

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        if (ACTIONS_ACTION_ICON_ATTRIBUTE_PATTERN.accepts(element)
            || GROUP_ACTION_ICON_ATTRIBUTE_PATTERN.accepts(element)
            || TOOL_WINDOW_ICON_ATTRIBUTE_PATTERN.accepts(element)) {
            var icon = determineIcon(element);
            if (icon != null) {
                var iconBuilder = NavigationGutterIconBuilder.create(icon)
                    .setTooltipText(message("line.marker.action.xml.icon"))
                    .setTarget(null);
                result.add(computeBlocking(() -> iconBuilder.createLineMarkerInfo(element.getFirstChild())));
            }
        }
    }

    @Nullable("When the icon path is invalid, or the icon with the given path cannot be found.")
    private Icon determineIcon(@NotNull PsiElement element) {
        String iconRef = computeBlocking(() -> ((XmlAttribute) element).getValue());
        if (iconRef == null || iconRef.isBlank()) return null;
        int lastIndexOfDot = iconRef.lastIndexOf('.');
        if (lastIndexOfDot == -1) return null;

        try {
            String iconsFqn;
            //AllIcons:
            // - AllIcons.Actions.MenuSaveAll -> AllIcons$Actions -> -> com.intellij.icons.AllIcons$Actions
            if (iconRef.startsWith("AllIcons"))
                iconsFqn = "com.intellij.icons." + normalizedIconClassName(iconRef, lastIndexOfDot);

            //Icon classes in the 'icons' package, e.g.:
            // - GradleIcons.Gradle -> GradleIcons -> icons.GradleIcons
            // - JetgroovyIcons.Groovy.GroovyFile -> JetgroovyIcons$Groovy -> icons.JetgroovyIcons$Groovy
            else if (ICONS_CLASS_PATTERN.matcher(iconRef).matches())
                iconsFqn = "icons." + normalizedIconClassName(iconRef, lastIndexOfDot);

            //Fully qualified names, e.g.:
            else {
                int indexOfFirstUppercase = indexOfFirstUppercase(iconRef);
                if (indexOfFirstUppercase == -1) return null;

                int numberOfDots = Strings.countChars(/*classAndFieldNames: */iconRef.substring(indexOfFirstUppercase), '.');

                //e.g. org.intellij.images.ImagesIcons.ToggleTransparencyChessboard -> org.intellij.images.ImagesIcons
                if (numberOfDots == 1) iconsFqn = iconRef.substring(0, lastIndexOfDot);
                //e.g. org.intellij.plugins.markdown.MarkdownIcons.EditorActions.Table -> org.intellij.plugins.markdown.MarkdownIcons$EditorActions
                else if (numberOfDots > 1)
                    iconsFqn = iconRef.substring(0, indexOfFirstUppercase) + normalizedIconClassName(iconRef, indexOfFirstUppercase, lastIndexOfDot);
                else return null;
            }

            //Gets the Icon value of the specified field name
            return getStaticFieldValue(Class.forName(iconsFqn), Icon.class, iconRef.substring(lastIndexOfDot + 1));
        } catch (ClassNotFoundException e) {
            //Fall through to return null
        }
        return null;
    }

    private static String normalizedIconClassName(String iconRef, int endIndex) {
        return normalizedIconClassName(iconRef, 0, endIndex);
    }

    private static String normalizedIconClassName(String iconRef, int startIndex, int endIndex) {
        return iconRef.substring(startIndex, endIndex).replace('.', '$');
    }

    private static int indexOfFirstUppercase(@NotNull CharSequence s) {
        for (int i = 0; i < s.length(); i++) {
            if (Character.isUpperCase(s.charAt(i))) return i;
        }
        return -1;
    }

    @Override
    public String getName() {
        return message("line.marker.action.xml.icon.name");
    }

    @Override
    public Icon getIcon() {
        return AllIcons.FileTypes.Image;
    }
}
