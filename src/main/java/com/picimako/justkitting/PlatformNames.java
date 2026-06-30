//Copyright 2026 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting;

/**
 * Provides strings for class, method and other names in the IntelliJ Platform.
 */
public final class PlatformNames {

    //Services
    public static final String SERVICE_ANNOTATION = "com.intellij.openapi.components.Service";
    public static final String SERVICE_LEVEL = "com.intellij.openapi.components.Service.Level";
    public static final String PROJECT = "com.intellij.openapi.project.Project";

    //Persistence
    public static final String STATE_ANNOTATION = "com.intellij.openapi.components.State";
    public static final String PERSISTENT_STATE_COMPONENT = "com.intellij.openapi.components.PersistentStateComponent";
    public static final String STORAGE_ANNOTATION = "com.intellij.openapi.components.Storage";
    
    //Method calls
    public static final String CALL_MATCHER = "com.siyeh.ig.callMatcher.CallMatcher";
    
    //Caching
    public static final String CACHED_VALUE_PROVIDER_RESULT = "com.intellij.psi.util.CachedValueProvider.Result";
    
    //PSI
    public static final String PSI_EXPRESSION_LIST = "com.intellij.psi.PsiExpressionList";
    public static final String PSI_CALL = "com.intellij.psi.PsiCall";

    //Modification trackers
    public static final String MODIFICATION_TRACKER_NEVER_CHANGED = "com.intellij.openapi.util.ModificationTracker.NEVER_CHANGED";
    public static final String PSI_MODIFICATION_TRACKER_MODIFICATION_COUNT = "com.intellij.psi.util.PsiModificationTracker.MODIFICATION_COUNT";

    private PlatformNames() {
        //Utility class
    }
}
