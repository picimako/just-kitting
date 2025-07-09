//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting;

import org.jetbrains.kotlin.psi.KtFile;
import org.junit.jupiter.api.Test;

import static com.intellij.openapi.application.ReadAction.compute;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Functional test for {@link ServiceLevelDecider}.
 */
public final class ServiceLevelDeciderKotlinTest extends JustKittingTestBase {

    //By annotation

    @Test
    public void testProjectServiceForAnnotation() {
        KtFile psiFile = (KtFile) getFixture().configureByText("SomeService.kt",
            """
                import com.intellij.openapi.components.Service

                @Service(Service.Level.PROJECT)
                class SomeService {
                }
                """);

        var serviceLevel = ServiceLevelUtil.getServiceLevel(psiFile);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.PROJECT);
    }

    @Test
    public void testApplicationServiceForAnnotation() {
        KtFile psiFile = (KtFile) getFixture().configureByText("SomeService.kt",
            """
                import com.intellij.openapi.components.Service

                @Service(Service.Level.APP)
                class SomeService {
                }
                """);

        var serviceLevel = ServiceLevelUtil.getServiceLevel(psiFile);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.APP);
    }

    @Test
    public void testProjectAndApplicationServiceForAnnotation() {
        KtFile psiFile = (KtFile) getFixture().configureByText("SomeService.kt",
            """
                import com.intellij.openapi.components.Service

                @Service(Service.Level.PROJECT, Service.Level.APP)
                class SomeService {
                }
                """);

        var serviceLevel = ServiceLevelUtil.getServiceLevel(psiFile);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.PROJECT_AND_APP);
    }

    @Test
    public void testProjectAndApplicationServiceForAnnotationServiceLevelImported() {
        KtFile psiFile = (KtFile) getFixture().configureByText("SomeService.kt",
            """
                import com.intellij.openapi.components.Service
                import com.intellij.openapi.components.Service.Level

                @Service(Level.PROJECT, Level.APP)
                class SomeService {
                }
                """);

        var serviceLevel = ServiceLevelUtil.getServiceLevel(psiFile);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.PROJECT_AND_APP);
    }

    @Test
    public void testProjectAndApplicationServiceForAnnotationServiceLevelsImported() {
        KtFile psiFile = (KtFile) getFixture().configureByText("SomeService.kt",
            """
                import com.intellij.openapi.components.Service
                import com.intellij.openapi.components.Service.Level.PROJECT
                import com.intellij.openapi.components.Service.Level.APP

                @Service(PROJECT, APP)
                class SomeService {
                }
                """);

        var serviceLevel = ServiceLevelUtil.getServiceLevel(psiFile);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.PROJECT_AND_APP);
    }

    @Test
    public void testApplicationServiceForDefaultLevel() {
        KtFile psiFile = (KtFile) getFixture().configureByText("SomeService.kt",
            """
                import com.intellij.openapi.components.Service

                @Service
                class SomeService {
                }
                """);

        var serviceLevel = ServiceLevelUtil.getServiceLevel(psiFile);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.APP);
    }

    //Nested Kotlin classes

    @Test
    public void testServiceForNestedKotlinClass() {
        KtFile psiFile = (KtFile) getFixture().configureByText("AnApplicationServiceInNestedKotlinClass.kt",
            """
                import com.intellij.openapi.components.Service
                
                @Service
                class AnApplicationServiceInNestedKotlinClass {
                    @com.intellij.openapi.components.Service(Service.Level.PROJECT)
                    class NestedClass {
                    }
                }
                """);

        var serviceLevel = ServiceLevelDecider.getServiceLevel(compute(() -> psiFile.getClasses()[0].findInnerClassByName("NestedClass", false)));
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.PROJECT);
    }

    @Test
    public void testServiceForNestedKotlinClassInCompanionObject() {
        KtFile psiFile = (KtFile) getFixture().configureByText("AnApplicationServiceInNestedKotlinClassInCompanionObject.kt",
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
            compute(() -> psiFile.getClasses()[0].getInnerClasses()[0].findInnerClassByName("NestedClass", false)));
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.PROJECT);
    }

    //With multiple annotations

    @Test
    public void testServiceForClassWithOtherAnnotationsOnIt() {
        KtFile psiFile = (KtFile) getFixture().configureByText("AnApplicationServiceInNestedKotlinClass.kt",
            """
                import com.intellij.openapi.components.Service
                
                @Deprecated
                @com.intellij.openapi.components.Service(Service.Level.PROJECT)
                class AnApplicationServiceInNestedKotlinClass {
                }
                """);

        var serviceLevel = ServiceLevelUtil.getServiceLevel(psiFile);
        assertThat(serviceLevel).isSameAs(ServiceLevelDecider.ServiceLevel.PROJECT);
    }
}
