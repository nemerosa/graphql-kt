package net.nemerosa.graphql.kotlin.spring

import graphql.schema.GraphQLSchema

interface GraphQLSchemaProvider {
    val schema: GraphQLSchema
}
