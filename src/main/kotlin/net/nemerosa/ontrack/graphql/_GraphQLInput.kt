package net.nemerosa.ontrack.graphql

import graphql.Scalars
import graphql.schema.GraphQLInputObjectField
import graphql.schema.GraphQLInputObjectType
import graphql.schema.GraphQLInputType
import graphql.schema.GraphQLNonNull
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmErasure

@Target(AnnotationTarget.PROPERTY)
@MustBeDocumented
annotation class InputField(
        val description: String = ""
)

@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class Input

fun <A : Any> KClass<A>.toInputType(): GraphQLInputType =
        when {
            this.isSubclassOf(String::class) -> Scalars.GraphQLString
            this.isSubclassOf(Int::class) -> Scalars.GraphQLInt
        // TODO All scalar types
            this.findAnnotation<Input>() != null -> this.toInputObjectType()
            else -> throw IllegalArgumentException("Cannot convert $qualifiedName to input type")
        }

fun <A : Any> KClass<A>.toInputObjectType(): GraphQLInputObjectType =
        GraphQLInputObjectType.newInputObject()
                .name(typeName)
                // TODO Description from annotation
                .fields(memberProperties.map { it.asField() })
                .build()


private fun <T, R> KProperty1<T, R>.asField(): GraphQLInputObjectField {
    val f = GraphQLInputObjectField.newInputObjectField()
    // Annotation
    val annotation: InputField? = findAnnotation()
    // Name = property name
    f.name(name)
    // Description
    f.description(annotation?.description ?: "")
    // Type
    val type = returnType.jvmErasure.toInputType()
    if (returnType.isMarkedNullable) {
        f.type(type)
    } else {
        f.type(GraphQLNonNull(type))
    }
    // OK
    return f.build()
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> KClass<T>.getInputValue(value: Any): Any =
        when {
            this.isSubclassOf(String::class) -> String::class.cast(value)
            this.isSubclassOf(Int::class) -> Int::class.cast(value)
            findAnnotation<Input>() != null -> this.getInputObjectValue(value as Map<String, Any>)
            else -> throw IllegalArgumentException("Cannot convert input from $qualifiedName")
        }

fun <T : Any> KClass<T>.getInputObjectValue(value: Map<String, Any>): T {
    if (findAnnotation<Input>() == null) {
        throw IllegalArgumentException("$qualifiedName must be annotated with @${Input::class.simpleName}")
    }
    val constructor = primaryConstructor!!
    val inputs: List<Any?> = constructor.parameters.map {
        val name = it.name!!
        val type = it.type
        val actualValue: Any? = value[name]
        if (actualValue == null) {
            if (type.isMarkedNullable) {
                null
            } else {
                throw IllegalArgumentException("$name is null but is not marked as nullable")
            }
        } else {
            type.jvmErasure.getInputValue(actualValue)
        }
    }
    // Call
    return constructor.call(*inputs.toTypedArray())
}