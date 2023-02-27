//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.intention.callmatcher;

import java.util.function.Consumer;

/**
 * Base type for generating {@link com.siyeh.ig.callMatcher.CallMatcher} initializer calls.
 */
public interface CallMatcherGenerator<T> {

    /**
     * Generates the CallMatcher initializer for the argument method's signature.
     *
     * @param method the method to generate a CallMatcher for
     * @param postActions any action to execute after the CallMatcher initializer is deleted.
     *                    This makes it possible to execute logic after for example a user's
     *                    choice of some sort on the UI.
     */
    void generateCallMatcher(T method, Consumer<String> postActions);
}
