//Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.picimako.justkitting.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.picimako.justkitting.JustKittingTestBase;

import java.util.function.Supplier;

/**
 * Base class for testing actions.
 */
public abstract class JustKittingActionTestBase extends JustKittingTestBase {

    protected void checkAction(String filename, Supplier<AnAction> action, String beforeText, String afterText) {
        myFixture.configureByText(filename, beforeText);
        myFixture.testAction(action.get());
        myFixture.checkResult(afterText);
    }
}
