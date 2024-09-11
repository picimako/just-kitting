//Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.intention.callmatcher;

import static com.picimako.justkitting.CallMatcherUtil.EXACT_INSTANCE_CALL;
import static com.picimako.justkitting.CallMatcherUtil.INSTANCE_CALL;
import static com.picimako.justkitting.CallMatcherUtil.STATIC_CALL;
import static java.util.stream.Collectors.joining;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiParameterList;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.ui.SimpleListCellRenderer;
import com.picimako.justkitting.resources.JustKittingBundle;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Creates {@link com.siyeh.ig.callMatcher.CallMatcher} initializer calls for Java methods.
 * <p>
 * It supports static and instance methods, as wells as different types and number of parameters,
 * along with static nested classes.
 *
 * @since 0.1.0
 */
@RequiredArgsConstructor
public class JavaCallMatcherGenerator implements CallMatcherGenerator<PsiMethod, PsiMethodCallExpression> {
    private final Project project;
    private final Editor editor;

    @Override
    public void generateCallMatcherForMethod(@Nullable PsiMethod method, Consumer<String> postActions) {
        if (method == null) {
            CommonRefactoringUtil.showErrorHint(project, editor,
                JustKittingBundle.message("intention.call.matcher.could.not.resolve.method.message"),
                JustKittingBundle.message("intention.call.matcher.could.not.resolve.method.title"),
                "");
            return;
        }

        //In case of a static method it generates {@code CallMatcher.staticCall()}.
        if (method.getModifierList().hasModifierProperty(PsiModifier.STATIC)) {
            postActions.accept(generateCallMatcher(method, STATIC_CALL));
        }
        //In case of an instance method it lets the user choose between {@code instanceCall} and {@code exactInstanceCall}</li>
        else {
            //Displays a popup list with the options {@code instanceCall} and {@code exactInstanceCall}
            // to let the user choose.
            var step = new BaseListPopupStep<>(
                JustKittingBundle.message("intention.call.matcher.select.instance.call.type"),
                List.of(INSTANCE_CALL, EXACT_INSTANCE_CALL)) {
                @Override
                public @Nullable PopupStep<?> onChosen(String instanceCallType, boolean finalChoice) {
                    postActions.accept(generateCallMatcher(method, instanceCallType));
                    return null;
                }
            };

            JBPopupFactory.getInstance()
                .createListPopup(project, step, listCellRenderer -> SimpleListCellRenderer.create("", Object::toString))
                .showInBestPositionFor(editor);
        }
    }

    @Override
    public void generateCallMatcherForMethodCall(PsiMethodCallExpression methodCall, Consumer<String> postActions) {
        generateCallMatcherForMethod(methodCall.resolveMethod(), postActions);
    }

    private String generateCallMatcher(PsiMethod method, String callType) {
        //Initializer: CallMatcher.instanceCall(" or CallMatcher.staticCall("
        var callMatcher = new StringBuilder("CallMatcher.").append(callType).append("(\"")
            //Class name: CallMatcher.instanceCall("SomeClassName",
            .append(method.getContainingClass().getQualifiedName()).append("\", ")
            //Method name: CallMatcher.instanceCall("SomeClassName", "someMethodName")
            .append("\"").append(method.getName()).append("\"").append(")");

        var parameterList = method.getParameterList();
        if (!parameterList.isEmpty()) {
            //The parameter list delimited by commas:
            // CallMatcher.instanceCall("SomeClassName", "someMethodName").parameterTypes("Type1", "Type2")
            callMatcher.append(".parameterTypes(").append(generateParameters(parameterList)).append(")");
        }

        //Closing the statement with a semicolon.
        // CallMatcher.instanceCall("SomeClassName", "someMethodName");
        // or
        // CallMatcher.instanceCall("SomeClassName", "someMethodName").parameterTypes("Type1", "Type2");
        return callMatcher.append(";").toString();
    }

    /**
     * Generates the String representation of parameter list,
     * inside the {@code .parameterTypes("Type1", "Type2")} calls, from the argument method parameter list.
     * <p>
     * The parameters are delimited by commas.
     *
     * @param parameterList the method's parameter list
     */
    private static String generateParameters(PsiParameterList parameterList) {
        return Arrays.stream(parameterList.getParameters())
            .map(parameter -> "\"" + parameter.getType().getCanonicalText() + "\"")
            .collect(joining(", "));
    }
}
