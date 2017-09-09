package net.nemerosa.ontrack.graphql

import org.junit.Test
import kotlin.test.assertEquals

class ArgumentTests {

    @Test
    fun `Simple argument`() {
        val argument = SimpleArg::class.asArgument()
        val arguments = argument.bindingArguments
        assertEquals(1, arguments.size)
        val a = arguments[0]
        assertEquals("name", a.name)
        assertEquals("", a.description)
        assertEquals("String", a.type.name)
    }

    @Test
    fun `Simple argument with description`() {
        val argument = SimpleArgWithDescription::class.asArgument()
        val arguments = argument.bindingArguments
        assertEquals(1, arguments.size)
        val a = arguments[0]
        assertEquals("name", a.name)
        assertEquals("Name of the argument", a.description)
        assertEquals("String", a.type.name)
    }

    @Test
    fun `Several arguments`() {
        val argument = MultipleArgument::class.asArgument()
        val arguments = argument.bindingArguments
        assertEquals(2, arguments.size)

        val (a, b) = arguments

        assertEquals("id", a.name)
        assertEquals("", a.description)
        assertEquals("Int", a.type.name)

        assertEquals("name", a.name)
        assertEquals("Regular expression", a.description)
        assertEquals("String", a.type.name)
    }

    // TODO Composite type

}

data class SimpleArg(val name: String?)

data class SimpleArgWithDescription(
        @ArgumentField("Name of the argument")
        val name: String?
)

data class MultipleArgument(val id: Int?, @ArgumentField("Regular expression") val name: String?)
