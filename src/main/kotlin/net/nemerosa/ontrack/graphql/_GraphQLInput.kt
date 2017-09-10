package net.nemerosa.ontrack.graphql

import graphql.Scalars
import graphql.schema.GraphQLInputObjectField
import graphql.schema.GraphQLInputObjectType
import graphql.schema.GraphQLInputType
import graphql.schema.GraphQLNonNull
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.cast
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure

@Target(AnnotationTarget.PROPERTY)
@MustBeDocumented
annotation class InputField(
        val description: String = ""
)

fun <A : Any> KClass<A>.toInputType(): GraphQLInputType =
        when {
            this.isSubclassOf(String::class) -> Scalars.GraphQLString
            this.isSubclassOf(Int::class) -> Scalars.GraphQLInt
        // TODO All scalar types
            else -> this.toInputObjectType()
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

fun <T : Any> KClass<T>.getInputValue(value: Any): Any =
        when {
            this.isSubclassOf(String::class) -> String::class.cast(value)
            this.isSubclassOf(Int::class) -> Int::class.cast(value)
            else -> this.getInputObjectValue(value)
        }

fun <T : Any> KClass<T>.getInputObjectValue(value: Any): T = TODO()