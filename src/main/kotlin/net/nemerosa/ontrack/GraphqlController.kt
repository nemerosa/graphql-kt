package net.nemerosa.ontrack

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import graphql.ExecutionResult
import graphql.GraphQL
import graphql.schema.GraphQLSchema
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.io.IOException

@Transactional
@RestController
@RequestMapping("/graphql")
class GraphqlController
@Autowired
constructor(
        private val schema: GraphQLSchema
) {

    private val objectMapper: ObjectMapper = ObjectMapper()

    /**
     * Request model
     */
    data class Request(
            val query: String,
            val variables: Map<String, Any>? = null,
            val operationName: String?
    )

    /**
     * GET end point
     */
    @GetMapping
    @Transactional
    @Throws(IOException::class)
    fun get(
            @RequestParam query: String,
            @RequestParam(required = false) variables: String,
            @RequestParam(required = false) operationName: String
    ): ResponseEntity<JsonNode> {
        // Parses the arguments
        val arguments = decodeIntoMap(variables)
        // Runs the query
        return ResponseEntity.ok(
                requestAsJson(
                        Request(
                                query,
                                arguments,
                                operationName
                        )
                )
        )
    }

    /**
     * POST end point
     */
    @PostMapping
    @Transactional
    @Throws(IOException::class)
    fun post(@RequestBody input: String): ResponseEntity<JsonNode> {
        // Gets the components
        val request = objectMapper.readValue(input, Request::class.java)!!
        // Runs the query
        return ResponseEntity.ok(
                requestAsJson(request)
        )
    }

    /**
     * Request execution (JSON)
     */
    fun requestAsJson(request: Request): JsonNode {
        return objectMapper.valueToTree(
                request(request)
        )

    }

    /**
     * Request execution
     */
    fun request(request: Request): ExecutionResult {
        // TODO Execution strategy
        return GraphQL(schema).execute(
                request.query,
                request.operationName,
                null, // No context
                request.variables ?: mapOf()
        )

    }

    @Throws(IOException::class)
    private fun decodeIntoMap(variablesParam: String): Map<String, Any> {
        return if (StringUtils.isNotBlank(variablesParam)) {
            @Suppress("UNCHECKED_CAST")
            objectMapper.readValue(variablesParam, Map::class.java) as Map<String, Any>
        } else {
            emptyMap()
        }
    }

}
