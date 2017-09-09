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
 * Object field
 */
class ObjectField<C : Any, F : Any>(
        private val type: TypeReference<F>,
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
 * String field
 */

class StringField<C : Any>(
        name: String,
        description: String? = null,
        getter: C.() -> String
) : ScalarField<C, String>(name, description, getter)

/**
 * Type reference
 */
interface TypeReference<C : Any> {
    val typeName: String
}

val <C : Any> KClass<C>.typeName: String get() = simpleName!!

inline fun <reified C : Any> typeRef(): TypeReference<C> =
        object : TypeReference<C> {
            override val typeName: String
                get() = C::class.typeName
        }

/**
 * Type
 */
class Type<C : Any>(
        val cls: KClass<C>
) : TypeReference<C> {
    /**
     * Type name
     */
    override val typeName: String get() = cls.typeName
}

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

fun <C : Any> TypeBuilder<C>.fieldString(name: String, description: String?, getter: C.() -> String) {
    field(
            StringField<C>(
                    name,
                    description,
                    getter
            )
    )
}

fun <C : Any, F : Any> TypeBuilder<C>.fieldOf(type: TypeReference<F>, name: String, description: String?, getter: C.() -> F) {
    field(
            ObjectField<C, F>(
                    type,
                    name,
                    description,
                    getter
            )
    )
}

inline fun <C : Any, reified F : Any> TypeBuilder<C>.fieldOf(name: String, description: String?, noinline getter: C.() -> F) {
    fieldOf(
            typeRef<F>(),
            name,
            description,
            getter
    )
}
