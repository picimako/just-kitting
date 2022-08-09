//Copyright 2021 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.devkitplus.intention.callmatcher;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.psi.PsiFile;

import com.picimako.devkitplus.ThirdPartyLibraryLoader;
import com.picimako.devkitplus.intention.DevKitPlusIntentionTestBase;

/**
 * Functional test for {@link CallMatchersConversionToAnyOfIntention}.
 */
public class CallMatchersConversionToAnyOfIntentionTest extends DevKitPlusIntentionTestBase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ThirdPartyLibraryLoader.loadJavaImpl(myFixture);
    }

    @Override
    protected IntentionAction getIntention() {
        return new CallMatchersConversionToAnyOfIntention();
    }

    //Positive cases
    public void testConversionOfBinaryExpression() {
        doIntentionTest("BinaryExpression.java",
            "import com.siyeh.ig.callMatcher.CallMatcher;\n" +
                "\n" +
                "public class BinaryExpression {\n" +
                "    static final CallMatcher LIST_OF = CallMatcher.staticCall(\"java.util.List\", \"of\");\n" +
                "    static final CallMatcher SET_OF = CallMatcher.staticCall(\"java.util.Set\", \"of\");\n" +
                "    \n" +
                "    public void method(PsiMethodCallExpression expression) {\n" +
                "        if (<selection>LIST_OF.matches(expression) || SET_OF.matches(expression)</selection>) {\n" +
                "        }\n" +
                "    }\n" +
                "}",
            "import com.siyeh.ig.callMatcher.CallMatcher;\n" +
                "\n" +
                "public class BinaryExpression {\n" +
                "    static final CallMatcher LIST_OF = CallMatcher.staticCall(\"java.util.List\", \"of\");\n" +
                "    static final CallMatcher SET_OF = CallMatcher.staticCall(\"java.util.Set\", \"of\");\n" +
                "    private static final CallMatcher CALL_MATCHER = CallMatcher.anyOf(LIST_OF, SET_OF);\n" +
                "\n" +
                "    public void method(PsiMethodCallExpression expression) {\n" +
                "        if (CALL_MATCHER.matches(expression)) {\n" +
                "        }\n" +
                "    }\n" +
                "}");
    }

    public void testConversionOfParenthesisedBinaryExpression() {
        doIntentionTest("BinaryParenthesised.java",
            "import com.siyeh.ig.callMatcher.CallMatcher;\n" +
                "\n" +
                "public class BinaryParenthesised {\n" +
                "    static final CallMatcher LIST_OF = CallMatcher.staticCall(\"java.util.List\", \"of\");\n" +
                "    static final CallMatcher SET_OF = CallMatcher.staticCall(\"java.util.Set\", \"of\");\n" +
                "    \n" +
                "    public void method(PsiMethodCallExpression expression) {\n" +
                "        if (<selection>(LIST_OF.matches(expression) || SET_OF.matches(expression))</selection>) {\n" +
                "        }\n" +
                "    }\n" +
                "}",
            "import com.siyeh.ig.callMatcher.CallMatcher;\n" +
                "\n" +
                "public class BinaryParenthesised {\n" +
                "    static final CallMatcher LIST_OF = CallMatcher.staticCall(\"java.util.List\", \"of\");\n" +
                "    static final CallMatcher SET_OF = CallMatcher.staticCall(\"java.util.Set\", \"of\");\n" +
                "    private static final CallMatcher CALL_MATCHER = CallMatcher.anyOf(LIST_OF, SET_OF);\n" +
                "\n" +
                "    public void method(PsiMethodCallExpression expression) {\n" +
                "        if ((CALL_MATCHER.matches(expression))) {\n" +
                "        }\n" +
                "    }\n" +
                "}");
    }

    public void testConversionOfPolyadicExpression() {
        doIntentionTest("Polyadic.java",
            "import com.siyeh.ig.callMatcher.CallMatcher;\n" +
                "\n" +
                "public class Polyadic {\n" +
                "    static final CallMatcher LIST_OF = CallMatcher.staticCall(\"java.util.List\", \"of\");\n" +
                "    static final CallMatcher SET_OF = CallMatcher.staticCall(\"java.util.Set\", \"of\");\n" +
                "    static final CallMatcher MAP_OF = CallMatcher.staticCall(\"java.util.Map\", \"of\");\n" +
                "    \n" +
                "    public void method(PsiMethodCallExpression expression) {\n" +
                "        if (<selection>LIST_OF.matches(expression) || SET_OF.matches(expression) || MAP_OF.matches(expression)</selection>) {\n" +
                "        }\n" +
                "    }\n" +
                "}",
            "import com.siyeh.ig.callMatcher.CallMatcher;\n" +
                "\n" +
                "public class Polyadic {\n" +
                "    static final CallMatcher LIST_OF = CallMatcher.staticCall(\"java.util.List\", \"of\");\n" +
                "    static final CallMatcher SET_OF = CallMatcher.staticCall(\"java.util.Set\", \"of\");\n" +
                "    static final CallMatcher MAP_OF = CallMatcher.staticCall(\"java.util.Map\", \"of\");\n" +
                "    private static final CallMatcher CALL_MATCHER = CallMatcher.anyOf(LIST_OF, SET_OF, MAP_OF);\n" +
                "\n" +
                "    public void method(PsiMethodCallExpression expression) {\n" +
                "        if (CALL_MATCHER.matches(expression)) {\n" +
                "        }\n" +
                "    }\n" +
                "}");
    }

    public void testConversionOfParenthesisedPolyadicExpression() {
        doIntentionTest("PolyadicParenthesised.java",
            "import com.siyeh.ig.callMatcher.CallMatcher;\n" +
                "\n" +
                "public class PolyadicParenthesised {\n" +
                "    static final CallMatcher LIST_OF = CallMatcher.staticCall(\"java.util.List\", \"of\");\n" +
                "    static final CallMatcher SET_OF = CallMatcher.staticCall(\"java.util.Set\", \"of\");\n" +
                "    static final CallMatcher MAP_OF = CallMatcher.staticCall(\"java.util.Map\", \"of\");\n" +
                "    \n" +
                "    public void method(PsiMethodCallExpression expression) {\n" +
                "        if (<selection>(LIST_OF.matches(expression) || SET_OF.matches(expression) || MAP_OF.matches(expression))</selection>) {\n" +
                "        }\n" +
                "    }\n" +
                "}",
            "import com.siyeh.ig.callMatcher.CallMatcher;\n" +
                "\n" +
                "public class PolyadicParenthesised {\n" +
                "    static final CallMatcher LIST_OF = CallMatcher.staticCall(\"java.util.List\", \"of\");\n" +
                "    static final CallMatcher SET_OF = CallMatcher.staticCall(\"java.util.Set\", \"of\");\n" +
                "    static final CallMatcher MAP_OF = CallMatcher.staticCall(\"java.util.Map\", \"of\");\n" +
                "    private static final CallMatcher CALL_MATCHER = CallMatcher.anyOf(LIST_OF, SET_OF, MAP_OF);\n" +
                "\n" +
                "    public void method(PsiMethodCallExpression expression) {\n" +
                "        if ((CALL_MATCHER.matches(expression))) {\n" +
                "        }\n" +
                "    }\n" +
                "}");
    }

    public void testConversionWithTestMethod() {
        doIntentionTest("Test.java",
            "import com.siyeh.ig.callMatcher.CallMatcher;\n" +
                "\n" +
                "public class Test {\n" +
                "    static final CallMatcher LIST_OF = CallMatcher.staticCall(\"java.util.List\", \"of\");\n" +
                "    static final CallMatcher SET_OF = CallMatcher.staticCall(\"java.util.Set\", \"of\");\n" +
                "    \n" +
                "    public void method(PsiMethodCallExpression expression) {\n" +
                "        if (<selection>LIST_OF.test(expression) || SET_OF.test(expression)</selection>) {\n" +
                "        }\n" +
                "    }\n" +
                "}",
            "import com.siyeh.ig.callMatcher.CallMatcher;\n" +
                "\n" +
                "public class Test {\n" +
                "    static final CallMatcher LIST_OF = CallMatcher.staticCall(\"java.util.List\", \"of\");\n" +
                "    static final CallMatcher SET_OF = CallMatcher.staticCall(\"java.util.Set\", \"of\");\n" +
                "    private static final CallMatcher CALL_MATCHER = CallMatcher.anyOf(LIST_OF, SET_OF);\n" +
                "\n" +
                "    public void method(PsiMethodCallExpression expression) {\n" +
                "        if (CALL_MATCHER.test(expression)) {\n" +
                "        }\n" +
                "    }\n" +
                "}");
    }

    public void testConversionWithMethodMatchesMethod() {
        doIntentionTest("Method.java",
            "import com.siyeh.ig.callMatcher.CallMatcher;\n" +
                "\n" +
                "public class Method {\n" +
                "    static final CallMatcher LIST_OF = CallMatcher.staticCall(\"java.util.List\", \"of\");\n" +
                "    static final CallMatcher SET_OF = CallMatcher.staticCall(\"java.util.Set\", \"of\");\n" +
                "    \n" +
                "    public void method(PsiMethodCallExpression expression) {\n" +
                "        if (<selection>LIST_OF.methodMatches(expression) || SET_OF.methodMatches(expression)</selection>) {\n" +
                "        }\n" +
                "    }\n" +
                "}",
            "import com.siyeh.ig.callMatcher.CallMatcher;\n" +
                "\n" +
                "public class Method {\n" +
                "    static final CallMatcher LIST_OF = CallMatcher.staticCall(\"java.util.List\", \"of\");\n" +
                "    static final CallMatcher SET_OF = CallMatcher.staticCall(\"java.util.Set\", \"of\");\n" +
                "    private static final CallMatcher CALL_MATCHER = CallMatcher.anyOf(LIST_OF, SET_OF);\n" +
                "\n" +
                "    public void method(PsiMethodCallExpression expression) {\n" +
                "        if (CALL_MATCHER.methodMatches(expression)) {\n" +
                "        }\n" +
                "    }\n" +
                "}");
    }

    public void testConversionWithMethodReferenceMatchesMethod() {
        doIntentionTest("MethodReference.java",
            "import com.siyeh.ig.callMatcher.CallMatcher;\n" +
                "\n" +
                "public class MethodReference {\n" +
                "    static final CallMatcher LIST_OF = CallMatcher.staticCall(\"java.util.List\", \"of\");\n" +
                "    static final CallMatcher SET_OF = CallMatcher.staticCall(\"java.util.Set\", \"of\");\n" +
                "    \n" +
                "    public void method(PsiMethodCallExpression expression) {\n" +
                "        if (<selection>LIST_OF.methodReferenceMatches(expression) || SET_OF.methodReferenceMatches(expression)</selection>) {\n" +
                "        }\n" +
                "    }\n" +
                "}",
            "import com.siyeh.ig.callMatcher.CallMatcher;\n" +
                "\n" +
                "public class MethodReference {\n" +
                "    static final CallMatcher LIST_OF = CallMatcher.staticCall(\"java.util.List\", \"of\");\n" +
                "    static final CallMatcher SET_OF = CallMatcher.staticCall(\"java.util.Set\", \"of\");\n" +
                "    private static final CallMatcher CALL_MATCHER = CallMatcher.anyOf(LIST_OF, SET_OF);\n" +
                "\n" +
                "    public void method(PsiMethodCallExpression expression) {\n" +
                "        if (CALL_MATCHER.methodReferenceMatches(expression)) {\n" +
                "        }\n" +
                "    }\n" +
                "}");
    }

    public void testConversionWithUCallMatchesMethod() {
        doIntentionTest("UCall.java",
            "import com.siyeh.ig.callMatcher.CallMatcher;\n" +
                "\n" +
                "public class UCall {\n" +
                "    static final CallMatcher LIST_OF = CallMatcher.staticCall(\"java.util.List\", \"of\");\n" +
                "    static final CallMatcher SET_OF = CallMatcher.staticCall(\"java.util.Set\", \"of\");\n" +
                "    \n" +
                "    public void method(PsiMethodCallExpression expression) {\n" +
                "        if (<selection>LIST_OF.uCallMatches(expression) || SET_OF.uCallMatches(expression)</selection>) {\n" +
                "        }\n" +
                "    }\n" +
                "}",
            "import com.siyeh.ig.callMatcher.CallMatcher;\n" +
                "\n" +
                "public class UCall {\n" +
                "    static final CallMatcher LIST_OF = CallMatcher.staticCall(\"java.util.List\", \"of\");\n" +
                "    static final CallMatcher SET_OF = CallMatcher.staticCall(\"java.util.Set\", \"of\");\n" +
                "    private static final CallMatcher CALL_MATCHER = CallMatcher.anyOf(LIST_OF, SET_OF);\n" +
                "\n" +
                "    public void method(PsiMethodCallExpression expression) {\n" +
                "        if (CALL_MATCHER.uCallMatches(expression)) {\n" +
                "        }\n" +
                "    }\n" +
                "}");
    }

    public void testConversionInVariableAssignment() {
        doIntentionTest("VariableAssignment.java",
            "import com.siyeh.ig.callMatcher.CallMatcher;\n" +
                "\n" +
                "public class VariableAssignment {\n" +
                "    static final CallMatcher LIST_OF = CallMatcher.staticCall(\"java.util.List\", \"of\");\n" +
                "    static final CallMatcher SET_OF = CallMatcher.staticCall(\"java.util.Set\", \"of\");\n" +
                "    \n" +
                "    public void method(PsiMethodCallExpression expression) {\n" +
                "        var isCollection = <selection>LIST_OF.matches(expression) || SET_OF.matches(expression)</selection>;\n" +
                "    }\n" +
                "}",
            "import com.siyeh.ig.callMatcher.CallMatcher;\n" +
                "\n" +
                "public class VariableAssignment {\n" +
                "    static final CallMatcher LIST_OF = CallMatcher.staticCall(\"java.util.List\", \"of\");\n" +
                "    static final CallMatcher SET_OF = CallMatcher.staticCall(\"java.util.Set\", \"of\");\n" +
                "    private static final CallMatcher CALL_MATCHER = CallMatcher.anyOf(LIST_OF, SET_OF);\n" +
                "\n" +
                "    public void method(PsiMethodCallExpression expression) {\n" +
                "        var isCollection = CALL_MATCHER.matches(expression);\n" +
                "    }\n" +
                "}");
    }

    //No availability cases

    public void testNotAvailableForNonOrBinaryExpression() {
        PsiFile psiFile = myFixture.configureByText("NotAvailable.java",
            "import com.siyeh.ig.callMatcher.CallMatcher;\n" +
                "\n" +
                "public class NotAvailable {\n" +
                "    static final CallMatcher LIST_OF = CallMatcher.staticCall(\"java.util.List\", \"of\");\n" +
                "    static final CallMatcher SET_OF = CallMatcher.staticCall(\"java.util.Set\", \"of\");\n" +
                "    \n" +
                "    public void method(PsiMethodCallExpression expression) {\n" +
                "        if (<selection>LIST_OF.matches(expression) && SET_OF.matches(expression)</selection>) {\n" +
                "        }\n" +
                "    }\n" +
                "}");
        checkIfNotAvailableIn(psiFile);
    }

    public void testNotAvailableForNonOrPolyadicExpression() {
        PsiFile psiFile = myFixture.configureByText("NotAvailable.java",
            "import com.siyeh.ig.callMatcher.CallMatcher;\n" +
                "\n" +
                "public class NotAvailable {\n" +
                "    static final CallMatcher LIST_OF = CallMatcher.staticCall(\"java.util.List\", \"of\");\n" +
                "    static final CallMatcher SET_OF = CallMatcher.staticCall(\"java.util.Set\", \"of\");\n" +
                "    static final CallMatcher MAP_OF = CallMatcher.staticCall(\"java.util.MAP\", \"of\");\n" +
                "    \n" +
                "    public void method(PsiMethodCallExpression expression) {\n" +
                "        if (<selection>MAP_OF.matches(expression) && LIST_OF.matches(expression) && SET_OF.matches(expression)</selection>) {\n" +
                "        }\n" +
                "    }\n" +
                "}");
        checkIfNotAvailableIn(psiFile);
    }

    public void testNotAvailableForPartialParenthesisedPolyadic() {
        PsiFile psiFile = myFixture.configureByText("NotAvailable.java",
            "import com.siyeh.ig.callMatcher.CallMatcher;\n" +
                "\n" +
                "public class NotAvailable {\n" +
                "    static final CallMatcher LIST_OF = CallMatcher.staticCall(\"java.util.List\", \"of\");\n" +
                "    static final CallMatcher SET_OF = CallMatcher.staticCall(\"java.util.Set\", \"of\");\n" +
                "    static final CallMatcher MAP_OF = CallMatcher.staticCall(\"java.util.MAP\", \"of\");\n" +
                "    \n" +
                "    public void method(PsiMethodCallExpression expression) {\n" +
                "        if (<selection>(MAP_OF.matches(expression) || LIST_OF.matches(expression)) && SET_OF.matches(expression)</selection>) {\n" +
                "        }\n" +
                "    }\n" +
                "}");
        checkIfNotAvailableIn(psiFile);
    }

    public void testNotAvailableForDifferentMatcherMethods() {
        PsiFile psiFile = myFixture.configureByText("NotAvailable.java",
            "import com.siyeh.ig.callMatcher.CallMatcher;\n" +
                "\n" +
                "public class NotAvailable {\n" +
                "    static final CallMatcher LIST_OF = CallMatcher.staticCall(\"java.util.List\", \"of\");\n" +
                "    static final CallMatcher SET_OF = CallMatcher.staticCall(\"java.util.Set\", \"of\");\n" +
                "    \n" +
                "    public void method(PsiMethodCallExpression expression) {\n" +
                "        if (<selection>LIST_OF.matches(expression) && SET_OF.uCallMatches(expression)</selection>) {\n" +
                "        }\n" +
                "    }\n" +
                "}");
        checkIfNotAvailableIn(psiFile);
    }

    public void testNotAvailableForDifferentTestedExpressions() {
        PsiFile psiFile = myFixture.configureByText("NotAvailable.java",
            "import com.siyeh.ig.callMatcher.CallMatcher;\n" +
                "\n" +
                "public class NotAvailable {\n" +
                "    static final CallMatcher LIST_OF = CallMatcher.staticCall(\"java.util.List\", \"of\");\n" +
                "    static final CallMatcher SET_OF = CallMatcher.staticCall(\"java.util.Set\", \"of\");\n" +
                "    \n" +
                "    public void method(PsiMethodCallExpression expression, PsiMethodCallExpression expression2) {\n" +
                "        if (<selection>LIST_OF.matches(expression) && SET_OF.matches(expression2)</selection>) {\n" +
                "        }\n" +
                "    }\n" +
                "}");
        checkIfNotAvailableIn(psiFile);
    }
}
