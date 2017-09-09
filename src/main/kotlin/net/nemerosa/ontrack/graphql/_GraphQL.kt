package net.nemerosa.ontrack.graphql

import kotlin.reflect.KClass

/**
 * Field
 */
interface Field<C : Any, F : Any> {
    val name: String
    val description: String?
}

abstract class AbstractField<C : Any, F : Any>(
        override val name: String,
        override val description: String? = null)
    : Field<C, F>

abstract class ScalarField<C : Any, F : Any>(
        name: String,
        description: String? = null,
        private val getter: C.() -> F
) : AbstractField<C, F>(name, description)

/**
 * Integer field
 */

class IntField<C : Any>(
        name: String,
        description: String? = null,
        getter: C.() -> Int
) : ScalarField<C, Int>(name, description, getter)

/**
 * Type
 */
class Type<C : Any>(
        val cls: KClass<C>
)

/**
 * Object type definition
 */
interface TypeDef<C : Any> {
    val type: Type<C>
}

/**
 * Root query definition
 */
interface RootQueryDef<F : Any> {
    val field: Field<Unit, F>
}

/**
 * Definition of a type
 */

class TypeBuilder<C : Any>(
        private val cls: KClass<C>
) {
    private val fields = mutableListOf<Field<C, *>>()

    fun field(field: Field<C, *>) {
        fields += field
    }

    val type: Type<C>
        get() = Type(cls)

}

inline fun <reified C : Any> objectType(init: TypeBuilder<C>.() -> Unit): Type<C> {
    val builder = TypeBuilder(C::class)
    builder.init()
    return builder.type
}

fun <C : Any> TypeBuilder<C>.fieldInt(name: String, description: String?, getter: C.() -> Int) {
    field(
            IntField<C>(
                    name,
                    description,
                    getter
            )
    )
}