//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.picimako.justkitting.JustKittingTestBase;

import java.util.function.Supplier;

/**
 * Base class for testing actions.
 */
public abstract class JustKittingActionTestBase extends JustKittingTestBase {

    protected void checkAction(String filename, Supplier<AnAction> action, String beforeText, String afterText) {
        getFixture().configureByText(filename, beforeText);
        ApplicationManager.getApplication().invokeAndWait(() -> getFixture().testAction(action.get()));
        getFixture().checkResult(afterText);
    }
}
