//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.linemarker;

import static com.intellij.patterns.XmlPatterns.xmlAttribute;
import static com.intellij.patterns.XmlPatterns.xmlTag;
import static com.intellij.util.ReflectionUtil.getStaticFieldValue;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.patterns.XmlNamedElementPattern.XmlAttributePattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.picimako.justkitting.resources.JustKittingBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;

/**
 * This provider displays the actual referenced icons of {@code AnAction}s and eligible extensions in
 * plugin descriptor files in the following XML attributes:
 * <ul>
 *     <li>{@code idea-plugin.actions.action@icon}</li>
 *     <li>{@code idea-plugin.actions.group.action@icon}</li>
 *     <li>{@code idea-plugin.extensions.toolWindow@icon}</li>
 * </ul>
 * <p>
 * This line marker doesn't support resolving icons when the icon path is an actual fully qualified
 * name or a relative path within the project.
 *
 * @since 1.0.0
 */
final class PluginDescriptorIconLineMarkerProvider extends RelatedItemLineMarkerProvider {

    //Actions

    private static final XmlAttributePattern ACTIONS_ACTION_ICON_ATTRIBUTE_PATTERN =
        xmlAttribute().withLocalName("icon")
            .withParent(xmlTag().withLocalName("action")
                .withParent(xmlTag().withLocalName("actions")
                    .withParent(xmlTag().withLocalName("idea-plugin"))));
    private static final XmlAttributePattern GROUP_ACTION_ICON_ATTRIBUTE_PATTERN =
        xmlAttribute().withLocalName("icon")
            .withParent(xmlTag().withLocalName("action")
                .withParent(xmlTag().withLocalName("group")
                    .withParent(xmlTag().withLocalName("actions")
                        .withParent(xmlTag().withLocalName("idea-plugin")))));

    //Tool Window

    private static final XmlAttributePattern TOOL_WINDOW_ICON_ATTRIBUTE_PATTERN =
        xmlAttribute().withLocalName("icon")
            .withParent(xmlTag().withLocalName("toolWindow")
                .withParent(xmlTag().withLocalName("extensions")
                    .withParent(xmlTag().withLocalName("idea-plugin"))));

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        if (ACTIONS_ACTION_ICON_ATTRIBUTE_PATTERN.accepts(element)
            || GROUP_ACTION_ICON_ATTRIBUTE_PATTERN.accepts(element)
            || TOOL_WINDOW_ICON_ATTRIBUTE_PATTERN.accepts(element)) {
            var icon = determineIcon(element);
            if (icon != null)
                result.add(NavigationGutterIconBuilder.create(icon)
                    .setTooltipText(JustKittingBundle.message("line.marker.action.xml.icon"))
                    .setTarget(null)
                    .createLineMarkerInfo(element.getFirstChild()));
        }
    }

    @Nullable("When the icon path is invalid, or the _icon with the given path cannot be found.")
    private Icon determineIcon(@NotNull PsiElement element) {
        String iconRef = ((XmlAttribute) element).getValue();
        if (iconRef == null) return null;
        int lastIndexOfDot = iconRef.lastIndexOf('.');
        if (lastIndexOfDot == -1) return null;

        try {
            var iconsClass = iconRef.startsWith("AllIcons")
                             //E.g.: com.intellij.icons.AllIcons$Actions
                             ? Class.forName("com.intellij.icons." + iconRef.substring(0, lastIndexOfDot).replace('.', '$'))
                             //E.g.: icons.GradleIcons$ToolWindowGradle
                             : Class.forName("icons." + iconRef.substring(0, lastIndexOfDot).replace('.', '$'));

            //Gets the Icon value of the specified field name
            return getStaticFieldValue(iconsClass, Icon.class, iconRef.substring(lastIndexOfDot + 1));
        } catch (ClassNotFoundException e) {
            //Fall through to return null
        }
        return null;
    }

    @Override
    public String getName() {
        return JustKittingBundle.message("line.marker.action.xml.icon.name");
    }

    @Override
    public @Nullable Icon getIcon() {
        return AllIcons.FileTypes.Image;
    }
}
