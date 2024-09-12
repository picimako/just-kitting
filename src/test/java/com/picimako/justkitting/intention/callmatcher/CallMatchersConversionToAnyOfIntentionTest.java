//Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.intention.callmatcher;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.psi.PsiFile;

import com.picimako.justkitting.ThirdPartyLibraryLoader;
import com.picimako.justkitting.intention.JustKittingIntentionTestBase;

/**
 * Integration test for {@link CallMatchersConversionToAnyOfIntention}.
 */
public class CallMatchersConversionToAnyOfIntentionTest extends JustKittingIntentionTestBase {

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
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class BinaryExpression {
                    static final CallMatcher LIST_OF = CallMatcher.staticCall("java.util.List", "of");
                    static final CallMatcher SET_OF = CallMatcher.staticCall("java.util.Set", "of");
                   \s
                    public void method(PsiMethodCallExpression expression) {
                        if (<selection>LIST_OF.matches(expression) || SET_OF.matches(expression)</selection>) {
                        }
                    }
                }""",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class BinaryExpression {
                    static final CallMatcher LIST_OF = CallMatcher.staticCall("java.util.List", "of");
                    static final CallMatcher SET_OF = CallMatcher.staticCall("java.util.Set", "of");
                    private static final CallMatcher CALL_MATCHER = CallMatcher.anyOf(LIST_OF, SET_OF);

                    public void method(PsiMethodCallExpression expression) {
                        if (CALL_MATCHER.matches(expression)) {
                        }
                    }
                }""");
    }

    public void testConversionOfParenthesisedBinaryExpression() {
        doIntentionTest("BinaryParenthesised.java",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class BinaryParenthesised {
                    static final CallMatcher LIST_OF = CallMatcher.staticCall("java.util.List", "of");
                    static final CallMatcher SET_OF = CallMatcher.staticCall("java.util.Set", "of");
                   \s
                    public void method(PsiMethodCallExpression expression) {
                        if (<selection>(LIST_OF.matches(expression) || SET_OF.matches(expression))</selection>) {
                        }
                    }
                }""",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class BinaryParenthesised {
                    static final CallMatcher LIST_OF = CallMatcher.staticCall("java.util.List", "of");
                    static final CallMatcher SET_OF = CallMatcher.staticCall("java.util.Set", "of");
                    private static final CallMatcher CALL_MATCHER = CallMatcher.anyOf(LIST_OF, SET_OF);

                    public void method(PsiMethodCallExpression expression) {
                        if ((CALL_MATCHER.matches(expression))) {
                        }
                    }
                }""");
    }

    public void testConversionOfPolyadicExpression() {
        doIntentionTest("Polyadic.java",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class Polyadic {
                    static final CallMatcher LIST_OF = CallMatcher.staticCall("java.util.List", "of");
                    static final CallMatcher SET_OF = CallMatcher.staticCall("java.util.Set", "of");
                    static final CallMatcher MAP_OF = CallMatcher.staticCall("java.util.Map", "of");
                   \s
                    public void method(PsiMethodCallExpression expression) {
                        if (<selection>LIST_OF.matches(expression) || SET_OF.matches(expression) || MAP_OF.matches(expression)</selection>) {
                        }
                    }
                }""",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class Polyadic {
                    static final CallMatcher LIST_OF = CallMatcher.staticCall("java.util.List", "of");
                    static final CallMatcher SET_OF = CallMatcher.staticCall("java.util.Set", "of");
                    static final CallMatcher MAP_OF = CallMatcher.staticCall("java.util.Map", "of");
                    private static final CallMatcher CALL_MATCHER = CallMatcher.anyOf(LIST_OF, SET_OF, MAP_OF);

                    public void method(PsiMethodCallExpression expression) {
                        if (CALL_MATCHER.matches(expression)) {
                        }
                    }
                }""");
    }

    public void testConversionOfParenthesisedPolyadicExpression() {
        doIntentionTest("PolyadicParenthesised.java",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class PolyadicParenthesised {
                    static final CallMatcher LIST_OF = CallMatcher.staticCall("java.util.List", "of");
                    static final CallMatcher SET_OF = CallMatcher.staticCall("java.util.Set", "of");
                    static final CallMatcher MAP_OF = CallMatcher.staticCall("java.util.Map", "of");
                   \s
                    public void method(PsiMethodCallExpression expression) {
                        if (<selection>(LIST_OF.matches(expression) || SET_OF.matches(expression) || MAP_OF.matches(expression))</selection>) {
                        }
                    }
                }""",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class PolyadicParenthesised {
                    static final CallMatcher LIST_OF = CallMatcher.staticCall("java.util.List", "of");
                    static final CallMatcher SET_OF = CallMatcher.staticCall("java.util.Set", "of");
                    static final CallMatcher MAP_OF = CallMatcher.staticCall("java.util.Map", "of");
                    private static final CallMatcher CALL_MATCHER = CallMatcher.anyOf(LIST_OF, SET_OF, MAP_OF);

                    public void method(PsiMethodCallExpression expression) {
                        if ((CALL_MATCHER.matches(expression))) {
                        }
                    }
                }""");
    }

    public void testConversionWithTestMethod() {
        doIntentionTest("Test.java",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class Test {
                    static final CallMatcher LIST_OF = CallMatcher.staticCall("java.util.List", "of");
                    static final CallMatcher SET_OF = CallMatcher.staticCall("java.util.Set", "of");
                   \s
                    public void method(PsiMethodCallExpression expression) {
                        if (<selection>LIST_OF.test(expression) || SET_OF.test(expression)</selection>) {
                        }
                    }
                }""",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class Test {
                    static final CallMatcher LIST_OF = CallMatcher.staticCall("java.util.List", "of");
                    static final CallMatcher SET_OF = CallMatcher.staticCall("java.util.Set", "of");
                    private static final CallMatcher CALL_MATCHER = CallMatcher.anyOf(LIST_OF, SET_OF);

                    public void method(PsiMethodCallExpression expression) {
                        if (CALL_MATCHER.test(expression)) {
                        }
                    }
                }""");
    }

    public void testConversionWithMethodMatchesMethod() {
        doIntentionTest("Method.java",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class Method {
                    static final CallMatcher LIST_OF = CallMatcher.staticCall("java.util.List", "of");
                    static final CallMatcher SET_OF = CallMatcher.staticCall("java.util.Set", "of");
                   \s
                    public void method(PsiMethodCallExpression expression) {
                        if (<selection>LIST_OF.methodMatches(expression) || SET_OF.methodMatches(expression)</selection>) {
                        }
                    }
                }""",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class Method {
                    static final CallMatcher LIST_OF = CallMatcher.staticCall("java.util.List", "of");
                    static final CallMatcher SET_OF = CallMatcher.staticCall("java.util.Set", "of");
                    private static final CallMatcher CALL_MATCHER = CallMatcher.anyOf(LIST_OF, SET_OF);

                    public void method(PsiMethodCallExpression expression) {
                        if (CALL_MATCHER.methodMatches(expression)) {
                        }
                    }
                }""");
    }

    public void testConversionWithMethodReferenceMatchesMethod() {
        doIntentionTest("MethodReference.java",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class MethodReference {
                    static final CallMatcher LIST_OF = CallMatcher.staticCall("java.util.List", "of");
                    static final CallMatcher SET_OF = CallMatcher.staticCall("java.util.Set", "of");
                   \s
                    public void method(PsiMethodCallExpression expression) {
                        if (<selection>LIST_OF.methodReferenceMatches(expression) || SET_OF.methodReferenceMatches(expression)</selection>) {
                        }
                    }
                }""",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class MethodReference {
                    static final CallMatcher LIST_OF = CallMatcher.staticCall("java.util.List", "of");
                    static final CallMatcher SET_OF = CallMatcher.staticCall("java.util.Set", "of");
                    private static final CallMatcher CALL_MATCHER = CallMatcher.anyOf(LIST_OF, SET_OF);

                    public void method(PsiMethodCallExpression expression) {
                        if (CALL_MATCHER.methodReferenceMatches(expression)) {
                        }
                    }
                }""");
    }

    public void testConversionWithUCallMatchesMethod() {
        doIntentionTest("UCall.java",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class UCall {
                    static final CallMatcher LIST_OF = CallMatcher.staticCall("java.util.List", "of");
                    static final CallMatcher SET_OF = CallMatcher.staticCall("java.util.Set", "of");
                   \s
                    public void method(PsiMethodCallExpression expression) {
                        if (<selection>LIST_OF.uCallMatches(expression) || SET_OF.uCallMatches(expression)</selection>) {
                        }
                    }
                }""",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class UCall {
                    static final CallMatcher LIST_OF = CallMatcher.staticCall("java.util.List", "of");
                    static final CallMatcher SET_OF = CallMatcher.staticCall("java.util.Set", "of");
                    private static final CallMatcher CALL_MATCHER = CallMatcher.anyOf(LIST_OF, SET_OF);

                    public void method(PsiMethodCallExpression expression) {
                        if (CALL_MATCHER.uCallMatches(expression)) {
                        }
                    }
                }""");
    }

    public void testConversionInVariableAssignment() {
        doIntentionTest("VariableAssignment.java",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class VariableAssignment {
                    static final CallMatcher LIST_OF = CallMatcher.staticCall("java.util.List", "of");
                    static final CallMatcher SET_OF = CallMatcher.staticCall("java.util.Set", "of");
                   \s
                    public void method(PsiMethodCallExpression expression) {
                        var isCollection = <selection>LIST_OF.matches(expression) || SET_OF.matches(expression)</selection>;
                    }
                }""",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class VariableAssignment {
                    static final CallMatcher LIST_OF = CallMatcher.staticCall("java.util.List", "of");
                    static final CallMatcher SET_OF = CallMatcher.staticCall("java.util.Set", "of");
                    private static final CallMatcher CALL_MATCHER = CallMatcher.anyOf(LIST_OF, SET_OF);

                    public void method(PsiMethodCallExpression expression) {
                        var isCollection = CALL_MATCHER.matches(expression);
                    }
                }""");
    }

    //No availability cases

    public void testNotAvailableForNonOrBinaryExpression() {
        PsiFile psiFile = myFixture.configureByText("NotAvailable.java",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class NotAvailable {
                    static final CallMatcher LIST_OF = CallMatcher.staticCall("java.util.List", "of");
                    static final CallMatcher SET_OF = CallMatcher.staticCall("java.util.Set", "of");
                   \s
                    public void method(PsiMethodCallExpression expression) {
                        if (<selection>LIST_OF.matches(expression) && SET_OF.matches(expression)</selection>) {
                        }
                    }
                }""");
        checkIfNotAvailableIn(psiFile);
    }

    public void testNotAvailableForNonOrPolyadicExpression() {
        PsiFile psiFile = myFixture.configureByText("NotAvailable.java",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class NotAvailable {
                    static final CallMatcher LIST_OF = CallMatcher.staticCall("java.util.List", "of");
                    static final CallMatcher SET_OF = CallMatcher.staticCall("java.util.Set", "of");
                    static final CallMatcher MAP_OF = CallMatcher.staticCall("java.util.MAP", "of");
                   \s
                    public void method(PsiMethodCallExpression expression) {
                        if (<selection>MAP_OF.matches(expression) && LIST_OF.matches(expression) && SET_OF.matches(expression)</selection>) {
                        }
                    }
                }""");
        checkIfNotAvailableIn(psiFile);
    }

    public void testNotAvailableForPartialParenthesisedPolyadic() {
        PsiFile psiFile = myFixture.configureByText("NotAvailable.java",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class NotAvailable {
                    static final CallMatcher LIST_OF = CallMatcher.staticCall("java.util.List", "of");
                    static final CallMatcher SET_OF = CallMatcher.staticCall("java.util.Set", "of");
                    static final CallMatcher MAP_OF = CallMatcher.staticCall("java.util.MAP", "of");
                   \s
                    public void method(PsiMethodCallExpression expression) {
                        if (<selection>(MAP_OF.matches(expression) || LIST_OF.matches(expression)) && SET_OF.matches(expression)</selection>) {
                        }
                    }
                }""");
        checkIfNotAvailableIn(psiFile);
    }

    public void testNotAvailableForDifferentMatcherMethods() {
        PsiFile psiFile = myFixture.configureByText("NotAvailable.java",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class NotAvailable {
                    static final CallMatcher LIST_OF = CallMatcher.staticCall("java.util.List", "of");
                    static final CallMatcher SET_OF = CallMatcher.staticCall("java.util.Set", "of");
                   \s
                    public void method(PsiMethodCallExpression expression) {
                        if (<selection>LIST_OF.matches(expression) && SET_OF.uCallMatches(expression)</selection>) {
                        }
                    }
                }""");
        checkIfNotAvailableIn(psiFile);
    }

    public void testNotAvailableForDifferentTestedExpressions() {
        PsiFile psiFile = myFixture.configureByText("NotAvailable.java",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class NotAvailable {
                    static final CallMatcher LIST_OF = CallMatcher.staticCall("java.util.List", "of");
                    static final CallMatcher SET_OF = CallMatcher.staticCall("java.util.Set", "of");
                   \s
                    public void method(PsiMethodCallExpression expression, PsiMethodCallExpression expression2) {
                        if (<selection>LIST_OF.matches(expression) && SET_OF.matches(expression2)</selection>) {
                        }
                    }
                }""");
        checkIfNotAvailableIn(psiFile);
    }
}
