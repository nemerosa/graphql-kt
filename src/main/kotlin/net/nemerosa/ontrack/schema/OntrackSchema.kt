package net.nemerosa.ontrack.schema

import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLSchema
import net.nemerosa.ontrack.graphql.RootQueryDef
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OntrackSchema
@Autowired
constructor(
        private val rootQueries: List<RootQueryDef<*>>
) {

    @Bean
    fun schema(): GraphQLSchema = GraphQLSchema.newSchema()
            .query(createQueryType())
            .build()

    private fun createQueryType(): GraphQLObjectType =
            GraphQLObjectType.newObject()
                    .name("Query")
                    .fields(rootQueries.map { it.field.binding })
                    .build()

}