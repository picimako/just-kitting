//Copyright 2021 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.devkitplus.inspection;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;

/**
 * Functional test for {@link CallMatcherInspection}.
 */
public class CallMatcherInspectionTest extends DevKitPlusInspectionTestBase {

    @Override
    protected InspectionProfileEntry getInspection() {
        return new CallMatcherInspection();
    }

    @Override
    protected void tuneFixture(JavaModuleFixtureBuilder<?> moduleBuilder) throws Exception {
        loadJavaImplJar(moduleBuilder);
    }

    public void testNonExistentMethod() {
        doJavaTest("CallMatcherNonexistentMethod.java",
            "import com.siyeh.ig.callMatcher.CallMatcher;\n" +
                "\n" +
                "public class CallMatcherNonexistentMethod {\n" +
                "   CallMatcher callMatcherInstance1 = CallMatcher.instanceCall(\"java.util.List\", \"add\");\n" +
                "   CallMatcher callMatcherInstance2 = CallMatcher.instanceCall(\"java.util.List\", \"clear\");\n" +
                "   CallMatcher callMatcherInstance3 = CallMatcher.instanceCall(\"java.lang.Integer\", <error descr=\"No instance method exists with this name in the referenced class or any of its super classes.\">\"toUnsignedString\"</error>);\n" +
                "   CallMatcher callMatcherInstance4 = CallMatcher.instanceCall(\"java.util.List\", <error descr=\"No instance method exists with this name in the referenced class or any of its super classes.\">\"asdasd\"</error>);\n" +
                "\n" +
                "   CallMatcher callMatcherStatic1 = CallMatcher.staticCall(\"java.text.MessageFormat\", \"format\");\n" +
                "   CallMatcher callMatcherStatic2 = CallMatcher.staticCall(\"java.lang.String\", <error descr=\"No static method exists with this name in the referenced class or any of its super classes.\">\"chars\"</error>);\n" +
                "   CallMatcher callMatcherStatic3 = CallMatcher.staticCall(\"java.lang.String\", <error descr=\"No static method exists with this name in the referenced class or any of its super classes.\">\"asdasd\"</error>);\n" +
                "\n" +
                "   CallMatcher callMatcherExactInstance1 = CallMatcher.exactInstanceCall(\"java.util.List\", \"add\");\n" +
                "   CallMatcher callMatcherExactInstance2 = CallMatcher.exactInstanceCall(\"java.util.List\", \"clear\");\n" +
                "   CallMatcher callMatcherExactInstance3 = CallMatcher.exactInstanceCall(\"java.util.List\", \"isEmpty\");\n" +
                "   CallMatcher callMatcherExactInstance4 = CallMatcher.exactInstanceCall(\"java.util.List\", <error descr=\"No instance method exists with this name in the referenced class.\">\"removeIf\"</error>);\n" +
                "   CallMatcher callMatcherExactInstance5 = CallMatcher.exactInstanceCall(\"java.util.List\", <error descr=\"No instance method exists with this name in the referenced class.\">\"asdasd\"</error>);\n" +
                "}");
    }
}
