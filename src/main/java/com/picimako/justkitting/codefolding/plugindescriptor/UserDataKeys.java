//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.picimako.justkitting.codefolding.plugindescriptor;

import com.intellij.openapi.util.Key;

/**
 * Stores user data keys for data caching.
 */
public final class UserDataKeys {

    /**
     * Used to mark an XML tag's closing tag AstNode, so that it can be targeted when assembling
     * the placeholder text for it during code folding.
     *
     * @see IntentionActionFolder
     */
    public static final Key<String> CLOSING_TAG_NODE = Key.create("closingTagNode");

    private UserDataKeys() {
        //Utility class
    }
}
