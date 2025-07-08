//Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.inspection;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.picimako.justkitting.ThirdPartyLibraryLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Functional test for {@link CallMatcherInspection}.
 */
public final class CallMatcherInspectionTest extends JustKittingInspectionTestBase {

    @Override
    protected InspectionProfileEntry getInspection() {
        return new CallMatcherInspection();
    }

    @BeforeEach
    protected void setUp() {
        ThirdPartyLibraryLoader.loadJavaImpl(getFixture());
    }

    @Test
    public void testNonExistentMethod() {
        doJavaTest("CallMatcherNonexistentMethod.java",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class CallMatcherNonexistentMethod {
                   private static final String JAVA_UTIL_LIST = "java.util.List";
                   private static final String JAVA_LANG_STRING = "java.lang.String";
                
                   CallMatcher callMatcherInstance1 = CallMatcher.instanceCall("java.util.List", "add");
                   CallMatcher callMatcherInstance2 = CallMatcher.instanceCall("java.util.List", "clear");
                   CallMatcher callMatcherInstance3 = CallMatcher.instanceCall("java.lang.Integer", <error descr="No instance method exists with this name in the referenced class or any of its super classes.">"toUnsignedString"</error>);
                   CallMatcher callMatcherInstance4 = CallMatcher.instanceCall("java.util.List", <error descr="No instance method exists with this name in the referenced class or any of its super classes.">"asdasd"</error>);
                   CallMatcher callMatcherInstance5 = CallMatcher.instanceCall(JAVA_UTIL_LIST, <error descr="No instance method exists with this name in the referenced class or any of its super classes.">"asdasd"</error>);

                   CallMatcher callMatcherStatic1 = CallMatcher.staticCall("java.text.MessageFormat", "format");
                   CallMatcher callMatcherStatic2 = CallMatcher.staticCall("java.lang.String", <error descr="No static method exists with this name in the referenced class or any of its super classes.">"chars"</error>);
                   CallMatcher callMatcherStatic3 = CallMatcher.staticCall("java.lang.String", <error descr="No static method exists with this name in the referenced class or any of its super classes.">"asdasd"</error>);
                   CallMatcher callMatcherStatic4 = CallMatcher.staticCall(JAVA_LANG_STRING, <error descr="No static method exists with this name in the referenced class or any of its super classes.">"asdasd"</error>);

                   CallMatcher callMatcherExactInstance1 = CallMatcher.exactInstanceCall("java.util.List", "add");
                   CallMatcher callMatcherExactInstance2 = CallMatcher.exactInstanceCall("java.util.List", "clear");
                   CallMatcher callMatcherExactInstance3 = CallMatcher.exactInstanceCall("java.util.List", "isEmpty");
                   CallMatcher callMatcherExactInstance4 = CallMatcher.exactInstanceCall("java.util.List", <error descr="No instance method exists with this name in the referenced class.">"removeIf"</error>);
                   CallMatcher callMatcherExactInstance5 = CallMatcher.exactInstanceCall("java.util.List", <error descr="No instance method exists with this name in the referenced class.">"asdasd"</error>);
                   CallMatcher callMatcherExactInstance6 = CallMatcher.exactInstanceCall(JAVA_UTIL_LIST, <error descr="No instance method exists with this name in the referenced class.">"asdasd"</error>);
                }""");
    }
}
