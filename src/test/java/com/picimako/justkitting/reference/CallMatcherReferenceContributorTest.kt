//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.reference

import com.intellij.openapi.application.ReadAction.compute
import com.intellij.psi.*
import com.intellij.psi.util.MethodSignature
import com.picimako.justkitting.JustKittingTestBase
import com.picimako.justkitting.ThirdPartyLibraryLoader
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Functional test for [CallMatcherReferenceContributor].
 */
class CallMatcherReferenceContributorTest : JustKittingTestBase() {

    @BeforeEach
    override fun setUp() {
        ThirdPartyLibraryLoader.loadJavaImpl(fixture)
    }

    //Class reference

    @Test
    fun testClassReferenceWithNoMethodNameSpecified() {
        fixture.configureByText(
            "CallMatcherChecker.java",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class CallMatcherChecker {
                   CallMatcher callMatcher = CallMatcher.instanceCall("java.uti<caret>l.List");
                }
                """.trimIndent()
        )

        val resolveResults = resolveElementReferences(findElementAtCaret())
        assertThat(resolveResults).hasSize(1)
        assertThat((resolveResults[0].element as PsiClass?)!!.qualifiedName).isEqualTo("java.util.List")
    }

    @Test
    fun testClassReferenceWithMethodNameSpecified() {
        fixture.configureByText(
            "CallMatcherChecker.java",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class CallMatcherChecker {
                   CallMatcher callMatcher = CallMatcher.instanceCall("java.uti<caret>l.List", "add");
                }
                """.trimIndent()
        )

        val resolveResults = resolveElementReferences(findElementAtCaret())
        assertThat(resolveResults).hasSize(1)
        assertThat((resolveResults[0].element as PsiClass?)!!.qualifiedName).isEqualTo("java.util.List")
    }

    @Test
    fun testNoClassReference() {
        fixture.configureByText(
            "CallMatcherChecker.java",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class CallMatcherChecker {
                   CallMatcher callMatcher = CallMatcher.instanceCall("<caret>List", "add");
                }
                """.trimIndent()
        )
        val element = findElementAtCaret()
        assertThat(compute<Array<PsiReference>, Exception> { element?.references }).isEmpty()
    }

    //Method reference

    @Test
    fun testSingleMethodReferenceResult() {
        fixture.configureByText(
            "CallMatcherChecker.java",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class CallMatcherChecker {
                   CallMatcher callMatcher = CallMatcher.instanceCall("java.util.List", "cl<caret>ear");
                }
                """.trimIndent()
        )

        val resolveResults = resolveElementReferences(findElementAtCaret())
        assertThat(resolveResults).hasSize(2)
        assertThat(getMethodSignatureOfResult(resolveResults[0])).hasToString("MethodSignatureBackedByPsiMethod: clear([])") //from java.util.List
        assertThat(getMethodSignatureOfResult(resolveResults[1])).hasToString("MethodSignatureBackedByPsiMethod: clear([])") //from java.util.Collection
    }

    @Test
    fun testSingleMethodReferenceResultEvaluated() {
        fixture.configureByText(
            "CallMatcherChecker.java",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class CallMatcherChecker {
                   private static final String CALL_MATCHER = "com.siyeh.ig.callMatcher.CallMatcher";
                   private static final CallMatcher callMatcher = CallMatcher.instanceCall(CALL_MATCHER, "na<caret>mes");
                }
                """.trimIndent()
        )

        val resolveResults = resolveElementReferences(findElementAtCaret())
        assertThat(resolveResults).hasSize(1)
        assertThat(getMethodSignatureOfResult(resolveResults[0])).hasToString("MethodSignatureBackedByPsiMethod: names([])")
    }

    @Test
    fun testMultipleMethodReferenceResults() {
        fixture.configureByText(
            "CallMatcherChecker.java",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class CallMatcherChecker {
                   CallMatcher callMatcher = CallMatcher.instanceCall("java.util.List", "a<caret>dd");
                }
                """.trimIndent()
        )

        val resolveResults = resolveElementReferences(findElementAtCaret())
        assertThat(resolveResults).hasSize(3)
        assertThat(getMethodSignatureOfResult(resolveResults[0])).hasToString("MethodSignatureBackedByPsiMethod: add([PsiType:E])") //from java.util.List
        assertThat(getMethodSignatureOfResult(resolveResults[1])).hasToString("MethodSignatureBackedByPsiMethod: add([PsiType:int, PsiType:E])") //from java.util.List
        assertThat(getMethodSignatureOfResult(resolveResults[2])).hasToString("MethodSignatureBackedByPsiMethod: add([PsiType:E])") //from java.util.Collection
    }

    @Test
    fun testNoMethodReferenceResult() {
        fixture.configureByText(
            "CallMatcherChecker.java",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class CallMatcherChecker {
                   CallMatcher callMatcher = CallMatcher.instanceCall("java.util.List", "asda<caret>sd");
                }
                """.trimIndent()
        )

        val resolveResults = resolveElementReferences(findElementAtCaret())
        assertThat(resolveResults).isEmpty()
    }

    @Test
    fun testMethodReferenceInExactClass() {
        fixture.configureByText(
            "CallMatcherChecker.java",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class CallMatcherChecker {
                   CallMatcher callMatcher = CallMatcher.exactInstanceCall("CallMatcherChecker.Inner", "some<caret>Method");

                   private static final class Inner extends SuperInner {
                       public void someMethod() {
                       }
                   }

                   private static class SuperInner {
                       public void someMethod(int i) {
                      }
                   }
                }
                """.trimIndent()
        )

        val resolveResults = resolveElementReferences(findElementAtCaret())
        assertThat(resolveResults).hasSize(1)
        assertThat(getMethodSignatureOfResult(resolveResults[0])).hasToString("MethodSignatureBackedByPsiMethod: someMethod([])")
    }

    @Test
    fun testMethodReferenceInExactAndSuperClass() {
        fixture.configureByText(
            "CallMatcherChecker.java",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class CallMatcherChecker {
                   CallMatcher callMatcher = CallMatcher.instanceCall("CallMatcherChecker.Inner", "some<caret>Method");

                   private static final class Inner extends SuperInner {
                       public void someMethod() {
                       }
                   }

                   private static class SuperInner {
                       public void someMethod(int i) {
                      }
                   }
                }
                """.trimIndent()
        )

        val resolveResults = resolveElementReferences(findElementAtCaret())
        assertThat(resolveResults).hasSize(2)
        assertThat(getMethodSignatureOfResult(resolveResults[0])).hasToString("MethodSignatureBackedByPsiMethod: someMethod([])")
        assertThat(getMethodSignatureOfResult(resolveResults[1] )).hasToString("MethodSignatureBackedByPsiMethod: someMethod([PsiType:int])")
    }

    @Test
    fun testMethodReferenceForStaticMethod() {
        fixture.configureByText(
            "CallMatcherChecker.java",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class CallMatcherChecker {
                   CallMatcher callMatcher = CallMatcher.staticCall("CallMatcherChecker.Inner", "some<caret>Method");

                   private static final class Inner {
                       public void someMethod() {
                       }
                       public static void someMethod() {
                       }
                   }
                }
                """.trimIndent()
        )

        val resolveResults = resolveElementReferences(findElementAtCaret())
        assertThat(resolveResults).hasSize(1)
        assertThat(getMethodSignatureOfResult(resolveResults[0])).hasToString("MethodSignatureBackedByPsiMethod: someMethod([])")
    }

    @Test
    fun testMethodReferenceForStaticInterfaceMethod() {
        fixture.configureByText(
            "CallMatcherChecker.java",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class CallMatcherChecker {
                   CallMatcher callMatcher = CallMatcher.staticCall("CallMatcherChecker.InnerInt", "some<caret>Method");

                   private static final class Inner {
                       public void someMethod() {
                       }
                       public static void someMethod() {
                       }
                   }
                   private static final interface InnerInt {
                       public static void someMethod();
                   }
                }
                """.trimIndent()
        )

        val resolveResults = resolveElementReferences(findElementAtCaret())
        assertThat(resolveResults).hasSize(1)
        assertThat(getMethodSignatureOfResult(resolveResults[0])).hasToString("MethodSignatureBackedByPsiMethod: someMethod([])")
    }

    @Test
    fun testMethodReferenceFoExternalStaticMethod() {
        fixture.configureByText(
            "CallMatcherChecker.java",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class CallMatcherChecker {
                   CallMatcher callMatcher = CallMatcher.staticCall("java.lang.Integer", "toUnsignedSt<caret>ring");
                }
                """.trimIndent()
        )

        val resolveResults = resolveElementReferences(findElementAtCaret())
        assertThat(resolveResults).hasSize(2)
        assertThat(getMethodSignatureOfResult(resolveResults[0])).hasToString("MethodSignatureBackedByPsiMethod: toUnsignedString([PsiType:int, PsiType:int])")
        assertThat(getMethodSignatureOfResult(resolveResults[1])).hasToString("MethodSignatureBackedByPsiMethod: toUnsignedString([PsiType:int])")
    }

    private fun findElementAtCaret(): PsiElement? =
        compute<PsiElement, Exception> { fixture.file.findElementAt(fixture.caretOffset)!!.parent }

    private fun resolveElementReferences(element: PsiElement?): Array<ResolveResult> =
        compute<Array<ResolveResult>, Exception> { (element?.references[0] as PsiPolyVariantReference).multiResolve(false) }

    private fun getMethodSignatureOfResult(resolveResult: ResolveResult): MethodSignature? =
        compute<MethodSignature, Exception> { (resolveResult.element as PsiMethod?)!!.getSignature(PsiSubstitutor.EMPTY) }
}
