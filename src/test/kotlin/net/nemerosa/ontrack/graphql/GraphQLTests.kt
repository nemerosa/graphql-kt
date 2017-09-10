package net.nemerosa.ontrack.graphql

import net.nemerosa.graphql.kotlin.spring.DefaultGraphQLExecutor
import net.nemerosa.graphql.kotlin.spring.GraphQLExecutor
import net.nemerosa.graphql.kotlin.spring.GraphQLSchemaProvider
import net.nemerosa.graphql.kotlin.spring.toGraphQLRequest
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertEquals

@RunWith(SpringRunner::class)
@SpringBootTest
class GraphQLTests {

    @Autowired
    private lateinit var schemaProvider: GraphQLSchemaProvider

    private val executor: GraphQLExecutor = DefaultGraphQLExecutor()

    @Test
    fun `Gets all projects`() {
        val data = executor.requestAsJson(schemaProvider.schema, """
            |projects {
            |   id
            |   name
            |}""".trimMargin().toGraphQLRequest()
        )
        assertEquals(1, data.path("projects").size())
        assertEquals(1, data.path("projects")[0].path("id").asInt())
        assertEquals("ontrack", data.path("projects")[0].path("name").asText())
    }

}