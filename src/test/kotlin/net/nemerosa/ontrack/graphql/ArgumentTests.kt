package net.nemerosa.ontrack.graphql

import graphql.schema.*
import net.nemerosa.graphql.kotlin.core.*
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
    fun `List type`() {
        val argument = Mailing::class.asArgument()
        val arguments = argument.bindingArguments
        assertEquals(2, arguments.size)

        val (addresses, subject) = arguments

        assertEquals("subject", subject.name)
        assertNonNullType("String", subject.type)

        assertEquals("addresses", addresses.name)
        assertListType("Address", addresses.type)
    }

    @Test
    fun `List input`() {
        val input = Mailing::class.getInputObjectValue(
                mapOf(
                        "subject" to "Test",
                        "addresses" to listOf(
                                mapOf(
                                        "city" to "Orléans",
                                        "country" to "France"
                                ),
                                mapOf(
                                        "city" to "Brussels",
                                        "country" to "Belgium"
                                )
                        )
                )
        )
        assertEquals(
                Mailing(
                        "Test",
                        listOf(
                                Address("Orléans", "France"),
                                Address("Brussels", "Belgium")
                        )
                ),
                input
        )
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

    @Test
    fun `Composite input`() {
        val input = Person::class.getInputObjectValue(mapOf(
                "name" to "Damien",
                "address" to mapOf(
                        "city" to "Brussels",
                        "country" to "Belgium"
                )
        ))
        assertEquals("Damien", input.name)
        assertEquals("Brussels", input.address.city)
        assertEquals("Belgium", input.address.country)
    }

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

@Input
data class Mailing(
        val subject: String,
        @InputField("List of addresses")
        @InputList(type = Address::class)
        val addresses: List<Address>
)

fun assertNonNullType(expected: String, type: GraphQLType): GraphQLInputType {
    if (type is GraphQLNonNull) {
        assertEquals(expected, type.wrappedType.name)
        return type.wrappedType as GraphQLInputType
    } else {
        fail("$type is not a GraphQLNonNull")
    }
}

fun assertListType(expected: String, type: GraphQLType) {
    if (type is GraphQLNonNull) {
        val listType = type.wrappedType
        if (listType is GraphQLList) {
            assertNonNullType(expected, listType.wrappedType)
        } else {
            fail("$type is not a GraphQLNonNull(GraphQLList)")
        }
    } else {
        fail("$type is not a GraphQLNonNull(GraphQLList)")
    }
}