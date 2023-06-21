// Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.inspection;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.picimako.justkitting.ThirdPartyLibraryLoader;

/**
 * Functional test for {@link OptimizeExpressionsInspection}.
 */
public class OptimizeExpressionsInspectionTest extends JustKittingInspectionTestBase {

    @Override
    protected InspectionProfileEntry getInspection() {
        return new OptimizeExpressionsInspection();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ThirdPartyLibraryLoader.loadJavaApi(myFixture);
    }

    //Empty array creation

    public void testReplaceEmptyArrayCreationWithConstant() {
        doQuickFixTest("Replace with PsiElement.EMPTY_ARRAY", "EmptyArray.java",
            """
                import com.intellij.psi.PsiElement;

                class EmptyArray {
                   PsiElement[] array = new PsiEl<caret>ement[0];
                }""",
            """
                import com.intellij.psi.PsiElement;

                class EmptyArray {
                   PsiElement[] array = PsiElement.EMPTY_ARRAY;
                }""");
    }

    public void testNonEmptyArrayCreationIsNotReported() {
        doJavaTest("EmptyArray.java",
            """
                import com.intellij.psi.PsiElement;

                class EmptyArray {
                \tPsiElement[] array = new PsiElement[1];
                }""");
    }

    //isEmpty with getExpressions().length comparison

    public void testReplaceExpressionsLengthEqualsZeroWithIsEmpty() {
        doQuickFixTest("Replace with isEmpty()", "IsEmpty.java",
            """
                import com.intellij.psi.PsiMethodCallExpression;

                class IsEmpty {
                   void method(PsiMethodCallExpression expression) {
                       boolean b = expression.getArgumentList().getExpressions().length ==<caret> 0;
                   }
                }""",
            """
                import com.intellij.psi.PsiMethodCallExpression;

                class IsEmpty {
                   void method(PsiMethodCallExpression expression) {
                       boolean b = expression.getArgumentList().isEmpty();
                   }
                }""");
    }

    public void testReplaceExpressionsLengthGreaterThanZeroWithNotIsEmpty() {
        doQuickFixTest("Replace with !isEmpty()", "IsEmpty.java",
            """
                import com.intellij.psi.PsiMethodCallExpression;

                class IsEmpty {
                   void method(PsiMethodCallExpression expression) {
                       boolean b = expression.getArgumentList().getExpressi<caret>ons().length > 0;
                   }
                }""",
            """
                import com.intellij.psi.PsiMethodCallExpression;

                class IsEmpty {
                   void method(PsiMethodCallExpression expression) {
                       boolean b = !expression.getArgumentList().isEmpty();
                   }
                }""");
    }

    public void testReplaceZeroEqualsExpressionsLengthWithIsEmpty() {
        doQuickFixTest("Replace with isEmpty()", "IsEmpty.java",
            """
                import com.intellij.psi.PsiMethodCallExpression;

                class IsEmpty {
                   void method(PsiMethodCallExpression expression) {
                       boolean b = 0 == expression.getArgumentList().getExpressio<caret>ns().length;
                   }
                }""",
            """
                import com.intellij.psi.PsiMethodCallExpression;

                class IsEmpty {
                   void method(PsiMethodCallExpression expression) {
                       boolean b = expression.getArgumentList().isEmpty();
                   }
                }""");
    }

    public void testReplaceZeroGreaterThanExpressionsLengthWithNotIsEmpty() {
        doQuickFixTest("Replace with !isEmpty()", "IsEmpty.java",
            """
                import com.intellij.psi.PsiMethodCallExpression;

                class IsEmpty {
                   void method(PsiMethodCallExpression expression) {
                       boolean b = 0 < expression.getArgumentList().getExpressi<caret>ons().length;
                   }
                }""",
            """
                import com.intellij.psi.PsiMethodCallExpression;

                class IsEmpty {
                   void method(PsiMethodCallExpression expression) {
                       boolean b = !expression.getArgumentList().isEmpty();
                   }
                }""");
    }

    public void testReplaceZeroEqualsExpressionsLengthWithIsEmptyInIf() {
        doQuickFixTest("Replace with isEmpty()", "IsEmpty.java",
            """
                import com.intellij.psi.PsiMethodCallExpression;

                class IsEmpty {
                   void method(PsiMethodCallExpression expression) {
                      if (0 == expression.getArgumentList().getExpressio<caret>ns().length) {
                      }
                   }
                }""",
            """
                import com.intellij.psi.PsiMethodCallExpression;

                class IsEmpty {
                   void method(PsiMethodCallExpression expression) {
                      if (expression.getArgumentList().isEmpty()) {
                      }
                   }
                }""");
    }

    public void testReplaceZeroEqualsExpressionsLengthWithIsEmptyInExtractedArgumentListVariable() {
        doQuickFixTest("Replace with isEmpty()", "IsEmpty.java",
            """
                import com.intellij.psi.PsiMethodCallExpression;

                class IsEmpty {
                    void method(PsiMethodCallExpression expression) {
                       var argumentList = expression.getArgumentList();
                       if (0 == argumentList.getExpressio<caret>ns().length) {
                       }
                    }
                }""",
            """
                import com.intellij.psi.PsiMethodCallExpression;

                class IsEmpty {
                    void method(PsiMethodCallExpression expression) {
                       var argumentList = expression.getArgumentList();
                       if (argumentList.isEmpty()) {
                       }
                    }
                }""");
    }

    public void testZeroEqualsExpressionsLengthWithIsEmptyInExtractedVariableIsNotReported() {
        doJavaTest("IsEmpty.java",
            """
                import com.intellij.psi.PsiMethodCallExpression;

                class IsEmpty {
                    void method(PsiMethodCallExpression expression) {
                       var expressions = expression.getArgumentList().getExpressions();
                       if (0 == expressions.length) {
                       }
                    }
                }""");
    }

    public void testExpressionsLengthEqualsNonZeroIsNotReported() {
        doJavaTest("IsEmpty.java",
            """
                import com.intellij.psi.PsiMethodCallExpression;

                class IsEmpty {
                   void method(PsiMethodCallExpression expression) {
                       boolean b = expression.getArgumentList().getExpressions().length == 1;
                   }
                }""");
    }

    public void testExpressionsLengthGreaterThanNonZeroIsNotReported() {
        doJavaTest("IsEmpty.java",
            """
                import com.intellij.psi.PsiMethodCallExpression;

                class IsEmpty {
                   void method(PsiMethodCallExpression expression) {
                       boolean b = expression.getArgumentList().getExpressions().length > 1;
                   }
                }""");
    }
}
