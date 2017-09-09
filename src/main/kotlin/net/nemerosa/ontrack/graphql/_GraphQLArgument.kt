package net.nemerosa.ontrack.graphql

import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLArgument
import kotlin.reflect.KClass

class ReflectionArgument<A : Any>(
        private val cls: KClass<A>
) : Argument<A> {

    override val bindingArguments: List<GraphQLArgument>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun bindFrom(environment: DataFetchingEnvironment): A {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

fun <A : Any> KClass<A>.asArgument() = ReflectionArgument(this)

@Target(AnnotationTarget.PROPERTY)
@MustBeDocumented
annotation class ArgumentField(
        val description: String = ""
)
