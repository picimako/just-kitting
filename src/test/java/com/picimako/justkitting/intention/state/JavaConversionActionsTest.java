//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.intention.state;

import com.picimako.justkitting.ThirdPartyLibraryLoader;
import com.picimako.justkitting.action.JustKittingActionTestBase;

/**
 * Functional test for {@link JavaConversionActions}.
 */
public class JavaConversionActionsTest extends JustKittingActionTestBase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ThirdPartyLibraryLoader.loadAppClient(myFixture);
    }

    public void testConvertsClassWithStandaloneStateObject() {
        checkAction("SomeComponent.java", JavaConversionActions.WithStandaloneStateObject::new,
            "public final class SomeCom<caret>ponent {\n" +
                "}",
            """
                import com.intellij.openapi.components.PersistentStateComponent;
                import com.intellij.openapi.components.State;
                import com.intellij.openapi.components.Storage;

                @State(name = "SomeComponent", storages = @Storage("<storage name>"))
                public final class SomeComponent implements PersistentStateComponent<SomeComponent.State> {
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
                import com.intellij.openapi.components.PersistentStateComponent;
                import com.intellij.openapi.components.State;
                import com.intellij.openapi.components.Storage;
                import com.intellij.util.xmlb.XmlSerializerUtil;

                @State(name = "SomeComponent", storages = @Storage("<storage name>"))
                public final class SomeComponent implements PersistentStateComponent<SomeComponent> {
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
