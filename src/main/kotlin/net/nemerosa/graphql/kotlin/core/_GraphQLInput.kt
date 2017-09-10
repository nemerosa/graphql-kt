package net.nemerosa.graphql.kotlin.core

import graphql.Scalars
import graphql.schema.*
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmErasure

@Target(AnnotationTarget.PROPERTY)
@MustBeDocumented
annotation class InputField(
        val description: String = ""
)

@Target(AnnotationTarget.PROPERTY)
@MustBeDocumented
annotation class InputList(
        val type: KClass<*>
)

@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class Input(
        val description: String = ""
)

fun <A : Any> KClass<A>.toInputType(): GraphQLInputType =
        when {
            this.isSubclassOf(String::class) -> Scalars.GraphQLString
            this.isSubclassOf(Int::class) -> Scalars.GraphQLInt
            this.isSubclassOf(Boolean::class) -> Scalars.GraphQLBoolean
        // TODO All scalar types
            this.findAnnotation<Input>() != null -> this.toInputObjectType()
            else -> throw IllegalArgumentException("Cannot convert $qualifiedName to input type")
        }

fun <A : Any> KClass<A>.toInputObjectType(): GraphQLInputObjectType {
    val input = findAnnotation<Input>() ?: throw IllegalArgumentException("$qualifiedName must be annotated with @${Input::class.simpleName}")
    return GraphQLInputObjectType.newInputObject()
            .name(typeName)
            .description(input.description)
            .fields(memberProperties.map { it.asField() })
            .build()
}


fun <T, R> KProperty1<T, R>.asField(): GraphQLInputObjectField {
    val f = GraphQLInputObjectField.newInputObjectField()
    // Annotation
    val annotation: InputField? = findAnnotation()
    // Name = property name
    f.name(name)
    // Description
    f.description(annotation?.description ?: "")
    // List?
    if (returnType.jvmErasure.isSubclassOf(List::class)) {
        // @InputList annotation is required because the type of element is erased
        val listAnnotation = getPropertyInputListAnnotation()
        val elementType = listAnnotation.type.toInputType()
        f.type(GraphQLNonNull(GraphQLList(GraphQLNonNull(elementType))))
    }
    // Any other type
    else {
        val type = returnType.jvmErasure.toInputType()
        if (returnType.isMarkedNullable) {
            f.type(type)
        } else {
            f.type(GraphQLNonNull(type))
        }
    }
    // OK
    return f.build()
}

private fun <R, T> KProperty1<T, R>.getPropertyInputListAnnotation(): InputList {
    return findAnnotation<InputList>() ?: throw IllegalArgumentException(
            """$name is a list and must be annotated
                    |with @${InputList::class.simpleName} because
                    |the type of element is erased at compilation time.""".trimMargin())
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> KClass<T>.getInputValue(value: Any): Any =
        when {
            this.isSubclassOf(String::class) -> String::class.cast(value)
            this.isSubclassOf(Int::class) -> Int::class.cast(value)
            this.isSubclassOf(Boolean::class) -> Boolean::class.cast(value)
            findAnnotation<Input>() != null -> this.getInputObjectValue(value as Map<String, Any>)
            else -> throw IllegalArgumentException("Cannot convert input from $qualifiedName")
        }

fun <T : Any> KClass<T>.getInputObjectValue(value: Map<String, Any>): T {
    if (findAnnotation<Input>() == null) {
        throw IllegalArgumentException("$qualifiedName must be annotated with @${Input::class.simpleName}")
    }
    val constructor = primaryConstructor!!
    val inputs: List<Any?> = constructor.parameters.map {
        getInputValueForParameter(it, value)
    }
    // Call
    return constructor.call(*inputs.toTypedArray())
}

private fun <T : Any> KClass<T>.getInputValueForParameter(parameter: KParameter, value: Map<String, Any>): Any? {
    val name = parameter.name!!
    val type = parameter.type
    val actualValue: Any? = value[name]
    // Gets the corresponding property
    val property = memberProperties.find { it.name == name }
            ?: throw IllegalStateException("Could not find $name property in $qualifiedName")
    // If list property?
    if (property.returnType.jvmErasure.isSubclassOf(List::class)) {
        // Gets the (required) annotation
        val listAnnotation = property.getPropertyInputListAnnotation()
        // Element type
        val elementType = listAnnotation.type
        // Value must be a list
        if (actualValue is List<*>) {
            val elements = actualValue.map { inputValue ->
                elementType.getInputValue(inputValue ?: throw IllegalArgumentException("All elements for $name in $qualifiedName must be non null"))
            }
            return elements
        } else {
            throw IllegalArgumentException("$name in $qualifiedName must be created from a list")
        }
    } else {
        if (actualValue == null) {
            if (type.isMarkedNullable) {
                return null
            } else {
                throw IllegalArgumentException("$name is null but is not marked as nullable")
            }
        } else {
            return type.jvmErasure.getInputValue(actualValue)
        }
    }
}