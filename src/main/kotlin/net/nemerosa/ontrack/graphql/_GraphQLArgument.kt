package net.nemerosa.ontrack.graphql

import kotlin.reflect.KClass

class ReflectionArgument<A : Any>(
        private val cls: KClass<A>
) : Argument<A> {

}

fun <A : Any> KClass<A>.asArgument() = ReflectionArgument(this)
