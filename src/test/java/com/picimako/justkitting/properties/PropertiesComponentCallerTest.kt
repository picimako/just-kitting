//Copyright 2026 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.properties

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.service
import com.picimako.justkitting.action.JustKittingActionTestBase
import com.picimako.justkitting.resources.JustKittingBundle.message
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Integration test for [PropertiesComponentCaller].
 */
class PropertiesComponentCallerTest : JustKittingActionTestBase() {

    //Query string

    @Test
    fun `queries empty string`() {
        //Given
        val propertiesComponent = PropertiesComponent.getInstance(project)
        propertiesComponent.setValue("key", "")
        configureSettings(Operation.QUERY, ValueType.STRING, "key", "unused")

        //When
        val result = TestPropertiesComponentCaller(propertiesComponent).call()

        //Then
        assertThat(result).isEmpty()
    }

    @Test
    fun `queries one-line string`() {
        //Given
        val propertiesComponent = PropertiesComponent.getInstance(project)
        propertiesComponent.setValue("key", "value")
        configureSettings(Operation.QUERY, ValueType.STRING, "key", "unused")

        //When
        val result = TestPropertiesComponentCaller(propertiesComponent).call()

        //Then
        assertThat(result).isEqualTo("value")
    }

    @Test
    fun `queries multi-line string`() {
        //Given
        val propertiesComponent = PropertiesComponent.getInstance(project)
        propertiesComponent.setValue("key", "value\non\nmultiple\nlines")
        configureSettings(Operation.QUERY, ValueType.STRING, "key", "unused")

        //When
        val result = TestPropertiesComponentCaller(propertiesComponent).call()

        //Then
        assertThat(result).isEqualTo("value\non\nmultiple\nlines")
    }

    @Test
    fun `returns null text when queried string does not exist`() {
        //Given
        configureSettings(Operation.QUERY, ValueType.STRING, "key", "unused")

        //When
        val result = TestPropertiesComponentCaller(PropertiesComponent.getInstance(project)).call()

        //Then
        assertThat(result).isEqualTo("<null>")
    }

    @Test
    fun `returns error message when string query fails`() {
        //Given
        configureSettings(Operation.QUERY, ValueType.STRING, "key", "unused")

        //When
        val result = TestPropertiesComponentCaller(ExceptionThrowingPropertiesComponent()).call()

        //Then
        assertThat(result).isEqualTo(message("pc.execution.failed.result"))
    }

    //Query string list

    @Test
    fun `queries empty string list`() {
        //Given
        val propertiesComponent = PropertiesComponent.getInstance(project)
        propertiesComponent.setList("key", emptyList())
        configureSettings(Operation.QUERY, ValueType.STRING_LIST, "key", "unused")

        //When
        val result = TestPropertiesComponentCaller(propertiesComponent).call()

        //Then
        assertThat(result).isEqualTo("<empty list>")
    }

    @Test
    fun `queries one-line string list`() {
        //Given
        val propertiesComponent = PropertiesComponent.getInstance(project)
        propertiesComponent.setList("key", listOf("value"))
        configureSettings(Operation.QUERY, ValueType.STRING_LIST, "key", "unused")

        //When
        val result = TestPropertiesComponentCaller(propertiesComponent).call()

        //Then
        assertThat(result).isEqualTo("[value]")
    }

    @Test
    fun `queries multi-line string list`() {
        //Given
        val propertiesComponent = PropertiesComponent.getInstance(project)
        propertiesComponent.setList("key", listOf("first", "second", "third"))
        configureSettings(Operation.QUERY, ValueType.STRING_LIST, "key", "unused")

        //When
        val result = TestPropertiesComponentCaller(propertiesComponent).call()

        //Then
        assertThat(result).isEqualTo("[first\nsecond\nthird]")
    }

    @Test
    fun `returns error message when string list query fails`() {
        //Given
        configureSettings(Operation.QUERY, ValueType.STRING_LIST, "key", "unused")

        //When
        val result = TestPropertiesComponentCaller(ExceptionThrowingPropertiesComponent()).call()

        //Then
        assertThat(result).isEqualTo(message("pc.execution.failed.result"))
    }

    @Test
    fun `returns null text when queried string list does not exist`() {
        //Given
        configureSettings(Operation.QUERY, ValueType.STRING_LIST, "key", "unused")

        //When
        val result = TestPropertiesComponentCaller(PropertiesComponent.getInstance(project)).call()

        //Then
        assertThat(result).isEqualTo("<null>")
    }

    //Set string

    @Test
    fun `sets empty string`() {
        //Given
        val propertiesComponent = PropertiesComponent.getInstance(project)
        configureSettings(Operation.SET, ValueType.STRING, "key", "")

        //When
        val result = TestPropertiesComponentCaller(propertiesComponent).call()

        //Then
        assertThat(result).isEmpty()
        assertThat(propertiesComponent.getValue("key")).isEmpty()
    }

    @Test
    fun `sets one-line string`() {
        //Given
        val propertiesComponent = PropertiesComponent.getInstance(project)
        configureSettings(Operation.SET, ValueType.STRING, "key", "value")

        //When
        val result = TestPropertiesComponentCaller(propertiesComponent).call()

        //Then
        assertThat(result).isEmpty()
        assertThat(propertiesComponent.getValue("key")).isEqualTo("value")
    }

    @Test
    fun `sets multi-line string`() {
        //Given
        val propertiesComponent = PropertiesComponent.getInstance(project)
        configureSettings(Operation.SET, ValueType.STRING, "key", "first\nsecond\nthird")

        //When
        val result = TestPropertiesComponentCaller(propertiesComponent).call()

        //Then
        assertThat(result).isEmpty()
        assertThat(propertiesComponent.getValue("key")).isEqualTo("first\nsecond\nthird")
    }

    @Test
    fun `returns error message when setting string fails`() {
        //Given
        configureSettings(Operation.SET, ValueType.STRING, "key", "value")

        //When
        val result = TestPropertiesComponentCaller(ExceptionThrowingPropertiesComponent()).call()

        //Then
        assertThat(result).isEqualTo(message("pc.execution.failed.result"))
    }

    //Set string list

    @Test
    fun `sets empty string list`() {
        //Given
        val propertiesComponent = PropertiesComponent.getInstance(project)
        configureSettings(Operation.SET, ValueType.STRING_LIST, "key", "")

        //When
        val result = TestPropertiesComponentCaller(propertiesComponent).call()

        //Then
        assertThat(result).isEmpty()
        assertThat(propertiesComponent.getList("key")).isEmpty()
    }

    @Test
    fun `sets one-line string list`() {
        //Given
        val propertiesComponent = PropertiesComponent.getInstance(project)
        configureSettings(Operation.SET, ValueType.STRING_LIST, "key", "value")

        //When
        val result = TestPropertiesComponentCaller(propertiesComponent).call()

        //Then
        assertThat(result).isEmpty()
        assertThat(propertiesComponent.getList("key")).containsExactly("value")
    }

    @Test
    fun `sets multi-line string list`() {
        //Given
        val propertiesComponent = PropertiesComponent.getInstance(project)
        configureSettings(Operation.SET, ValueType.STRING_LIST, "key", "first\nsecond\nthird")

        //When
        val result = TestPropertiesComponentCaller(propertiesComponent).call()

        //Then
        assertThat(result).isEmpty()
        assertThat(propertiesComponent.getList("key")).containsExactly("first", "second", "third")
    }

    @Test
    fun `returns error message when setting string list fails`() {
        //Given
        configureSettings(Operation.SET, ValueType.STRING_LIST, "key", "first\nsecond")

        //When
        val result = TestPropertiesComponentCaller(ExceptionThrowingPropertiesComponent()).call()

        //Then
        assertThat(result).isEqualTo(message("pc.execution.failed.result"))
    }

    //Delete string

    @Test
    fun `deletes string`() {
        //Given
        val propertiesComponent = PropertiesComponent.getInstance(project)
        propertiesComponent.setValue("key", "value")
        configureSettings(Operation.DELETE, ValueType.STRING, "key", "unused")

        //When
        val result = TestPropertiesComponentCaller(propertiesComponent).call()

        //Then
        assertThat(result).isEmpty()
        assertThat(propertiesComponent.getValue("key")).isNull()
    }

    @Test
    fun `returns error message when deleting string fails`() {
        //Given
        configureSettings(Operation.DELETE, ValueType.STRING, "key", "unused")

        //When
        val result = TestPropertiesComponentCaller(ExceptionThrowingPropertiesComponent()).call()

        //Then
        assertThat(result).isEqualTo(message("pc.execution.failed.result"))
    }

    //Delete string list

    @Test
    fun `deletes string list`() {
        //Given
        val propertiesComponent = PropertiesComponent.getInstance(project)
        propertiesComponent.setList("key", listOf("first", "second"))
        configureSettings(Operation.DELETE, ValueType.STRING_LIST, "key", "unused")

        //When
        val result = TestPropertiesComponentCaller(propertiesComponent).call()

        //Then
        assertThat(result).isEmpty()
        assertThat(propertiesComponent.getList("key")).isNull()
    }

    @Test
    fun `returns error message when deleting string list fails`() {
        //Given
        configureSettings(Operation.DELETE, ValueType.STRING_LIST, "key", "unused")

        //When
        val result = TestPropertiesComponentCaller(ExceptionThrowingPropertiesComponent()).call()

        //Then
        assertThat(result).isEqualTo(message("pc.execution.failed.result"))
    }

    //Helpers

    private fun configureSettings(operation: Operation, valueType: ValueType, key: String, value: String) {
        project.service<PropertiesComponentSettings>().apply {
            this.operation = operation
            this.valueType = valueType
            this.key = key
            this.value = value
        }
    }

    private inner class TestPropertiesComponentCaller(
        private val propertiesComponent: PropertiesComponent
    ) : PropertiesComponentCaller(project) {

        override fun getPropertiesComponent(): PropertiesComponent = propertiesComponent
    }

    private class ExceptionThrowingPropertiesComponent : PropertiesComponent() {
        override fun isValueSet(name: String) = throwPropertiesComponentFailure()
        override fun getValue(name: String) = throwPropertiesComponentFailure()
        override fun setValue(name: String, value: String?) = throwPropertiesComponentFailure()
        override fun setValue(name: String, value: String?, defaultValue: String?) = throwPropertiesComponentFailure()
        override fun setValue(name: String, value: Float, defaultValue: Float) = throwPropertiesComponentFailure()
        override fun setValue(name: String, value: Int, defaultValue: Int) = throwPropertiesComponentFailure()
        override fun setValue(name: String, value: Boolean, defaultValue: Boolean) = throwPropertiesComponentFailure()
        override fun unsetValue(name: String) = throwPropertiesComponentFailure()

        @Deprecated("Deprecated in Java")
        override fun getValues(name: String) = throwPropertiesComponentFailure()

        @Deprecated("Deprecated in Java")
        override fun setValues(name: String, values: Array<String>?) = throwPropertiesComponentFailure()
        override fun getList(name: String) = throwPropertiesComponentFailure()
        override fun setList(name: String, values: MutableCollection<String>?) = throwPropertiesComponentFailure()
        override fun updateValue(name: String, value: Boolean) = throwPropertiesComponentFailure()

        private fun throwPropertiesComponentFailure(): Nothing = throw RuntimeException("PropertiesComponent failure")
    }
}