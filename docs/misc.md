# Miscellaneous

## Expressions can be optimized

![](https://img.shields.io/badge/inspection-orange) ![](https://img.shields.io/badge/since-0.1.0-blue) [![](https://img.shields.io/badge/implementation-OptimizeExpressionsInspection-blue)](../src/main/java/com/picimako/devkitplus/inspection/OptimizeExpressionsInspection.java)

There are various convenience constants, methods, etc. with which one can optimize and/or simplify code.

This inspection groups them together and provides quick fixes when possible to replace code snippets to their more optimal form.

**EMPTY_ARRAY constants**

```java
//From:
PsiElement[] array = new PsiElement[0];
//To:
PsiElement[] array = PsiElement.EMPTY_ARRAY; //it is any type that has this EMPTY_ARRAY constant defined
```

**PsiExpressionList.getExpressions().length comparison**

```java
//From (the operands are recognized if they are switched too):
psiMethodCallExpression.getArgumentList().getExpressions().length == 0;
psiMethodCallExpression.getArgumentList().getExpressions().length > 0;
//To:
psiMethodCallExpression.getArgumentList().isEmpty();
!psiMethodCallExpression.getArgumentList().isEmpty();
```
