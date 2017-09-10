package net.nemerosa.ontrack.graphql

import graphql.Scalars
import graphql.schema.*
import kotlin.reflect.KClass
import kotlin.reflect.full.cast

/**
 * Field
 */
interface Field<C : Any, F> {
    val name: String
    val description: String?
    val binding: GraphQLFieldDefinition
        get() = GraphQLFieldDefinition.newFieldDefinition()
                .name(name)
                .description(description)
                .type(bindingType)
                // TODO Deprecation
                // Argument
                .argument(bindingArguments)
                // Data fetcher
                .dataFetcher(bindingGetter)
                .build()
    val bindingType: GraphQLOutputType
    val bindingArguments: List<GraphQLArgument>
    val bindingGetter: (DataFetchingEnvironment) -> Any?
}

abstract class AbstractField<C : Any, F>(
        private val containerClass: KClass<C>,
        override val name: String,
        override val description: String? = null)
    : Field<C, F> {
    override val bindingGetter: (DataFetchingEnvironment) -> Any?
        get() = {
            bindingGet(it, containerClass.cast(it.source))
        }

    override val bindingArguments: List<GraphQLArgument>
        get() = listOf()

    abstract fun bindingGet(environment: DataFetchingEnvironment, container: C): Any?
}

abstract class ScalarField<C : Any, F>(
        containerClass: KClass<C>,
        name: String,
        description: String? = null,
        private val getter: C.() -> F
) : AbstractField<C, F>(containerClass, name, description) {
    override fun bindingGet(environment: DataFetchingEnvironment, container: C) =
            container.getter()
}

/**
 * Object field
 */
class ObjectField<C : Any, F : Any>(
        private val containerClass: KClass<C>,
        private val type: TypeReference<F>,
        name: String,
        description: String? = null,
        private val getter: C.() -> F
) : AbstractField<C, F>(containerClass, name, description) {
    override val bindingType: GraphQLOutputType
        get() = GraphQLNonNull(GraphQLTypeReference(type.typeName))

    override fun bindingGet(environment: DataFetchingEnvironment, container: C) =
            container.getter()
}

/**
 * List field
 */

class ListField<C : Any, F : Any>(
        private val containerClass: KClass<C>,
        private val type: TypeReference<F>,
        name: String,
        description: String? = null,
        private val getter: C.() -> List<F>
) : AbstractField<C, F>(containerClass, name, description) {
    override val bindingType: GraphQLOutputType
        get() = GraphQLNonNull(GraphQLList(GraphQLNonNull(GraphQLTypeReference(type.typeName))))

    override fun bindingGet(environment: DataFetchingEnvironment, container: C) =
            container.getter()
}

/**
 * List field with arguments
 */

class ListWithArgumentField<C : Any, F : Any, A : Any>(
        private val containerClass: KClass<C>,
        private val type: TypeReference<F>,
        name: String,
        description: String? = null,
        private val argument: Argument<A>,
        private val getter: (C, A) -> List<F>
) : AbstractField<C, F>(containerClass, name, description) {
    override val bindingType: GraphQLOutputType
        get() = GraphQLNonNull(GraphQLList(GraphQLNonNull(GraphQLTypeReference(type.typeName))))

    override val bindingArguments: List<GraphQLArgument>
        get() = argument.bindingArguments

    override fun bindingGet(environment: DataFetchingEnvironment, container: C): List<F> {
        // Gets the argument from the environment
        val argumentValue: A = argument.bindFrom(environment)
        // Call
        return getter(container, argumentValue)
    }
}

/**
 * Integer field
 */

class IntField<C : Any>(
        private val containerClass: KClass<C>,
        name: String,
        description: String? = null,
        private val getter: C.() -> Int
) : ScalarField<C, Int>(containerClass, name, description, getter) {
    override val bindingType: GraphQLOutputType
        get() = GraphQLNonNull(Scalars.GraphQLInt)
}

/**
 * String field
 */

class StringField<C : Any>(
        private val containerClass: KClass<C>,
        name: String,
        description: String? = null,
        private val getter: C.() -> String
) : ScalarField<C, String>(containerClass, name, description, getter) {
    override val bindingType: GraphQLOutputType
        get() = GraphQLNonNull(Scalars.GraphQLString)
}


class NullableStringField<C : Any>(
        private val containerClass: KClass<C>,
        name: String,
        description: String? = null,
        private val getter: C.() -> String?
) : ScalarField<C, String?>(containerClass, name, description, getter) {
    override val bindingType: GraphQLOutputType
        get() = Scalars.GraphQLString
}


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
        private val cls: KClass<C>,
        private val description: String,
        private val fields: List<Field<C, *>>
) : TypeReference<C> {
    /**
     * Type name
     */
    override val typeName: String get() = cls.typeName
    /**
     * Binding
     */
    val binding: GraphQLObjectType
        get() =
            GraphQLObjectType.newObject()
                    .name(typeName)
                    .description(description)
                    .fields(
                            fields.map { it.binding }
                    )
                    .build()
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
interface RootQueryDef<F> {
    val field: Field<Unit, F>
}

/**
 * Argument interface
 */

interface Argument<A> {
    val bindingArguments: List<GraphQLArgument>
    fun bindFrom(environment: DataFetchingEnvironment): A
}

/**
 * Definition of a type
 */

class TypeBuilder<C : Any>(
        private val cls: KClass<C>,
        private val description: String
) {
    private val fields = mutableListOf<Field<C, *>>()

    fun field(field: Field<C, *>) {
        fields += field
    }

    val type: Type<C>
        get() = Type(cls, description, fields.toList())

}

inline fun <reified C : Any> objectType(description: String, init: TypeBuilder<C>.() -> Unit): Type<C> {
    val builder = TypeBuilder(C::class, description)
    builder.init()
    return builder.type
}

/**
 * Field definitions in builders
 */

inline fun <reified C : Any> TypeBuilder<C>.fieldInt(name: String, description: String?, noinline getter: C.() -> Int) {
    field(
            createFieldInt(
                    name,
                    description,
                    getter
            )
    )
}

inline fun <reified C : Any> TypeBuilder<C>.fieldString(name: String, description: String?, noinline getter: C.() -> String) {
    field(
            createFieldString(
                    name,
                    description,
                    getter
            )
    )
}

inline fun <reified C : Any> TypeBuilder<C>.fieldNullableString(name: String, description: String?, noinline getter: C.() -> String?) {
    field(
            createFieldNullableString(
                    name,
                    description,
                    getter
            )
    )
}

inline fun <reified C : Any, F : Any> TypeBuilder<C>.fieldOf(type: TypeReference<F>, name: String, description: String?, noinline getter: C.() -> F) {
    field(
            createFieldOf(
                    type,
                    name,
                    description,
                    getter
            )
    )
}

inline fun <reified C : Any, reified F : Any> TypeBuilder<C>.fieldOf(name: String, description: String?, noinline getter: C.() -> F) {
    fieldOf(
            typeRef<F>(),
            name,
            description,
            getter
    )
}

inline fun <reified C : Any, reified F : Any> TypeBuilder<C>.listOf(name: String, description: String?, noinline getter: C.() -> List<F>) {
    field(
            createListOf(
                    name,
                    description,
                    getter
            )
    )
}

inline fun <reified C : Any, reified F : Any, A : Any> TypeBuilder<C>.listOf(name: String, description: String?, argumentClass: KClass<A>, noinline getter: (C, A) -> List<F>) {
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

inline fun <reified C : Any> createFieldInt(name: String, description: String?, noinline getter: C.() -> Int) =
        IntField<C>(
                C::class,
                name,
                description,
                getter
        )

inline fun <reified C : Any> createFieldString(name: String, description: String?, noinline getter: C.() -> String) =
        StringField<C>(
                C::class,
                name,
                description,
                getter
        )

inline fun <reified C : Any> createFieldNullableString(name: String, description: String?, noinline getter: C.() -> String?) =
        NullableStringField<C>(
                C::class,
                name,
                description,
                getter
        )

inline fun <reified C : Any, F : Any> createFieldOf(type: TypeReference<F>, name: String, description: String?, noinline getter: C.() -> F) =
        ObjectField<C, F>(
                C::class,
                type,
                name,
                description,
                getter
        )

inline fun <reified C : Any, reified F : Any> createFieldOf(name: String, description: String?, noinline getter: C.() -> F) =
        createFieldOf(
                typeRef<F>(),
                name,
                description,
                getter
        )

inline fun <reified C : Any, F : Any> createListOf(type: TypeReference<F>, name: String, description: String?, noinline getter: C.() -> List<F>) =
        ListField<C, F>(
                C::class,
                type,
                name,
                description,
                getter
        )

inline fun <reified C : Any, reified F : Any> createListOf(name: String, description: String?, noinline getter: C.() -> List<F>) =
        createListOf(
                typeRef<F>(),
                name,
                description,
                getter
        )

inline fun <reified C : Any, F : Any, A : Any> createListOf(
        type: TypeReference<F>,
        name: String,
        description: String?,
        argument: Argument<A>,
        noinline getter: (C, A) -> List<F>
) =
        ListWithArgumentField<C, F, A>(
                C::class,
                type,
                name,
                description,
                argument,
                getter
        )

inline fun <reified C : Any, F : Any, A : Any> createListOf(
        type: TypeReference<F>,
        name: String,
        description: String?,
        argumentClass: KClass<A>,
        noinline getter: (C, A) -> List<F>
) =
        ListWithArgumentField<C, F, A>(
                C::class,
                type,
                name,
                description,
                argumentClass.asArgument(),
                getter
        )

inline fun <reified C : Any, reified F : Any, A : Any> createListOf(
        name: String,
        description: String?,
        argumentClass: KClass<A>,
        noinline getter: (C, A) -> List<F>
) =
        ListWithArgumentField<C, F, A>(
                C::class,
                typeRef<F>(),
                name,
                description,
                argumentClass.asArgument(),
                getter
        )
