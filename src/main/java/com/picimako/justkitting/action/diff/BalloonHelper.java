//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.picimako.justkitting.action.diff;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.util.ui.JBInsets;
import com.picimako.justkitting.resources.JustKittingBundle;
import org.jetbrains.annotations.NotNull;

/**
 * Utility for showing balloons via {@link JBPopupFactory}.
 */
final class BalloonHelper {

    /**
     * Shows a notification balloon for an action source (e.g. project view tree node) on which the action was invoked.
     *
     * @param e          the event containing the data context for calculating the popup location
     * @param messageKey the message key to fetch from the {@link JustKittingBundle} to display in the balloon
     */
    static void showBalloonForAction(@NotNull AnActionEvent e, String messageKey) {
        if (e.getData(PlatformCoreDataKeys.CONTEXT_COMPONENT) != null) {
            var relativePoint = JBPopupFactory.getInstance().guessBestPopupLocation(e.getDataContext());
            JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(JustKittingBundle.message(messageKey), MessageType.WARNING, null)
                    .setBorderInsets(JBInsets.create(3, 3))
                    .createBalloon()
                    .show(relativePoint, Balloon.Position.below);
        }
    }

    private BalloonHelper() {
        //Utility class
    }
}
