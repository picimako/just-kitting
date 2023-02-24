//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.devkitplus.intention.state;

import static com.picimako.devkitplus.PlatformNames.PERSISTENT_STATE_COMPONENT;
import static com.picimako.devkitplus.PlatformNames.STATE_ANNOTATION;

import com.intellij.codeInsight.actions.BaseCodeInsightAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for the child intention actions for converting classes to {@code PersistentStateComponent}s.
 * <p>
 * These intentions are available whenever {@link MakeClassPersistentStateComponentIntention} is available,
 * and not context-dependent individually.
 *
 * @see MakeClassPersistentStateComponentIntention
 * @see ConversionActions
 */
abstract class BasePersistentStateComponentConversionIntention extends BaseCodeInsightAction {

    /**
     * Adds the {@link com.intellij.openapi.components.State} annotation to the target class with some default values.
     * <p>
     * The {@code name} property is set to the target class' name, while the {@code storages} property is set to a single
     * {@link com.intellij.openapi.components.Storage} annotation with a dummy text.
     * <p>
     * <h3>From:</h3>
     * <pre>
     * public class SomeComponent {
     * }
     * </pre>
     * <h3>To:</h3>
     * <pre>
     * import com.intellij.openapi.components.State;
     * import com.intellij.openapi.components.Storage;
     *
     * &#064;State(name = "SomeComponent", storages = @Storage("TODO: INSERT STORAGE NAME"))
     * public class SomeComponent {
     * }
     * </pre>
     */
    protected static void addStateAnnotation(ConversionContext context) {
        // add @State annotation to class
        PsiAnnotation stateAnnotation = context.targetClass.getModifierList().addAnnotation(STATE_ANNOTATION);
        stateAnnotation.setDeclaredAttributeValue("name", context.factory.createExpressionFromText("\"" + context.targetClass.getName() + "\"", stateAnnotation));
        stateAnnotation.setDeclaredAttributeValue("storages", context.factory.createAnnotationFromText("@com.intellij.openapi.components.Storage(\"TODO: INSERT STORAGE NAME\")", stateAnnotation));
        PsiElement psiElement = context.styleManager.shortenClassReferences(stateAnnotation);
        stateAnnotation.replace(psiElement);
    }

    /**
     * Adds {@link com.intellij.openapi.components.PersistentStateComponent} to the class' implements list with the
     * state class name provided in the {@code stateClassName} argument.
     * <p>
     * <h3>From:</h3>
     * <pre>
     * public class SomeComponent {
     * }
     * </pre>
     * <h3>To (given stateClassName is 'SomeComponent.State'):</h3>
     * <pre>
     * import com.intellij.openapi.components.PersistentStateComponent;
     *
     * public class SomeComponent implements PersistentStateComponent&lt;SomeComponent.State> {
     * }
     * </pre>
     *
     * @param stateClassName the name of the class that holds the state of this component
     */
    protected static void addPersistentStateComponentToImplementsList(ConversionContext context, String stateClassName) {
        context.targetClass.getImplementsList()
            .add(context.styleManager.shortenClassReferences(context.factory.createReferenceFromText(PERSISTENT_STATE_COMPONENT + "<" + stateClassName + ">", context.targetClass.getImplementsList())));
    }

    /**
     * Adds an inner class named {@code State} within the target component class.
     *
     * <h3>From:</h3>
     * <pre>
     * public class SomeComponent {
     * }
     * </pre>
     * <h3>To:</h3>
     * <pre>
     * public class SomeComponent {
     *   static final class State {
     *   }
     * }
     * </pre>
     */
    protected static void addStandaloneStateClass(ConversionContext context) {
        PsiClass stateClass = context.factory.createClassFromText("", context.targetClass);
        stateClass.setName("State");
        stateClass.getModifierList().setModifierProperty(PsiModifier.STATIC, true);
        stateClass.getModifierList().setModifierProperty(PsiModifier.FINAL, true);
        context.targetClass.add(stateClass);
    }

    protected static ConversionContext createContext(@NotNull Project project, Editor editor, PsiFile file) {
        ConversionContext context = new ConversionContext();
        context.factory = PsiElementFactory.getInstance(project);
        context.styleManager = JavaCodeStyleManager.getInstance(project);
        context.targetClass = (PsiClass) file.findElementAt(editor.getCaretModel().getOffset()).getParent();
        context.project = project;
        return context;
    }

    static final class ConversionContext {
        PsiElementFactory factory;
        JavaCodeStyleManager styleManager;
        PsiClass targetClass;
        Project project;
    }
}
