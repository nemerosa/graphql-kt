package net.nemerosa.ontrack.graphql

import graphql.schema.GraphQLInputObjectType
import graphql.schema.GraphQLInputType
import graphql.schema.GraphQLNonNull
import org.junit.Test
import kotlin.reflect.full.cast
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

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

        assertEquals("name", b.name)
        assertEquals("Regular expression", b.description)
        assertEquals("String", b.type.name)
    }

    @Test
    fun `Composite type`() {
        val argument = Person::class.asArgument()
        val arguments = argument.bindingArguments
        assertEquals(2, arguments.size)

        val (address, name) = arguments

        assertEquals("name", name.name)
        assertNonNullType("String", name.type)

        assertEquals("address", address.name)
        val addressType = assertNonNullType("Address", address.type)

        assertTrue(addressType is GraphQLInputObjectType)
        val o = GraphQLInputObjectType::class.cast(addressType)
        assertEquals(2, o.fields.size)
        val (city, country) = o.fields

        assertEquals("city", city.name)
        assertNonNullType("String", city.type)

        assertEquals("country", country.name)
        assertEquals("String", country.type.name)

    }

    @Test
    fun `Simple input`() {
        val input = SimpleArg::class.getInputObjectValue(mapOf("name" to "Test"))
        assertEquals("Test", input.name)
    }

    @Test
    fun `Simple input with null`() {
        val input = SimpleArg::class.getInputObjectValue(mapOf())
        assertNull(input.name)
    }

    // TODO List

}

@Input
data class SimpleArg(val name: String?)

@Input
data class SimpleArgWithDescription(
        @InputField("Name of the argument")
        val name: String?
)

@Input
data class MultipleArgument(val id: Int?, @InputField("Regular expression") val name: String?)

@Input
data class Address(val city: String, val country: String?)
@Input
data class Person(val name: String, val address: Address)

fun assertNonNullType(expected: String, type: GraphQLInputType): GraphQLInputType {
    if (type is GraphQLNonNull) {
        assertEquals(expected, type.wrappedType.name)
        return type.wrappedType as GraphQLInputType
    } else {
        fail("$type is not a GraphQLNonNull")
    }
}