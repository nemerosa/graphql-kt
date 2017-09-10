package net.nemerosa.ontrack.graphql

import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLNonNull
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure

class ReflectionArgument<A : Any>(
        private val cls: KClass<A>
) : Argument<A> {

    override val bindingArguments: List<GraphQLArgument> = cls.memberProperties.map {
        it.asArgument()
    }

    override fun bindFrom(environment: DataFetchingEnvironment): A =
            cls.getInputObjectValue(environment.arguments)

}

private fun <T, R> KProperty1<T, R>.asArgument(): GraphQLArgument {
    val a = GraphQLArgument.newArgument()
    // Annotation
    val annotation: InputField? = findAnnotation()
    // Name = property name
    a.name(name)
    // Description
    a.description(annotation?.description ?: "")
    // Type
    val type = returnType.jvmErasure.toInputType()
    if (returnType.isMarkedNullable) {
        a.type(type)
    } else {
        a.type(GraphQLNonNull(type))
    }
    // OK
    return a.build()
}

fun <A : Any> KClass<A>.asArgument() = ReflectionArgument(this)
