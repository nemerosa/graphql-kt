package net.nemerosa.graphql.kotlin.spring

import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLSchema
import net.nemerosa.graphql.kotlin.core.RootQueryDef
import net.nemerosa.graphql.kotlin.core.TypeDef

open class DefaultGraphQLSchemaProvider(
        private val rootQueries: List<RootQueryDef<*>>,
        private val types: List<TypeDef<*>>
) : GraphQLSchemaProvider {

    override val schema: GraphQLSchema
        get() = GraphQLSchema.newSchema()
                .query(createQueryType())
                .build(
                        types.map { it.type.binding }.toSet()
                )

    private fun createQueryType(): GraphQLObjectType =
            GraphQLObjectType.newObject()
                    .name("Query")
                    .fields(rootQueries.map { it.field.binding })
                    .build()

}