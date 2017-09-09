package net.nemerosa.ontrack.graphql

import kotlin.reflect.KClass

/**
 * Field
 */
interface Field<C : Any, F : Any>

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
    val type: Type<C>
        get() = Type(cls)

}

inline fun <reified C : Any> objectType(init: TypeBuilder<C>.() -> Unit): Type<C> {
    val builder = TypeBuilder(C::class)
    builder.init()
    return builder.type
}
