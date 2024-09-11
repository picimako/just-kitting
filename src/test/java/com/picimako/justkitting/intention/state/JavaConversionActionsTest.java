//Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.intention.state;

import com.picimako.justkitting.action.JustKittingActionTestBase;

/**
 * Functional test for {@link JavaConversionActions}.
 * <p>
 * NOTE: {@code shortenClassReferences()} is not applied in all cases because classes requiring app-client.jar are not available.
 * But, the effect can still be seen applied on {@code XmlSerializerUtil}
 */
public class JavaConversionActionsTest extends JustKittingActionTestBase {

    public void testConvertsClassWithStandaloneStateObject() {
        checkAction("SomeComponent.java", JavaConversionActions.WithStandaloneStateObject::new,
            "public final class SomeCom<caret>ponent {\n" +
            "}",
            """
                @com.intellij.openapi.components.State(name = "SomeComponent", storages = @com.intellij.openapi.components.Storage("<storage name>"))
                public final class SomeComponent implements com.intellij.openapi.components.PersistentStateComponent<SomeComponent.State> {
                    private State myState = new State();
                
                    @Override
                    public State getState() {
                        return myState;
                    }
                
                    @Override
                    public void loadState(State state) {
                        myState = state;
                    }
                
                    static final class State {
                
                    }
                }""");
    }

    public void testConvertsClassWithSelfAsState() {
        checkAction("SomeComponent.java", JavaConversionActions.WithSelfAsState::new,
            "public final class SomeC<caret>omponent {\n" +
            "}",
            """
                import com.intellij.util.xmlb.XmlSerializerUtil;
                
                @com.intellij.openapi.components.State(name = "SomeComponent", storages = @com.intellij.openapi.components.Storage("<storage name>"))
                public final class SomeComponent implements com.intellij.openapi.components.PersistentStateComponent<SomeComponent> {
                    @Override
                    public SomeComponent getState() {
                        return this;
                    }
                
                    @Override
                    public void loadState(SomeComponent state) {
                        XmlSerializerUtil.copyBean(state, this);
                    }
                }"""
        );
    }
}
