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
 * List field
 */

class ListField<C : Any, F : Any>(
        private val type: TypeReference<F>,
        name: String,
        description: String? = null,
        private val getter: C.() -> List<F>
) : AbstractField<C, F>(name, description)

/**
 * List field with arguments
 */

class ListWithArgumentField<C : Any, F : Any, A : Any>(
        private val type: TypeReference<F>,
        name: String,
        description: String? = null,
        argumentClass: KClass<A>,
        private val getter: (C, A) -> List<F>
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

/**
 * Field definitions in builders
 */

fun <C : Any> TypeBuilder<C>.fieldInt(name: String, description: String?, getter: C.() -> Int) {
    field(
            createFieldInt(
                    name,
                    description,
                    getter
            )
    )
}

fun <C : Any> TypeBuilder<C>.fieldString(name: String, description: String?, getter: C.() -> String) {
    field(
            createFieldString(
                    name,
                    description,
                    getter
            )
    )
}

fun <C : Any, F : Any> TypeBuilder<C>.fieldOf(type: TypeReference<F>, name: String, description: String?, getter: C.() -> F) {
    field(
            createFieldOf(
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

inline fun <C : Any, reified F : Any> TypeBuilder<C>.listOf(name: String, description: String?, noinline getter: C.() -> List<F>) {
    field(
            createListOf(
                    name,
                    description,
                    getter
            )
    )
}

inline fun <C : Any, reified F : Any, A : Any> TypeBuilder<C>.listOf(name: String, description: String?, argumentClass: KClass<A>, noinline getter: (C, A) -> List<F>) {
    field(
            createListOf(
                    name,
                    description,
                    argumentClass,
                    getter
            )
    )
}

/**
 * Field definitions
 */

fun <C : Any> createFieldInt(name: String, description: String?, getter: C.() -> Int) =
        IntField<C>(
                name,
                description,
                getter
        )

fun <C : Any> createFieldString(name: String, description: String?, getter: C.() -> String) =
        StringField<C>(
                name,
                description,
                getter
        )

fun <C : Any, F : Any> createFieldOf(type: TypeReference<F>, name: String, description: String?, getter: C.() -> F) =
        ObjectField<C, F>(
                type,
                name,
                description,
                getter
        )

inline fun <C : Any, reified F : Any> createFieldOf(name: String, description: String?, noinline getter: C.() -> F) =
        createFieldOf(
                typeRef<F>(),
                name,
                description,
                getter
        )

fun <C : Any, F : Any> createListOf(type: TypeReference<F>, name: String, description: String?, getter: C.() -> List<F>) =
        ListField<C, F>(
                type,
                name,
                description,
                getter
        )

inline fun <C : Any, reified F : Any> createListOf(name: String, description: String?, noinline getter: C.() -> List<F>) =
        createListOf(
                typeRef<F>(),
                name,
                description,
                getter
        )

fun <C : Any, F : Any, A : Any> createListOf(
        type: TypeReference<F>,
        name: String,
        description: String?,
        argumentClass: KClass<A>,
        getter: (C, A) -> List<F>
) =
        ListWithArgumentField<C, F, A>(
                type,
                name,
                description,
                argumentClass,
                getter
        )

inline fun <C : Any, reified F : Any, A : Any> createListOf(
        name: String,
        description: String?,
        argumentClass: KClass<A>,
        noinline getter: (C, A) -> List<F>
) =
        ListWithArgumentField<C, F, A>(
                typeRef<F>(),
                name,
                description,
                argumentClass,
                getter
        )
