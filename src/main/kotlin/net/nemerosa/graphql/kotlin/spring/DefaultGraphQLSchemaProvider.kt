package net.nemerosa.graphql.kotlin.spring

import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLSchema
import net.nemerosa.graphql.kotlin.core.QueryDef
import net.nemerosa.graphql.kotlin.core.TypeDef

open class DefaultGraphQLSchemaProvider(
        private val queries: List<QueryDef<*>>,
        private val types: List<TypeDef<*>>
) : GraphQLSchemaProvider {

    override val schema: GraphQLSchema
        get() = GraphQLSchema.newSchema()
                .query(createQueryType())
                .mutation(createMutationType())
                .build(
                        types.map { it.type.binding }.toSet()
                )

    private fun createMutationType(): GraphQLObjectType =
            GraphQLObjectType.newObject()
                    .name("Mutation")
                    // TODO Mutations
                    .build()

    private fun createQueryType(): GraphQLObjectType =
            GraphQLObjectType.newObject()
                    .name("Query")
                    .fields(queries.map { it.field.binding })
                    .build()

}