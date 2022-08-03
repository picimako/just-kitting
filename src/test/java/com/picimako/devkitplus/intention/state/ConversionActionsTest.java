//Copyright 2021 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.devkitplus.intention.state;

import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.picimako.devkitplus.action.DevKitPlusActionTestBase;

/**
 * Functional test for {@link ConversionActions}.
 */
public class ConversionActionsTest extends DevKitPlusActionTestBase {

    @Override
    protected void tuneFixture(JavaModuleFixtureBuilder<?> moduleBuilder) throws Exception {
        super.tuneFixture(moduleBuilder);
        loadUtilJar(moduleBuilder);
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
                "    public State getState() {\n" +
                "        return myState;\n" +
                "    }\n" +
                "\n" +
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
                "    public SomeComponent getState() {\n" +
                "        return this;\n" +
                "    }\n" +
                "\n" +
                "    public void loadState(SomeComponent state) {\n" +
                "        XmlSerializerUtil.copyBean(state, this);\n" +
                "    }\n" +
                "}"
        );
    }
}
