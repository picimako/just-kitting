//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.intention.callmatcher;

import static com.picimako.justkitting.PlatformNames.CALL_MATCHER;
import static com.siyeh.ig.callMatcher.CallMatcher.instanceCall;
import static java.util.stream.Collectors.joining;

import com.intellij.codeInsight.CodeInsightUtil;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.ide.util.PsiClassListCellRenderer;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.JavaTokenType;
import com.intellij.psi.PsiBinaryExpression;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiPolyadicExpression;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.refactoring.introduceField.ElementToWorkOn;
import com.intellij.refactoring.introduceField.LocalToFieldHandler;
import com.intellij.refactoring.rename.inplace.MemberInplaceRenamer;
import com.intellij.util.Consumer;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.SmartList;
import com.picimako.justkitting.resources.JustKittingBundle;
import com.siyeh.ig.callMatcher.CallMatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This intention combines multiple CallMatcher.&lt;matches> calls to a single CallMatcher.anyOf() constant.
 * <p>
 * It is available only when a boolean expression with 2 or more operands is selected and they are all separated by OR operations.
 * <pre>
 * if (LIST_OF.matches(expression) || MAP_OF.matches(expression) || SET_OF.matches(expression)) { }
 * </pre>
 * All matcher methods of {@link CallMatcher} are recognized (matches, test, methodMatches, methodReferenceMatches, uCallMatches).
 * <p>
 * If the boolean expression is inside parenthesis, it is still recognized and available for conversion.
 * <p>
 * In case the selected expression has multiple parent classes, it lets the user choose which one to introduce the constant in.
 * <p>
 * TODO: if any of the embedded matcher constants is not used anywhere else, it could be removed and its initializer used in anyOf()
 *
 * @since 0.1.0
 */
public class CallMatchersConversionToAnyOfIntention implements IntentionAction {
    /**
     * Ordered descending based on usage statistics in intellij-community, except test, because it is overridden from java.util.Predicate.
     */
    private static final List<CallMatcher> MATCHES_MATCHERS = List.of(
        instanceCall(CALL_MATCHER, "matches").parameterCount(1),
        instanceCall(CALL_MATCHER, "methodMatches").parameterCount(1),
        instanceCall(CALL_MATCHER, "methodReferenceMatches").parameterCount(1),
        instanceCall(CALL_MATCHER, "uCallMatches").parameterCount(1),
        instanceCall(CALL_MATCHER, "test").parameterCount(1));

    @Override
    public @IntentionName @NotNull String getText() {
        return JustKittingBundle.message("intention.call.matcher.combine.to.any.of.name");
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return JustKittingBundle.message("intention.call.matcher.family.name");
    }

    //---- Availability check ----

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        if (!file.getFileType().equals(JavaFileType.INSTANCE) || !editor.getSelectionModel().hasSelection()) {
            return false;
        }
        var expressionInRange = getExpressionInRange(editor, file);
        if (expressionInRange instanceof PsiPolyadicExpression polyadicExpr) {
            if (!JavaTokenType.OROR.equals(polyadicExpr.getOperationTokenType())) {
                return false;
            }
            return expressionInRange instanceof PsiBinaryExpression binaryExpr
                //Both side is a method call to one of the CallMatcher.<matches> methods, with the same parameter expression
                ? MATCHES_MATCHERS.stream().anyMatch(matchesType -> areBothOperandsCallToMatches(binaryExpr, matchesType))
                && firstArgumentOf(binaryExpr.getLOperand()).textMatches(firstArgumentOf(binaryExpr.getROperand()))
                //All operands are method calls to one of the CallMatcher.<matches> methods, with the same parameter expression
                : Arrays.stream(polyadicExpr.getOperands()).allMatch(PsiMethodCallExpression.class::isInstance)
                && MATCHES_MATCHERS.stream().anyMatch(matchesType -> Arrays.stream(polyadicExpr.getOperands()).allMatch(operand -> isCallToCallMatcher(operand, matchesType)))
                && Arrays.stream(polyadicExpr.getOperands()).map(operand -> firstArgumentOf(operand).getText()).distinct().count() == 1;
        }
        return false;
    }

    private boolean areBothOperandsCallToMatches(PsiBinaryExpression binaryExpression, CallMatcher matchesType) {
        return isCallToCallMatcher(binaryExpression.getLOperand(), matchesType) && isCallToCallMatcher(binaryExpression.getROperand(), matchesType);
    }

    private boolean isCallToCallMatcher(PsiExpression expression, CallMatcher matchesType) {
        return expression instanceof PsiMethodCallExpression && matchesType.matches(expression);
    }

    private PsiExpression firstArgumentOf(PsiExpression call) {
        return ((PsiMethodCallExpression) call).getArgumentList().getExpressions()[0];
    }

    private PsiExpression getExpressionInRange(Editor editor, PsiFile file) {
        return PsiUtil.skipParenthesizedExprDown(CodeInsightUtil.findExpressionInRange(file, editor.getSelectionModel().getSelectionStart(), editor.getSelectionModel().getSelectionEnd()));
    }

    //---- Invocation ----

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        var expressionInRange = getExpressionInRange(editor, file);
        var parentClasses = getParentClasses(expressionInRange);
        if (parentClasses.size() > 1) {
            introduceCombinedCallMatcherInSelectedClass(parentClasses, selectedParentClass -> introduceCombinedCallMatcher(expressionInRange, selectedParentClass, file, editor), editor, project);
        } else if (parentClasses.size() == 1) {
            introduceCombinedCallMatcher(expressionInRange, parentClasses.get(0), file, editor);
        }
    }

    /**
     * Based on {@link com.intellij.refactoring.introduceField.BaseExpressionToFieldHandler}.
     */
    private List<PsiClass> getParentClasses(@NotNull PsiExpression expression) {
        var parentClasses = new SmartList<PsiClass>();
        var element = expression.getUserData(ElementToWorkOn.PARENT);
        if (element == null) element = expression.getParent();
        var parent = element;
        while (parent != null) {
            if (parent instanceof PsiClass && LocalToFieldHandler.mayContainConstants((PsiClass) parent)) {
                parentClasses.add((PsiClass) parent);
            }
            parent = PsiTreeUtil.getParentOfType(parent, PsiClass.class);
        }
        return parentClasses;
    }

    /**
     * Shows a list popup with the available parent classes.
     */
    private void introduceCombinedCallMatcherInSelectedClass(List<PsiClass> parentClasses, Consumer<PsiClass> introduceField, Editor editor, Project project) {
        var step = new BaseListPopupStep<>(JustKittingBundle.message("intention.call.matcher.combine.to.any.of.select.class"), parentClasses) {
            @Override
            public @Nullable PopupStep<?> onChosen(PsiClass selectedParentClass, boolean finalChoice) {
                introduceField.consume(selectedParentClass);
                return null;
            }
        };
        JBPopupFactory.getInstance().createListPopup(project, step, __ -> new PsiClassListCellRenderer()).showInBestPositionFor(editor);
    }

    /**
     * Creates a private static final CallMatcher field, calling to {@code CallMatcher.anyOf()} parameterized with the
     * list of CallMatchers from the selected expression.
     * <p>
     * Then replaces the selected expression with {@code <ANY_OF>.matches(expression)}, then initiates an inplace rename.
     */
    private void introduceCombinedCallMatcher(PsiExpression expressionInRange, PsiClass selectedParentClass, PsiFile file, Editor editor) {
        var elementFactory = JavaPsiFacade.getElementFactory(file.getProject());
        PsiElement anyOfField = JavaCodeStyleManager.getInstance(file.getProject())
            .shortenClassReferences(elementFactory
                .createFieldFromText("private static final com.siyeh.ig.callMatcher.CallMatcher ANY_OF = CallMatcher.anyOf(" + callMatcherAnyOfParamListFrom(expressionInRange) + ");", file));
        WriteCommandAction.runWriteCommandAction(file.getProject(), () -> {
            selectedParentClass.add(anyOfField);
            expressionInRange.replace(elementFactory.createExpressionFromText("ANY_OF." + anyOfMatchesFrom(expressionInRange), expressionInRange));
        });
        //The original field created is in a dummy editor, thus we need to work with the one actually introduced in the selected class
        PsiField anyOf = selectedParentClass.findFieldByName("ANY_OF", false);
        //Selecting the field name to have a smoother rename experience
        editor.getCaretModel().moveToOffset(anyOf.getTextOffset());
        editor.getSelectionModel().selectWordAtCaret(false);
        new MemberInplaceRenamer(anyOf, null, editor).performInplaceRename();
    }

    /**
     * Builds the string for the parameter list of the {@code CallMatcher.anyOf()} call, essentially the list of matcher expressions
     * that are used in the selected expression.
     * <p>
     * The returned string will be something like: {@code MATCHER_1, MATCHER_2}.
     */
    private String callMatcherAnyOfParamListFrom(PsiExpression expressionInRange) {
        return expressionInRange instanceof PsiBinaryExpression binaryExpression
            ? getQualifierText(binaryExpression.getLOperand()) + ", " + getQualifierText(binaryExpression.getROperand())
            : Arrays.stream(((PsiPolyadicExpression) expressionInRange).getOperands()).map(this::getQualifierText).collect(joining(","));
    }

    /**
     * Builds the selected expression's replacement string, taking into account the type of matcher method that were called.
     * <p>
     * The returned string will be something like: {@code (uCallMatches(expression))}.
     */
    private String anyOfMatchesFrom(PsiExpression expressionInRange) {
        if (expressionInRange instanceof PsiBinaryExpression) {
            var lOperand = (PsiMethodCallExpression) ((PsiBinaryExpression) expressionInRange).getLOperand();
            return lOperand.getMethodExpression().getReferenceName() + "(" + lOperand.getArgumentList().getExpressions()[0].getText() + ")";
        }
        var firstOperand = (PsiMethodCallExpression) ((PsiPolyadicExpression) expressionInRange).getOperands()[0];
        return firstOperand.getMethodExpression().getReferenceName() + "(" + firstArgumentOf(firstOperand).getText() + ")";
    }

    private String getQualifierText(PsiExpression matchesCall) {
        return ((PsiMethodCallExpression) matchesCall).getMethodExpression().getQualifierExpression().getText();
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }
}
