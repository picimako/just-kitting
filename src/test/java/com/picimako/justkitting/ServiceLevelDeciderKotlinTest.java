//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting;

import org.jetbrains.kotlin.psi.KtFile;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Functional test for {@link ServiceLevelDecider}.
 */
public class ServiceLevelDeciderKotlinTest extends JustKittingTestBase {

    //By annotation

    public void testProjectServiceForAnnotation() {
        KtFile psiFile = (KtFile) myFixture.configureByText("SomeService.kt",
            """
                import com.intellij.openapi.components.Service

                @Service(Service.Level.PROJECT)
                class SomeService {
                }
                """);

        var serviceLevel = ServiceLevelDecider.getServiceLevel(psiFile.getClasses()[0]);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.PROJECT);
    }

    public void testApplicationServiceForAnnotation() {
        KtFile psiFile = (KtFile) myFixture.configureByText("SomeService.kt",
            """
                import com.intellij.openapi.components.Service

                @Service(Service.Level.APP)
                class SomeService {
                }
                """);

        var serviceLevel = ServiceLevelDecider.getServiceLevel(psiFile.getClasses()[0]);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.APP);
    }

    public void testProjectAndApplicationServiceForAnnotation() {
        KtFile psiFile = (KtFile) myFixture.configureByText("SomeService.kt",
            """
                import com.intellij.openapi.components.Service

                @Service(Service.Level.PROJECT, Service.Level.APP)
                class SomeService {
                }
                """);

        var serviceLevel = ServiceLevelDecider.getServiceLevel(psiFile.getClasses()[0]);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.PROJECT_AND_APP);
    }

    public void testProjectAndApplicationServiceForAnnotationServiceLevelImported() {
        KtFile psiFile = (KtFile) myFixture.configureByText("SomeService.kt",
            """
                import com.intellij.openapi.components.Service
                import com.intellij.openapi.components.Service.Level

                @Service(Level.PROJECT, Level.APP)
                class SomeService {
                }
                """);

        var serviceLevel = ServiceLevelDecider.getServiceLevel(psiFile.getClasses()[0]);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.PROJECT_AND_APP);
    }

    public void testProjectAndApplicationServiceForAnnotationServiceLevelsImported() {
        KtFile psiFile = (KtFile) myFixture.configureByText("SomeService.kt",
            """
                import com.intellij.openapi.components.Service
                import com.intellij.openapi.components.Service.Level.PROJECT
                import com.intellij.openapi.components.Service.Level.APP

                @Service(PROJECT, APP)
                class SomeService {
                }
                """);

        var serviceLevel = ServiceLevelDecider.getServiceLevel(psiFile.getClasses()[0]);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.PROJECT_AND_APP);
    }

    //By class name

    public void testProjectServiceForClassName() {
        KtFile psiFile = (KtFile) myFixture.configureByText("AProjectService.kt",
            """
                import com.intellij.openapi.components.Service

                @Service
                class AProjectService {
                }
                """);

        var serviceLevel = ServiceLevelDecider.getServiceLevel(psiFile.getClasses()[0]);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.PROJECT);
    }

    public void testApplicationServiceForClassName() {
        KtFile psiFile = (KtFile) myFixture.configureByText("AnApplicationService.kt",
            """
                import com.intellij.openapi.components.Service

                @Service
                class AnApplicationService {
                }
                """);

        var serviceLevel = ServiceLevelDecider.getServiceLevel(psiFile.getClasses()[0]);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.APP);
    }

    //Nested Kotlin classes

    public void testServiceForNestedKotlinClass() {
        KtFile psiFile = (KtFile) myFixture.configureByText("AnApplicationServiceInNestedKotlinClass.kt",
            """
                import com.intellij.openapi.components.Service
                                
                @Service
                class AnApplicationServiceInNestedKotlinClass {
                    @com.intellij.openapi.components.Service(Service.Level.PROJECT)
                    class NestedClass {
                    }
                }
                """);

        var serviceLevel = ServiceLevelDecider.getServiceLevel(psiFile.getClasses()[0].findInnerClassByName("NestedClass", false));
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.PROJECT);
    }

    public void testServiceForNestedKotlinClassInCompanionObject() {
        KtFile psiFile = (KtFile) myFixture.configureByText("AnApplicationServiceInNestedKotlinClassInCompanionObject.kt",
            """
                import com.intellij.openapi.components.Service
                     
                @Service
                class AnApplicationServiceInNestedKotlinClassInCompanionObject {
                    companion object {
                        @Service(Service.Level.PROJECT)
                        internal class NestedClass {
                        }
                    }
                }
                """);

        var serviceLevel = ServiceLevelDecider.getServiceLevel(
            psiFile.getClasses()[0].getInnerClasses()[0].findInnerClassByName("NestedClass", false));
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.PROJECT);
    }

    //With multiple annotations

    public void testServiceForClassWithOtherAnnotationsOnIt() {
        KtFile psiFile = (KtFile) myFixture.configureByText("AnApplicationServiceInNestedKotlinClass.kt",
            """
                import com.intellij.openapi.components.Service
                
                @Deprecated
                @com.intellij.openapi.components.Service(Service.Level.PROJECT)
                class AnApplicationServiceInNestedKotlinClass {
                }
                """);

        var serviceLevel = ServiceLevelDecider.getServiceLevel(psiFile.getClasses()[0]);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.PROJECT);
    }

    //Not sure

    public void testNotSureWhatServiceLevel() {
        KtFile psiFile = (KtFile) myFixture.configureByText("SomeService.kt",
            """
                import com.intellij.openapi.components.Service

                @Service
                class SomeService {
                }
                """);

        var serviceLevel = ServiceLevelDecider.getServiceLevel(psiFile.getClasses()[0]);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.NOT_SURE);
    }
}
