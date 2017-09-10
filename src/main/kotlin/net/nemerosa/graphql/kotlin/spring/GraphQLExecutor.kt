package net.nemerosa.graphql.kotlin.spring

import com.fasterxml.jackson.databind.JsonNode
import graphql.schema.GraphQLSchema

interface GraphQLExecutor {

    /**
     * Request execution (JSON)
     */
    fun requestAsJson(schema: GraphQLSchema, request: GraphQLRequest): JsonNode

}