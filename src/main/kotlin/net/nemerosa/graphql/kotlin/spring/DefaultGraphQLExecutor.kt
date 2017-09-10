package net.nemerosa.graphql.kotlin.spring

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import graphql.ExecutionInput
import graphql.ExecutionResult
import graphql.GraphQL
import graphql.schema.GraphQLSchema

class DefaultGraphQLExecutor : GraphQLExecutor {

    private val objectMapper: ObjectMapper = ObjectMapper()

    /**
     * Request execution (JSON)
     */
    override fun requestAsJson(schema: GraphQLSchema, request: GraphQLRequest): JsonNode {
        return objectMapper.valueToTree(
                request(schema, request)
        )
    }

    /**
     * Request execution
     */
    private fun request(schema: GraphQLSchema, request: GraphQLRequest): ExecutionResult {
        // TODO Execution strategy
        return GraphQL.newGraphQL(schema)
                .build()
                .execute(
                        ExecutionInput.newExecutionInput()
                                .query(request.query)
                                .operationName(request.operationName)
                                .variables(request.variables ?: mapOf())
                                .build()
                )

    }

}