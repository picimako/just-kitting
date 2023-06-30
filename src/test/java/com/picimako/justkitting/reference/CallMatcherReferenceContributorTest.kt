//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.picimako.justkitting.reference

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.PsiSubstitutor
import com.picimako.justkitting.JustKittingTestBase
import com.picimako.justkitting.ThirdPartyLibraryLoader
import org.assertj.core.api.Assertions.assertThat

/**
 * Functional test for [CallMatcherReferenceContributor].
 */
class CallMatcherReferenceContributorTest : JustKittingTestBase() {

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        ThirdPartyLibraryLoader.loadJavaImpl(myFixture)
    }

    //Class reference

    fun testClassReferenceWithNoMethodNameSpecified() {
        myFixture.configureByText("CallMatcherChecker.java",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class CallMatcherChecker {
                   CallMatcher callMatcher = CallMatcher.instanceCall("java.uti<caret>l.List");
                }
                """.trimIndent())
        val element = myFixture.file.findElementAt(myFixture.caretOffset)!!.parent
        val resolveResults = (element.references[0] as PsiPolyVariantReference).multiResolve(false)
        assertThat(resolveResults).hasSize(1)
        assertThat((resolveResults[0].element as PsiClass?)!!.qualifiedName).isEqualTo("java.util.List")
    }

    fun testClassReferenceWithMethodNameSpecified() {
        myFixture.configureByText("CallMatcherChecker.java",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class CallMatcherChecker {
                   CallMatcher callMatcher = CallMatcher.instanceCall("java.uti<caret>l.List", "add");
                }
                """.trimIndent())
        val element = myFixture.file.findElementAt(myFixture.caretOffset)!!.parent
        val resolveResults = (element.references[0] as PsiPolyVariantReference).multiResolve(false)
        assertThat(resolveResults).hasSize(1)
        assertThat((resolveResults[0].element as PsiClass?)!!.qualifiedName).isEqualTo("java.util.List")
    }

    fun testNoClassReference() {
        myFixture.configureByText("CallMatcherChecker.java",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class CallMatcherChecker {
                   CallMatcher callMatcher = CallMatcher.instanceCall("<caret>List", "add");
                }
                """.trimIndent())
        val element = myFixture.file.findElementAt(myFixture.caretOffset)!!.parent
        assertThat(element.references).isEmpty()
    }

    //Method reference

    fun testSingleMethodReferenceResult() {
        myFixture.configureByText("CallMatcherChecker.java",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class CallMatcherChecker {
                   CallMatcher callMatcher = CallMatcher.instanceCall("java.util.List", "cl<caret>ear");
                }
                """.trimIndent())
        val element = myFixture.file.findElementAt(myFixture.caretOffset)!!.parent
        val resolveResults = (element.references[0] as PsiPolyVariantReference).multiResolve(false)
        assertThat(resolveResults).hasSize(2)
        assertThat((resolveResults[0].element as PsiMethod?)!!.getSignature(PsiSubstitutor.EMPTY)).hasToString("MethodSignatureBackedByPsiMethod: clear([])") //from java.util.List
        assertThat((resolveResults[1].element as PsiMethod?)!!.getSignature(PsiSubstitutor.EMPTY)).hasToString("MethodSignatureBackedByPsiMethod: clear([])") //from java.util.Collection
    }

    fun testSingleMethodReferenceResultEvaluated() {
        myFixture.configureByText("CallMatcherChecker.java",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class CallMatcherChecker {
                   private static final String CALL_MATCHER = "com.siyeh.ig.callMatcher.CallMatcher";
                   private static final CallMatcher callMatcher = CallMatcher.instanceCall(CALL_MATCHER, "na<caret>mes");
                }
                """.trimIndent())
        val element = myFixture.file.findElementAt(myFixture.caretOffset)!!.parent
        val resolveResults = (element.references[0] as PsiPolyVariantReference).multiResolve(false)
        assertThat(resolveResults).hasSize(1)
        assertThat((resolveResults[0].element as PsiMethod?)!!.getSignature(PsiSubstitutor.EMPTY)).hasToString("MethodSignatureBackedByPsiMethod: names([])")
    }

    fun testMultipleMethodReferenceResults() {
        myFixture.configureByText("CallMatcherChecker.java",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class CallMatcherChecker {
                   CallMatcher callMatcher = CallMatcher.instanceCall("java.util.List", "a<caret>dd");
                }
                """.trimIndent())
        val element = myFixture.file.findElementAt(myFixture.caretOffset)!!.parent
        val resolveResults = (element.references[0] as PsiPolyVariantReference).multiResolve(false)
        assertThat(resolveResults).hasSize(3)
        assertThat((resolveResults[0].element as PsiMethod?)!!.getSignature(PsiSubstitutor.EMPTY)).hasToString("MethodSignatureBackedByPsiMethod: add([PsiType:E])") //from java.util.List
        assertThat((resolveResults[1].element as PsiMethod?)!!.getSignature(PsiSubstitutor.EMPTY)).hasToString("MethodSignatureBackedByPsiMethod: add([PsiType:int, PsiType:E])") //from java.util.List
        assertThat((resolveResults[2].element as PsiMethod?)!!.getSignature(PsiSubstitutor.EMPTY)).hasToString("MethodSignatureBackedByPsiMethod: add([PsiType:E])") //from java.util.Collection
    }

    fun testNoMethodReferenceResult() {
        myFixture.configureByText("CallMatcherChecker.java",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class CallMatcherChecker {
                   CallMatcher callMatcher = CallMatcher.instanceCall("java.util.List", "asda<caret>sd");
                }
                """.trimIndent())
        val element = myFixture.file.findElementAt(myFixture.caretOffset)!!.parent
        val resolveResults = (element.references[0] as PsiPolyVariantReference).multiResolve(false)
        assertThat(resolveResults).isEmpty()
    }

    fun testMethodReferenceInExactClass() {
        myFixture.configureByText("CallMatcherChecker.java",
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
                """.trimIndent())
        val element = myFixture.file.findElementAt(myFixture.caretOffset)!!.parent
        val resolveResults = (element.references[0] as PsiPolyVariantReference).multiResolve(false)
        assertThat(resolveResults).hasSize(1)
        assertThat((resolveResults[0].element as PsiMethod?)!!.getSignature(PsiSubstitutor.EMPTY)).hasToString("MethodSignatureBackedByPsiMethod: someMethod([])")
    }

    fun testMethodReferenceInExactAndSuperClass() {
        myFixture.configureByText("CallMatcherChecker.java",
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
                """.trimIndent())
        val element = myFixture.file.findElementAt(myFixture.caretOffset)!!.parent
        val resolveResults = (element.references[0] as PsiPolyVariantReference).multiResolve(false)
        assertThat(resolveResults).hasSize(2)
        assertThat((resolveResults[0].element as PsiMethod?)!!.getSignature(PsiSubstitutor.EMPTY)).hasToString("MethodSignatureBackedByPsiMethod: someMethod([])")
        assertThat((resolveResults[1].element as PsiMethod?)!!.getSignature(PsiSubstitutor.EMPTY)).hasToString("MethodSignatureBackedByPsiMethod: someMethod([PsiType:int])")
    }

    fun testMethodReferenceForStaticMethod() {
        myFixture.configureByText("CallMatcherChecker.java",
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
                """.trimIndent())
        val element = myFixture.file.findElementAt(myFixture.caretOffset)!!.parent
        val resolveResults = (element.references[0] as PsiPolyVariantReference).multiResolve(false)
        assertThat(resolveResults).hasSize(1)
        assertThat((resolveResults[0].element as PsiMethod?)!!.getSignature(PsiSubstitutor.EMPTY)).hasToString("MethodSignatureBackedByPsiMethod: someMethod([])")
    }

    fun testMethodReferenceForStaticInterfaceMethod() {
        myFixture.configureByText("CallMatcherChecker.java",
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
                """.trimIndent())
        val element = myFixture.file.findElementAt(myFixture.caretOffset)!!.parent
        val resolveResults = (element.references[0] as PsiPolyVariantReference).multiResolve(false)
        assertThat(resolveResults).hasSize(1)
        assertThat((resolveResults[0].element as PsiMethod?)!!.getSignature(PsiSubstitutor.EMPTY)).hasToString("MethodSignatureBackedByPsiMethod: someMethod([])")
    }

    fun testMethodReferenceFoExternalStaticMethod() {
        myFixture.configureByText("CallMatcherChecker.java",
            """
                import com.siyeh.ig.callMatcher.CallMatcher;

                public class CallMatcherChecker {
                   CallMatcher callMatcher = CallMatcher.staticCall("java.lang.Integer", "toUnsignedSt<caret>ring");
                }
                """.trimIndent())
        val element = myFixture.file.findElementAt(myFixture.caretOffset)!!.parent
        val resolveResults = (element.references[0] as PsiPolyVariantReference).multiResolve(false)
        assertThat(resolveResults).hasSize(2)
        assertThat((resolveResults[0].element as PsiMethod?)!!.getSignature(PsiSubstitutor.EMPTY)).hasToString("MethodSignatureBackedByPsiMethod: toUnsignedString([PsiType:int, PsiType:int])")
        assertThat((resolveResults[1].element as PsiMethod?)!!.getSignature(PsiSubstitutor.EMPTY)).hasToString("MethodSignatureBackedByPsiMethod: toUnsignedString([PsiType:int])")
    }
}
