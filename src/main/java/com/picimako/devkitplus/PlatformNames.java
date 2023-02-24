//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.devkitplus;

/**
 * Provides strings for class, method and other names in the IntelliJ Platform.
 */
public final class PlatformNames {

    //Services
    public static final String SERVICE_ANNOTATION = "com.intellij.openapi.components.Service";
    public static final String SERVICE_LEVEL = "com.intellij.openapi.components.Service.Level";
    public static final String COMPONENT_MANAGER = "com.intellij.openapi.components.ComponentManager";
    public static final String PROJECT = "com.intellij.openapi.project.Project";
    public static final String APPLICATION = "com.intellij.openapi.application.Application";
    
    //Persistence
    public static final String STATE_ANNOTATION = "com.intellij.openapi.components.State";
    public static final String PERSISTENT_STATE_COMPONENT = "com.intellij.openapi.components.PersistentStateComponent";
    
    //Method calls
    public static final String CALL_MATCHER = "com.siyeh.ig.callMatcher.CallMatcher";
    
    //InspectionProfileEntry
    public static final String INSPECTION_PROFILE_ENTRY = "com.intellij.codeInspection.InspectionProfileEntry";
    public static final String MULTIPLE_CHECKBOX_OPTIONS_PANEL = "com.intellij.codeInspection.ui.MultipleCheckboxOptionsPanel";
    public static final String SINGLE_CHECKBOX_OPTIONS_PANEL = "com.intellij.codeInspection.ui.SingleCheckboxOptionsPanel";
    public static final String SINGLE_INTEGER_FIELD_OPTIONS_PANEL = "com.intellij.codeInspection.ui.SingleIntegerFieldOptionsPanel";
    public static final String CONVENTION_OPTIONS_PANEL = "com.intellij.codeInspection.ui.ConventionOptionsPanel";
    
    //Caching
    public static final String CACHED_VALUE_PROVIDER_RESULT = "com.intellij.psi.util.CachedValueProvider.Result";
    
    //PSI
    public static final String PSI_EXPRESSION_LIST = "com.intellij.psi.PsiExpressionList";
    public static final String PSI_CALL = "com.intellij.psi.PsiCall";

    private PlatformNames() {
        //Utility class
    }
}
