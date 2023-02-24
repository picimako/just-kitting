//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.devkitplus;

import static com.intellij.openapi.editor.ex.util.EditorUtil.getEditorDataContext;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.popup.JBPopupFactory;

import java.util.List;

/**
 * Provides methods to work with list popups.
 */
public final class ListPopupHelper {

    /**
     * Shows a list popup with the provided list of actions.
     *
     * @param title     the title of the list popup. Can include & symbols for shortcut keys.
     *                  See {@link JBPopupFactory.ActionSelectionAid#MNEMONICS}.
     * @param listItems the list of actions to display
     * @param editor    the editor in which it is invoked from
     */
    public static void showActionsInListPopup(String title, List<? extends AnAction> listItems, Editor editor) {
        JBPopupFactory.getInstance().createActionGroupPopup(title,
            new DefaultActionGroup(listItems), getEditorDataContext(editor),
            JBPopupFactory.ActionSelectionAid.MNEMONICS, true
        ).showInBestPositionFor(editor);
    }

    public ListPopupHelper() {
        //Utility class
    }
}
