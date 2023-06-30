//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.intention.callmatcher;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.application.ex.ClipboardUtil;
import com.intellij.psi.PsiFile;
import com.picimako.justkitting.ThirdPartyLibraryLoader;
import com.picimako.justkitting.intention.JustKittingIntentionTestBase;

/**
 * Integration test for {@link GenerateCallMatcherFromSignatureIntention}.
 */
public class GenerateCallMatcherFromSignatureIntentionTest extends JustKittingIntentionTestBase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ThirdPartyLibraryLoader.loadJavaImpl(myFixture);
    }

    @Override
    protected IntentionAction getIntention() {
        return new GenerateCallMatcherFromSignatureIntention();
    }

    //Availability

    public void testNotAvailableInNonJavaFile() {
        PsiFile psiFile = myFixture.configureByText("NotAvailable.kt", "<caret>");

        checkIfNotAvailableIn(psiFile);
    }

    public void testNotAvailableOnNonMethodIdentifier() {
        PsiFile psiFile = myFixture.configureByText("NotAvailable.java",
            "public class NotAva<caret>ilable {\n" +
                "}");

        checkIfNotAvailableIn(psiFile);
    }

    public void testAvailableOnMethodIdentifier() {
        PsiFile psiFile = myFixture.configureByText("Available.java",
            """
                public class Available {
                    public void met<caret>hod() {
                    }
                }""");

        checkIfAvailableIn(psiFile);
    }

    public void testAvailableOnMethodCallIdentifier() {
        PsiFile psiFile = myFixture.configureByText("Available.java",
            """
                public class Available {
                    public void met<caret>hod() {
                       methodToC<caret>all();
                    }
                    public void methodToCall() {
                    }
                }""");

        checkIfAvailableIn(psiFile);
    }

    //Generation from method signature

    public void testGeneratesMatcherFromNoParameterMethod() {
        PsiFile psiFile = myFixture.configureByText("NoParameter.java",
            """
                public class NoParameter {
                    public void noParam<caret>eterMethod() {
                    }
                }""");
        runIntentionOn(psiFile, getIntention());

        assertThat(ClipboardUtil.getTextInClipboard()).isEqualTo(
            "CallMatcher.instanceCall(\"NoParameter\", \"noParameterMethod\");");
    }

    public void testGeneratesMatcherFromOneParameterMethod() {
        PsiFile psiFile = myFixture.configureByText("OneParameter.java",
            """
                package generate.call.matcher;
                public class OneParameter {
                    public void oneParam<caret>eterMethod(String singleStringParam) {
                    }
                }""");
        runIntentionOn(psiFile, getIntention());

        assertThat(ClipboardUtil.getTextInClipboard()).isEqualTo(
            "CallMatcher.instanceCall(\"generate.call.matcher.OneParameter\", \"oneParameterMethod\").parameterTypes(\"java.lang.String\");");
    }

    public void testGeneratesMatcherFromMultipleParameterMethod() {
        PsiFile psiFile = myFixture.configureByText("MultipleParameters.java",
            """
                package generate.call.matcher;
                import java.util.List;

                public class MultipleParameters {
                    public void multipleParam<caret>eterMethod(String stringParam, int intParam, List listParam) {
                    }
                }""");
        runIntentionOn(psiFile, getIntention());

        assertThat(ClipboardUtil.getTextInClipboard()).isEqualTo(
            "CallMatcher.instanceCall(\"generate.call.matcher.MultipleParameters\", \"multipleParameterMethod\").parameterTypes(\"java.lang.String\", \"int\", \"java.util.List\");");
    }

    public void testGeneratesMatcherFromVarargParameterMethod() {
        PsiFile psiFile = myFixture.configureByText("VarargParameter.java",
            """
                package generate.call.matcher;
                public class VarargParameter {
                    public void varargParam<caret>eterMethod(int intParam, String... stringParam) {
                    }
                }""");
        runIntentionOn(psiFile, getIntention());

        assertThat(ClipboardUtil.getTextInClipboard()).isEqualTo(
            "CallMatcher.instanceCall(\"generate.call.matcher.VarargParameter\", \"varargParameterMethod\").parameterTypes(\"int\", \"java.lang.String...\");");
    }

    public void testGeneratesMatcherFromGenericParameterMethod() {
        PsiFile psiFile = myFixture.configureByText("GenericParameter.java",
            """
                package generate.call.matcher;
                public class GenericParameter<T> {
                    public void genericParam<caret>eterMethod(T genericParameter) {
                    }
                }""");
        runIntentionOn(psiFile, getIntention());

        assertThat(ClipboardUtil.getTextInClipboard()).isEqualTo(
            "CallMatcher.instanceCall(\"generate.call.matcher.GenericParameter\", \"genericParameterMethod\").parameterTypes(\"T\");");
    }

    public void testGeneratesMatcherFromParameterWithGenericTypeMethod() {
        PsiFile psiFile = myFixture.configureByText("ParameterWithGenericType.java",
            """
                package generate.call.matcher;
                import java.util.List;

                public class ParameterWithGenericType {
                    public void <T> parameterWith<caret>GenericTypeMethod(List<T> genericTypeParameter) {
                    }
                }""");
        runIntentionOn(psiFile, getIntention());

        assertThat(ClipboardUtil.getTextInClipboard()).isEqualTo(
            "CallMatcher.instanceCall(\"generate.call.matcher.ParameterWithGenericType\", \"parameterWithGenericTypeMethod\").parameterTypes(\"java.util.List<T>\");");
    }

    public void testGeneratesMatcherFromParameterWithExactGenericTypeMethod() {
        PsiFile psiFile = myFixture.configureByText("ParameterWithExactGenericType.java",
            """
                package generate.call.matcher;
                import java.util.List;

                public class ParameterWithExactGenericType {
                    public void parameterWith<caret>ExactGenericTypeMethod(List<Integer> genericTypeParameter) {
                    }
                }""");
        runIntentionOn(psiFile, getIntention());

        assertThat(ClipboardUtil.getTextInClipboard()).isEqualTo(
            "CallMatcher.instanceCall(\"generate.call.matcher.ParameterWithExactGenericType\", \"parameterWithExactGenericTypeMethod\").parameterTypes(\"java.util.List<java.lang.Integer>\");");
    }

    public void testGeneratesMatcherFromParameterWithBoundGenericTypeMethod() {
        PsiFile psiFile = myFixture.configureByText("ParameterWithBoundGenericType.java",
            """
                package generate.call.matcher;

                public class ParameterWithBoundGenericType {
                    public void parameterWith<caret>BoundGenericTypeMethod(java.lang.Class<? extends java.lang.Throwable> genericTypeParameter) {
                    }
                }""");
        runIntentionOn(psiFile, getIntention());

        assertThat(ClipboardUtil.getTextInClipboard()).isEqualTo(
            "CallMatcher.instanceCall(\"generate.call.matcher.ParameterWithBoundGenericType\", \"parameterWithBoundGenericTypeMethod\").parameterTypes(\"java.lang.Class<? extends java.lang.Throwable>\");");
    }

    public void testGeneratesMatcherFromParameterWithWildcardGenericTypeMethod() {
        PsiFile psiFile = myFixture.configureByText("ParameterWithWildcardGenericType.java",
            """
                package generate.call.matcher;
                import java.util.List;

                public class ParameterWithWildcardGenericType {
                    public void parameterWith<caret>WildcardTypeMethod(List<?> genericTypeParameter) {
                    }
                }""");
        runIntentionOn(psiFile, getIntention());

        assertThat(ClipboardUtil.getTextInClipboard()).isEqualTo(
            "CallMatcher.instanceCall(\"generate.call.matcher.ParameterWithWildcardGenericType\", \"parameterWithWildcardTypeMethod\").parameterTypes(\"java.util.List<?>\");");
    }

    public void testGeneratesMatcherFromArrayParameterMethod() {
        PsiFile psiFile = myFixture.configureByText("ArrayParameter.java",
            """
                package generate.call.matcher;
                public class ArrayParameter {
                    public void array<caret>ParameterMethod(String[] stringArray) {
                    }
                }""");
        runIntentionOn(psiFile, getIntention());

        assertThat(ClipboardUtil.getTextInClipboard()).isEqualTo(
            "CallMatcher.instanceCall(\"generate.call.matcher.ArrayParameter\", \"arrayParameterMethod\").parameterTypes(\"java.lang.String[]\");");
    }

    public void testGeneratesMatcherFromNestedClassMethod() {
        PsiFile psiFile = myFixture.configureByText("NestedClass.java",
            """
                package generate.call.matcher;
                public class NestedClassMethod {
                    public static final class NestedClass {
                        public void nested<caret>ClassMethod(String string) {
                        }
                    }
                }""");
        runIntentionOn(psiFile, getIntention());

        assertThat(ClipboardUtil.getTextInClipboard()).isEqualTo(
            "CallMatcher.instanceCall(\"generate.call.matcher.NestedClassMethod.NestedClass\", \"nestedClassMethod\").parameterTypes(\"java.lang.String\");");
    }

    public void testGeneratesMatcherFromStaticMethod() {
        PsiFile psiFile = myFixture.configureByText("StaticMethod.java",
            """
                package generate.call.matcher;
                public class StaticMethod {
                    public static void aStatic<caret>Method(String singleStringParam) {
                    }
                }""");
        runIntentionOn(psiFile, getIntention());

        assertThat(ClipboardUtil.getTextInClipboard()).isEqualTo(
            "CallMatcher.staticCall(\"generate.call.matcher.StaticMethod\", \"aStaticMethod\").parameterTypes(\"java.lang.String\");");
    }

//    public void testGeneratesExactInstanceMatcherFromMethod() {
//        PsiFile psiFile = myFixture.configureByText("ExactInstanceMethod.java",
//            "package generate.call.matcher;\n" +
//                "public class ExactInstanceMethod {\n" +
//                "    public void anExactInstance<caret>Method(String singleStringParam) {\n" +
//                "    }\n" +
//                "}");
//        runIntentionOn(psiFile, getIntention());
//
//        assertThat(ClipboardUtil.getTextInClipboard()).isEqualTo(
//            "CallMatcher.exactInstanceCall(\"generate.call.matcher.ExactInstanceMethod\", \"anExactInstanceMethod\").parameterTypes(\"java.lang.String\");");
//    }

    //Generation from method call

    public void testGeneratesMatcherFromNoParameterMethodFromCall() {
        PsiFile psiFile = myFixture.configureByText("NoParameter.java",
            """
                public class NoParameter {
                    public void callingNoParameterMethod() {
                       new InnerClass().noP<caret>arameterMethod();
                    }
                    public static class InnerClass {
                       public void noParameterMethod() {
                       }
                    }
                }""");
        runIntentionOn(psiFile, getIntention());

        assertThat(ClipboardUtil.getTextInClipboard()).isEqualTo(
            "CallMatcher.instanceCall(\"NoParameter.InnerClass\", \"noParameterMethod\");");
    }

    public void testGeneratesMatcherFromOneParameterMethodFromCall() {
        PsiFile psiFile = myFixture.configureByText("OneParameter.java",
            """
                package generate.call.matcher;
                public class OneParameter {
                    public void callingOneParameterMethod(String singleStringParam) {
                       new InnerClass().oneParam<caret>eterMethod(singleStringParam);
                    }
                    public static class InnerClass {
                       public void oneParameterMethod(String singleStringParam) {
                       }
                    }
                }""");
        runIntentionOn(psiFile, getIntention());

        assertThat(ClipboardUtil.getTextInClipboard()).isEqualTo(
            "CallMatcher.instanceCall(\"generate.call.matcher.OneParameter.InnerClass\", \"oneParameterMethod\").parameterTypes(\"java.lang.String\");");
    }

    public void testGeneratesMatcherFromMultipleParameterMethodFromCall() {
        PsiFile psiFile = myFixture.configureByText("MultipleParameters.java",
            """
                package generate.call.matcher;
                import java.util.List;

                public class MultipleParameters {
                    public void callingMultipleParameterMethod(String stringParam, int intParam, List listParam) {
                       new InnerClass().multipleParam<caret>eterMethod(stringParam, intParam, listParam);
                    }
                    public static class InnerClass {
                       public void multipleParameterMethod(String stringParam, int intParam, List listParam) {
                       }
                    }
                }""");
        runIntentionOn(psiFile, getIntention());

        assertThat(ClipboardUtil.getTextInClipboard()).isEqualTo(
            "CallMatcher.instanceCall(\"generate.call.matcher.MultipleParameters.InnerClass\", \"multipleParameterMethod\").parameterTypes(\"java.lang.String\", \"int\", \"java.util.List\");");
    }

    public void testGeneratesMatcherFromVarargParameterMethodFromCall() {
        PsiFile psiFile = myFixture.configureByText("VarargParameter.java",
            """
                package generate.call.matcher;
                public class VarargParameter {
                    public void varargParam<caret>eterMethod(int intParam, String... stringParam) {
                       new InnerClass().varargParam<caret>eterMethod(intParam, stringParam);
                    }
                    public static class InnerClass {
                       public void varargParameterMethod(int intParam, String... stringParam) {
                       }
                    }
                }""");
        runIntentionOn(psiFile, getIntention());

        assertThat(ClipboardUtil.getTextInClipboard()).isEqualTo(
            "CallMatcher.instanceCall(\"generate.call.matcher.VarargParameter.InnerClass\", \"varargParameterMethod\").parameterTypes(\"int\", \"java.lang.String...\");");
    }

    public void testGeneratesMatcherFromGenericParameterMethodFromCall() {
        PsiFile psiFile = myFixture.configureByText("GenericParameter.java",
            """
                package generate.call.matcher;
                public class GenericParameter<T> {
                    public void genericParameterMethod(T genericParameter) {
                       new InnerClass().genericParam<caret>eterMethod(genericParameter);
                    }
                    public static class InnerClass {
                       public void genericParameterMethod(T genericParameter) {
                       }
                    }
                }""");
        runIntentionOn(psiFile, getIntention());

        assertThat(ClipboardUtil.getTextInClipboard()).isEqualTo(
            "CallMatcher.instanceCall(\"generate.call.matcher.GenericParameter.InnerClass\", \"genericParameterMethod\").parameterTypes(\"T\");");
    }

//    public void testGeneratesMatcherFromParameterWithGenericTypeMethodFromCall() {
//        PsiFile psiFile = myFixture.configureByText("ParameterWithGenericType.java",
//            "package generate.call.matcher;\n" +
//                "import java.util.List;\n" +
//                "\n" +
//                "public class ParameterWithGenericType {\n" +
//                "    public void <T> parameterWithGenericTypeMethod(List<T> genericTypeParameter) {\n" +
//                "       new InnerClass().parameterWith<caret>GenericTypeMethod(genericTypeParameter);\n" +
//                "    }\n" +
//                "    public static class InnerClass {\n" +
//                "       public void <T> parameterWithGenericTypeMethod(List<T> genericTypeParameter) {\n" +
//                "       }\n" +
//                "    }\n" +
//                "}");
//        runIntentionOn(psiFile, getIntention());
//
//        assertThat(ClipboardUtil.getTextInClipboard()).isEqualTo(
//            "CallMatcher.instanceCall(\"generate.call.matcher.ParameterWithGenericType.InnerClass\", \"parameterWithGenericTypeMethod\").parameterTypes(\"java.util.List<T>\");");
//    }

    public void testGeneratesMatcherFromParameterWithGenericClassTypeMethodFromCall() {
        PsiFile psiFile = myFixture.configureByText("ParameterWithGenericType.java",
            """
                package generate.call.matcher;
                import java.util.List;

                public class ParameterWithGenericType {
                    public void parameterWithGenericTypeMethod() {
                       new InnerClass<String>().parameterWith<caret>GenericTypeMethod(List.of());
                    }
                    public static class InnerClass<T> {
                       public void parameterWithGenericTypeMethod(List<T> genericTypeParameter) {
                       }
                    }
                }""");
        runIntentionOn(psiFile, getIntention());

        assertThat(ClipboardUtil.getTextInClipboard()).isEqualTo(
            "CallMatcher.instanceCall(\"generate.call.matcher.ParameterWithGenericType.InnerClass\", \"parameterWithGenericTypeMethod\").parameterTypes(\"java.util.List<T>\");");
    }

    public void testGeneratesMatcherFromParameterWithExactGenericTypeMethodFromCall() {
        PsiFile psiFile = myFixture.configureByText("ParameterWithExactGenericType.java",
            """
                package generate.call.matcher;
                import java.util.List;

                public class ParameterWithExactGenericType {
                    public void parameterWithExactGenericTypeMethod(List<Integer> genericTypeParameter) {
                       new InnerClass().parameterWith<caret>ExactGenericTypeMethod(genericTypeParameter);
                    }
                    public static class InnerClass {
                       public void parameterWithExactGenericTypeMethod(List<Integer> genericTypeParameter) {
                       }
                    }
                }""");
        runIntentionOn(psiFile, getIntention());

        assertThat(ClipboardUtil.getTextInClipboard()).isEqualTo(
            "CallMatcher.instanceCall(\"generate.call.matcher.ParameterWithExactGenericType.InnerClass\", \"parameterWithExactGenericTypeMethod\").parameterTypes(\"java.util.List<java.lang.Integer>\");");
    }

    public void testGeneratesMatcherFromParameterWithBoundGenericTypeMethodFromCall() {
        PsiFile psiFile = myFixture.configureByText("ParameterWithBoundGenericType.java",
            """
                package generate.call.matcher;

                public class ParameterWithBoundGenericType {
                    public void parameterWithBoundGenericTypeMethod(java.lang.Class<? extends java.lang.Throwable> genericTypeParameter) {
                       new InnerClass().parameterWith<caret>BoundGenericTypeMethod(genericTypeParameter);
                    }
                    public static class InnerClass {
                       public void parameterWithBoundGenericTypeMethod(java.lang.Class<? extends java.lang.Throwable> genericTypeParameter) {
                       }
                    }
                }""");
        runIntentionOn(psiFile, getIntention());

        assertThat(ClipboardUtil.getTextInClipboard()).isEqualTo(
            "CallMatcher.instanceCall(\"generate.call.matcher.ParameterWithBoundGenericType.InnerClass\", \"parameterWithBoundGenericTypeMethod\").parameterTypes(\"java.lang.Class<? extends java.lang.Throwable>\");");
    }

    public void testGeneratesMatcherFromParameterWithWildcardGenericTypeMethodFromCall() {
        PsiFile psiFile = myFixture.configureByText("ParameterWithWildcardGenericType.java",
            """
                package generate.call.matcher;
                import java.util.List;

                public class ParameterWithWildcardGenericType {
                    public void parameterWithWildcardTypeMethod(List<?> genericTypeParameter) {
                       new InnerClass().parameterWith<caret>WildcardTypeMethod(genericTypeParameter);
                    }
                    public static class InnerClass {
                       public void parameterWithWildcardTypeMethod(List<?> genericTypeParameter) {
                       }
                    }
                }""");
        runIntentionOn(psiFile, getIntention());

        assertThat(ClipboardUtil.getTextInClipboard()).isEqualTo(
            "CallMatcher.instanceCall(\"generate.call.matcher.ParameterWithWildcardGenericType.InnerClass\", \"parameterWithWildcardTypeMethod\").parameterTypes(\"java.util.List<?>\");");
    }

    public void testGeneratesMatcherFromArrayParameterMethodFromCall() {
        PsiFile psiFile = myFixture.configureByText("ArrayParameter.java",
            """
                package generate.call.matcher;
                public class ArrayParameter {
                    public void arrayParameterMethod(String[] stringArray) {
                       new InnerClass().array<caret>ParameterMethod(stringArray);
                    }
                    public static class InnerClass {
                       public void arrayParameterMethod(String[] stringArray) {
                       }
                    }
                }""");
        runIntentionOn(psiFile, getIntention());

        assertThat(ClipboardUtil.getTextInClipboard()).isEqualTo(
            "CallMatcher.instanceCall(\"generate.call.matcher.ArrayParameter.InnerClass\", \"arrayParameterMethod\").parameterTypes(\"java.lang.String[]\");");
    }

    public void testGeneratesMatcherFromStaticMethodFromCall() {
        PsiFile psiFile = myFixture.configureByText("StaticMethod.java",
            """
                package generate.call.matcher;
                public class StaticMethod {
                    public static void aStaticMethod(String singleStringParam) {
                       InnerClass.aStatic<caret>Method(singleStringParam);
                    }
                    public static class InnerClass {
                       public static void aStaticMethod(String singleStringParam) {
                       }
                    }
                }""");
        runIntentionOn(psiFile, getIntention());

        assertThat(ClipboardUtil.getTextInClipboard()).isEqualTo(
            "CallMatcher.staticCall(\"generate.call.matcher.StaticMethod.InnerClass\", \"aStaticMethod\").parameterTypes(\"java.lang.String\");");
    }
}
