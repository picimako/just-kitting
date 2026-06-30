//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting;

import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.KtFile;

import static com.intellij.openapi.application.ReadAction.computeBlocking;

final class ServiceLevelUtil {

    @Nullable
    static ServiceLevelDecider.ServiceLevel getServiceLevel(PsiFile psiFile) {
        if (psiFile instanceof PsiJavaFile javaFile)
            return ServiceLevelDecider.getServiceLevel(computeBlocking(javaFile::getClasses)[0]);
        if (psiFile instanceof KtFile ktFile)
            return ServiceLevelDecider.getServiceLevel(computeBlocking(ktFile::getClasses)[0]);

        return null;
    }

    private ServiceLevelUtil() {
        //Utility class
    }
}
