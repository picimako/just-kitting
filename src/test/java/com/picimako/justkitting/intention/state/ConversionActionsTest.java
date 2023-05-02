//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.intention.state;

import com.picimako.justkitting.ThirdPartyLibraryLoader;
import com.picimako.justkitting.action.JustKittingActionTestBase;

/**
 * Functional test for {@link ConversionActions}.
 */
public class ConversionActionsTest extends JustKittingActionTestBase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ThirdPartyLibraryLoader.loadUtil(myFixture);
    }

    public void testConvertsClassWithStandaloneStateObject() {
        checkAction("SomeComponent.java", ConversionActions.WithStandaloneStateObject::new,
            "public final class SomeCom<caret>ponent {\n" +
                "}",
            "import com.intellij.openapi.components.PersistentStateComponent;\n" +
                "import com.intellij.openapi.components.State;\n" +
                "import com.intellij.openapi.components.Storage;\n" +
                "\n" +
                "@State(name = \"SomeComponent\", storages = @Storage(\"TODO: INSERT STORAGE NAME\"))\n" +
                "public final class SomeComponent implements PersistentStateComponent<SomeComponent.State> {\n" +
                "    private State myState = new State();\n" +
                "\n" +
                "    @Override\n" +
                "    public State getState() {\n" +
                "        return myState;\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void loadState(State state) {\n" +
                "        myState = state;\n" +
                "    }\n" +
                "\n" +
                "    static final class State {\n" +
                "\n" +
                "    }\n" +
                "}");
    }

    public void testConvertsClassWithSelfAsState() {
        checkAction("SomeComponent.java", ConversionActions.WithSelfAsState::new,
            "public final class SomeC<caret>omponent {\n" +
                "}",
            "import com.intellij.openapi.components.PersistentStateComponent;\n" +
                "import com.intellij.openapi.components.State;\n" +
                "import com.intellij.openapi.components.Storage;\n" +
                "import com.intellij.util.xmlb.XmlSerializerUtil;\n" +
                "\n" +
                "@State(name = \"SomeComponent\", storages = @Storage(\"TODO: INSERT STORAGE NAME\"))\n" +
                "public final class SomeComponent implements PersistentStateComponent<SomeComponent> {\n" +
                "    @Override\n" +
                "    public SomeComponent getState() {\n" +
                "        return this;\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void loadState(SomeComponent state) {\n" +
                "        XmlSerializerUtil.copyBean(state, this);\n" +
                "    }\n" +
                "}"
        );
    }
}
