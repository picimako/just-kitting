//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.inspection;

import static com.intellij.psi.JavaPsiFacade.getElementFactory;
import static com.picimako.justkitting.PlatformNames.PSI_CALL;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.JavaTokenType;
import com.intellij.psi.PsiBinaryExpression;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiNewExpression;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.util.ConstantEvaluationOverflowException;
import com.intellij.psi.util.PsiTreeUtil;
import com.picimako.justkitting.PlatformNames;
import com.picimako.justkitting.resources.JustKittingBundle;
import com.siyeh.ig.callMatcher.CallMatcher;
import com.siyeh.ig.psiutils.TypeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * Reports code snippets that can be optimized in some way.
 * <p>
 * The following problems are reported:
 * <ul>
 *     <li>{@code new <TYPE>[0]} expressions can be replaced with {@code <TYPE>.EMPTY_ARRAY}, if that constant is available in a respective type.</li>
 *     <li>{@code PsiCall.getArgumentList().getExpressions().length} empty/non-empty comparisons can be replaced with {@code !isEmpty()}.</li>
 * </ul>
 *
 * @since 0.1.0
 */
public class OptimizeExpressionsInspection extends LocalInspectionTool {
    private static final CallMatcher.Simple GET_ARGUMENT_LIST = CallMatcher.instanceCall(PSI_CALL, "getArgumentList");

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
        return new JavaElementVisitor() {
            @Override
            public void visitNewExpression(@NotNull PsiNewExpression expression) {
                //If it's a new Type[0] array creation and Type has an empty array constant called EMPTY_ARRAY
                if (expression.isArrayCreation()
                    && expression.getArrayDimensions().length == 1
                    && isZero(expression.getArrayDimensions()[0])
                    && hasEmptyArrayConstantField(expression.getClassReference())) {
                    holder.registerProblem(expression,
                        JustKittingBundle.message("inspection.empty.array.creation", expression.getClassReference().getReferenceName()),
                        ProblemHighlightType.WEAK_WARNING,
                        new ReplaceWithEmptyArrayConstantQuickFix(expression.getClassReference().getReferenceName()));
                }
            }

            @Override
            public void visitBinaryExpression(@NotNull PsiBinaryExpression expr) {
                boolean isLeftBound;
                //If any of the expression operands is a reference to 'PsiArgumentList.getExpressions().length'
                if ((isLeftBound = isGetExpressionsLength(expr.getLOperand(), expr.getROperand())) || isGetExpressionsLength(expr.getROperand(), expr.getLOperand())) {
                    if (expr.getOperationTokenType() == JavaTokenType.EQEQ) {
                        holder.registerProblem(expr, JustKittingBundle.message("inspection.use.expression.list.is.empty", ""), new ReplaceWithIsEmptyQuickFix(false, isLeftBound));
                    } else if (expr.getOperationTokenType() == (isLeftBound ? JavaTokenType.GT : JavaTokenType.LT)) {
                        holder.registerProblem(expr, JustKittingBundle.message("inspection.use.expression.list.is.empty", "!"), new ReplaceWithIsEmptyQuickFix(true, isLeftBound));
                    }
                }
            }

            /**
             * Returns whether the operands represent a comparison between a call to PsiExpressionList.getExpressions().length and 0.
             */
            private boolean isGetExpressionsLength(PsiExpression operand1, PsiExpression operand2) {
                return operand1 instanceof PsiReferenceExpression operand1Ref
                    && "length".equals(operand1Ref.getReferenceName())
                    && getGetArgumentList(operand1).isPresent()
                    && isZero(operand2);
            }
        };
    }

    // ---- Empty array ----

    /**
     * Returns whether the class/interface referenced by the argument element has a field called {@code EMPTY_ARRAY}.
     * <p>
     * The type of the field is not check at the moment.
     */
    private static boolean hasEmptyArrayConstantField(@Nullable PsiJavaCodeReferenceElement element) {
        return Optional.ofNullable(element)
            .map(PsiReference::resolve)
            .filter(PsiClass.class::isInstance)
            .map(c -> ((PsiClass) c).findFieldByName("EMPTY_ARRAY", false))
            .isPresent();
    }

    /**
     * Replaces {@code new <TYPE>[0]} expressions with {@code <TYPE>.EMPTY_ARRAY}.</li>
     */
    private record ReplaceWithEmptyArrayConstantQuickFix(String arrayType) implements LocalQuickFix {

        @Override
        public void applyFix(@NotNull Project project, ProblemDescriptor descriptor) {
            var newEmptyArray = (PsiNewExpression) descriptor.getPsiElement();
            newEmptyArray.replace(getElementFactory(project)
                .createExpressionFromText(newEmptyArray.getClassReference().getReferenceName() + ".EMPTY_ARRAY", descriptor.getPsiElement()));
        }

        @Override
        public @IntentionName @NotNull String getName() {
            return JustKittingBundle.message("inspection.replace.with.empty.array.constant", arrayType);
        }

        @Override
        public @IntentionFamilyName @NotNull String getFamilyName() {
            return JustKittingBundle.message("inspection.optimize.expressions.family");
        }
    }

    // ---- isEmpty() comparison ----

    /**
     * Returns the method call expression of {@code getArgumentList()} under the provided parent (upwards in the method call chain) if it is present.
     */
    private static Optional<PsiExpression> getGetArgumentList(PsiExpression parent) {
        var getArgumentList = new Ref<PsiExpression>();
        PsiTreeUtil.processElements(parent, element -> {
            if ((element instanceof PsiMethodCallExpression methodCall && GET_ARGUMENT_LIST.matches(methodCall))
                || (element instanceof PsiReferenceExpression reference && TypeUtils.typeEquals(PlatformNames.PSI_EXPRESSION_LIST, (reference).getType()))) {
                getArgumentList.set((PsiExpression) element);
                return false;
            }
            return true;
        });
        return Optional.ofNullable(getArgumentList.get());
    }

    /**
     * Returns whether the argument expression evaluates to 0.
     */
    private static boolean isZero(@Nullable PsiExpression expression) {
        if (expression != null) {
            try {
                var constantEvaluationHelper = JavaPsiFacade.getInstance(expression.getProject()).getConstantEvaluationHelper();
                return Objects.equals(constantEvaluationHelper.computeConstantExpression(expression, true), 0);
            } catch (ConstantEvaluationOverflowException e) {
                //Fall through. Will return false.
            }
        }
        return false;
    }

    /**
     * Replaces {@code PsiCall.getArgumentList().getExpressions().length} empty/non-empty comparisons
     * with {@code isEmpty()} or {@code !isEmpty()} depending on the expression.
     */
    private static final class ReplaceWithIsEmptyQuickFix implements LocalQuickFix {
        private final String negate;
        private final boolean isExpressionAtLeft;

        public ReplaceWithIsEmptyQuickFix(boolean negate, boolean isExpressionAtLeft) {
            this.negate = negate ? "!" : "";
            this.isExpressionAtLeft = isExpressionAtLeft;
        }

        @Override
        public void applyFix(@NotNull Project project, ProblemDescriptor descriptor) {
            var binaryExpression = (PsiBinaryExpression) descriptor.getPsiElement();
            getGetArgumentList(isExpressionAtLeft ? binaryExpression.getLOperand() : binaryExpression.getROperand())
                .ifPresent(getArgumentList -> {
                    var isEmpty = getElementFactory(project).createExpressionFromText(negate + getArgumentList.getText() + ".isEmpty()", binaryExpression);
                    binaryExpression.replace(isEmpty);
                });
        }

        @Override
        public @IntentionName @NotNull String getName() {
            return JustKittingBundle.message("inspection.replace.with.is.empty", negate);
        }

        @Override
        public @IntentionFamilyName @NotNull String getFamilyName() {
            return JustKittingBundle.message("inspection.optimize.expressions.family");
        }
    }
}
