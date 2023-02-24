// Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.devkitplus.inspection;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.picimako.devkitplus.ThirdPartyLibraryLoader;

/**
 * Functional test for {@link OptimizeExpressionsInspection}.
 */
public class OptimizeExpressionsInspectionTest extends DevKitPlusInspectionTestBase {

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
            "import com.intellij.psi.PsiElement;\n" +
                "\n" +
                "class EmptyArray {\n" +
                "   PsiElement[] array = new PsiEl<caret>ement[0];\n" +
                "}",
            "import com.intellij.psi.PsiElement;\n" +
                "\n" +
                "class EmptyArray {\n" +
                "   PsiElement[] array = PsiElement.EMPTY_ARRAY;\n" +
                "}");
    }

    public void testNonEmptyArrayCreationIsNotReported() {
        doJavaTest("EmptyArray.java",
            "import com.intellij.psi.PsiElement;\n" +
                "\n" +
                "class EmptyArray {\n" +
                "\tPsiElement[] array = new PsiElement[1];\n" +
                "}");
    }

    //isEmpty with getExpressions().length comparison

    public void testReplaceExpressionsLengthEqualsZeroWithIsEmpty() {
        doQuickFixTest("Replace with isEmpty()", "IsEmpty.java",
            "import com.intellij.psi.PsiMethodCallExpression;\n" +
                "\n" +
                "class IsEmpty {\n" +
                "   void method(PsiMethodCallExpression expression) {\n" +
                "       boolean b = expression.getArgumentList().getExpressions().length ==<caret> 0;\n" +
                "   }\n" +
                "}",
            "import com.intellij.psi.PsiMethodCallExpression;\n" +
                "\n" +
                "class IsEmpty {\n" +
                "   void method(PsiMethodCallExpression expression) {\n" +
                "       boolean b = expression.getArgumentList().isEmpty();\n" +
                "   }\n" +
                "}");
    }

    public void testReplaceExpressionsLengthGreaterThanZeroWithNotIsEmpty() {
        doQuickFixTest("Replace with !isEmpty()", "IsEmpty.java",
            "import com.intellij.psi.PsiMethodCallExpression;\n" +
                "\n" +
                "class IsEmpty {\n" +
                "   void method(PsiMethodCallExpression expression) {\n" +
                "       boolean b = expression.getArgumentList().getExpressi<caret>ons().length > 0;\n" +
                "   }\n" +
                "}",
            "import com.intellij.psi.PsiMethodCallExpression;\n" +
                "\n" +
                "class IsEmpty {\n" +
                "   void method(PsiMethodCallExpression expression) {\n" +
                "       boolean b = !expression.getArgumentList().isEmpty();\n" +
                "   }\n" +
                "}");
    }

    public void testReplaceZeroEqualsExpressionsLengthWithIsEmpty() {
        doQuickFixTest("Replace with isEmpty()", "IsEmpty.java",
            "import com.intellij.psi.PsiMethodCallExpression;\n" +
                "\n" +
                "class IsEmpty {\n" +
                "   void method(PsiMethodCallExpression expression) {\n" +
                "       boolean b = 0 == expression.getArgumentList().getExpressio<caret>ns().length;\n" +
                "   }\n" +
                "}",
            "import com.intellij.psi.PsiMethodCallExpression;\n" +
                "\n" +
                "class IsEmpty {\n" +
                "   void method(PsiMethodCallExpression expression) {\n" +
                "       boolean b = expression.getArgumentList().isEmpty();\n" +
                "   }\n" +
                "}");
    }

    public void testReplaceZeroGreaterThanExpressionsLengthWithNotIsEmpty() {
        doQuickFixTest("Replace with !isEmpty()", "IsEmpty.java",
            "import com.intellij.psi.PsiMethodCallExpression;\n" +
                "\n" +
                "class IsEmpty {\n" +
                "   void method(PsiMethodCallExpression expression) {\n" +
                "       boolean b = 0 < expression.getArgumentList().getExpressi<caret>ons().length;\n" +
                "   }\n" +
                "}",
            "import com.intellij.psi.PsiMethodCallExpression;\n" +
                "\n" +
                "class IsEmpty {\n" +
                "   void method(PsiMethodCallExpression expression) {\n" +
                "       boolean b = !expression.getArgumentList().isEmpty();\n" +
                "   }\n" +
                "}");
    }

    public void testReplaceZeroEqualsExpressionsLengthWithIsEmptyInIf() {
        doQuickFixTest("Replace with isEmpty()", "IsEmpty.java",
            "import com.intellij.psi.PsiMethodCallExpression;\n" +
                "\n" +
                "class IsEmpty {\n" +
                "   void method(PsiMethodCallExpression expression) {\n" +
                "      if (0 == expression.getArgumentList().getExpressio<caret>ns().length) {\n" +
                "      }\n" +
                "   }\n" +
                "}",
            "import com.intellij.psi.PsiMethodCallExpression;\n" +
                "\n" +
                "class IsEmpty {\n" +
                "   void method(PsiMethodCallExpression expression) {\n" +
                "      if (expression.getArgumentList().isEmpty()) {\n" +
                "      }\n" +
                "   }\n" +
                "}");
    }

    public void testReplaceZeroEqualsExpressionsLengthWithIsEmptyInExtractedArgumentListVariable() {
        doQuickFixTest("Replace with isEmpty()", "IsEmpty.java",
            "import com.intellij.psi.PsiMethodCallExpression;\n" +
                "\n" +
                "class IsEmpty {\n" +
                "    void method(PsiMethodCallExpression expression) {\n" +
                "       var argumentList = expression.getArgumentList();\n" +
                "       if (0 == argumentList.getExpressio<caret>ns().length) {\n" +
                "       }\n" +
                "    }\n" +
                "}",
            "import com.intellij.psi.PsiMethodCallExpression;\n" +
                "\n" +
                "class IsEmpty {\n" +
                "    void method(PsiMethodCallExpression expression) {\n" +
                "       var argumentList = expression.getArgumentList();\n" +
                "       if (argumentList.isEmpty()) {\n" +
                "       }\n" +
                "    }\n" +
                "}");
    }

    public void testZeroEqualsExpressionsLengthWithIsEmptyInExtractedVariableIsNotReported() {
        doJavaTest("IsEmpty.java",
            "import com.intellij.psi.PsiMethodCallExpression;\n" +
                "\n" +
                "class IsEmpty {\n" +
                "    void method(PsiMethodCallExpression expression) {\n" +
                "       var expressions = expression.getArgumentList().getExpressions();\n" +
                "       if (0 == expressions.length) {\n" +
                "       }\n" +
                "    }\n" +
                "}");
    }

    public void testExpressionsLengthEqualsNonZeroIsNotReported() {
        doJavaTest("IsEmpty.java",
            "import com.intellij.psi.PsiMethodCallExpression;\n" +
                "\n" +
                "class IsEmpty {\n" +
                "   void method(PsiMethodCallExpression expression) {\n" +
                "       boolean b = expression.getArgumentList().getExpressions().length == 1;\n" +
                "   }\n" +
                "}");
    }

    public void testExpressionsLengthGreaterThanNonZeroIsNotReported() {
        doJavaTest("IsEmpty.java",
            "import com.intellij.psi.PsiMethodCallExpression;\n" +
                "\n" +
                "class IsEmpty {\n" +
                "   void method(PsiMethodCallExpression expression) {\n" +
                "       boolean b = expression.getArgumentList().getExpressions().length > 1;\n" +
                "   }\n" +
                "}");
    }
}
