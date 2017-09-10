package net.nemerosa.ontrack.graphql

import graphql.Scalars
import graphql.schema.GraphQLInputType
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf


fun <A : Any> KClass<A>.toInputType(): GraphQLInputType =
        when {
            this.isSubclassOf(String::class) -> Scalars.GraphQLString
            this.isSubclassOf(Int::class) -> Scalars.GraphQLInt
        // TODO All scalar types
        // TODO Conversion to input type
            else -> throw IllegalArgumentException("$qualifiedName cannot be converted to an input type")
        }