//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting;

import static com.intellij.openapi.application.ReadAction.compute;

import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.Nullable;

/**
 * Generic utility for dealing with service classes.
 */
public final class ServiceUtil {

    /**
     * Returns whether the argument class is annotated with {@link com.intellij.openapi.components.Service}.
     */
    public static boolean isLightService(@Nullable PsiClass psiClass) {
        return psiClass != null && compute(() -> psiClass.hasAnnotation(PlatformNames.SERVICE_ANNOTATION));
    }

    private ServiceUtil() {
        //Utility class
    }
}
