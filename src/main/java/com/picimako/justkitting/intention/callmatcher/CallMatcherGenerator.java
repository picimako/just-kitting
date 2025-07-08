//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.intention.callmatcher;

import java.util.function.Consumer;

/**
 * Base type for generating {@link com.siyeh.ig.callMatcher.CallMatcher} initializer calls.
 */
public interface CallMatcherGenerator<METHOD, METHOD_CALL> {

    /**
     * Generates the CallMatcher initializer for the argument method's signature.
     *
     * @param method the method to generate a CallMatcher for
     * @param postActions any action to execute after the CallMatcher initializer is deleted.
     *                    This makes it possible to execute logic after for example a user's
     *                    choice of some sort on the UI.
     */
    void generateCallMatcherForMethod(METHOD method, Consumer<String> postActions);

    /**
     * Generates the CallMatcher initializer for the argument method call's signature.
     *
     * @param methodCall the method call to generate a CallMatcher for
     * @param postActions any action to execute after the CallMatcher initializer is deleted.
     *                    This makes it possible to execute logic after for example a user's
     *                    choice of some sort on the UI.
     */
    void generateCallMatcherForMethodCall(METHOD_CALL methodCall, Consumer<String> postActions);
}
