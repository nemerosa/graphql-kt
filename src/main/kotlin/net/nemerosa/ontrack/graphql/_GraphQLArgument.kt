package net.nemerosa.ontrack.graphql

import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLArgument
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

class ReflectionArgument<A : Any>(
        private val cls: KClass<A>
) : Argument<A> {

    override val bindingArguments: List<GraphQLArgument> = cls.memberProperties.map {
        it.asArgument()
    }

    override fun bindFrom(environment: DataFetchingEnvironment): A =
            cls.getInputObjectValue(environment.arguments)

}

private fun <T, R> KProperty1<T, R>.asArgument(): GraphQLArgument =
        this.asField().run {
            GraphQLArgument.newArgument()
                    .name(name)
                    .description(description)
                    .type(type)
                    .build()
        }

fun <A : Any> KClass<A>.asArgument() = ReflectionArgument(this)
