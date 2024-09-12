//Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.picimako.justkitting.action.diff;

import com.intellij.diff.requests.DiffRequest;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.util.Ref;

public final class DiffDataKeys {

    public static final DataKey<Ref<DiffRequest>> DIFF_REQUEST = DataKey.create("diffRequest");

    private DiffDataKeys() {
        //Utility class
    }
}
