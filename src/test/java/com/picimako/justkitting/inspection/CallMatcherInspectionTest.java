//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.inspection;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.picimako.justkitting.ThirdPartyLibraryLoader;

/**
 * Functional test for {@link CallMatcherInspection}.
 */
public class CallMatcherInspectionTest extends JustKittingInspectionTestBase {

    @Override
    protected InspectionProfileEntry getInspection() {
        return new CallMatcherInspection();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ThirdPartyLibraryLoader.loadJavaImpl(myFixture);
    }

    public void testNonExistentMethod() {
        doJavaTest("CallMatcherNonexistentMethod.java",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class CallMatcherNonexistentMethod {
                   CallMatcher callMatcherInstance1 = CallMatcher.instanceCall("java.util.List", "add");
                   CallMatcher callMatcherInstance2 = CallMatcher.instanceCall("java.util.List", "clear");
                   CallMatcher callMatcherInstance3 = CallMatcher.instanceCall("java.lang.Integer", <error descr="No instance method exists with this name in the referenced class or any of its super classes.">"toUnsignedString"</error>);
                   CallMatcher callMatcherInstance4 = CallMatcher.instanceCall("java.util.List", <error descr="No instance method exists with this name in the referenced class or any of its super classes.">"asdasd"</error>);

                   CallMatcher callMatcherStatic1 = CallMatcher.staticCall("java.text.MessageFormat", "format");
                   CallMatcher callMatcherStatic2 = CallMatcher.staticCall("java.lang.String", <error descr="No static method exists with this name in the referenced class or any of its super classes.">"chars"</error>);
                   CallMatcher callMatcherStatic3 = CallMatcher.staticCall("java.lang.String", <error descr="No static method exists with this name in the referenced class or any of its super classes.">"asdasd"</error>);

                   CallMatcher callMatcherExactInstance1 = CallMatcher.exactInstanceCall("java.util.List", "add");
                   CallMatcher callMatcherExactInstance2 = CallMatcher.exactInstanceCall("java.util.List", "clear");
                   CallMatcher callMatcherExactInstance3 = CallMatcher.exactInstanceCall("java.util.List", "isEmpty");
                   CallMatcher callMatcherExactInstance4 = CallMatcher.exactInstanceCall("java.util.List", <error descr="No instance method exists with this name in the referenced class.">"removeIf"</error>);
                   CallMatcher callMatcherExactInstance5 = CallMatcher.exactInstanceCall("java.util.List", <error descr="No instance method exists with this name in the referenced class.">"asdasd"</error>);
                }""");
    }
}
