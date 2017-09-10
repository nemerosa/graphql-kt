package net.nemerosa.graphql.kotlin.spring

data class GraphQLRequest(
        val query: String,
        val variables: Map<String, Any>? = null,
        val operationName: String?
)
