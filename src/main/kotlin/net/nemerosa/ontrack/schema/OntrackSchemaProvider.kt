package net.nemerosa.ontrack.schema

import net.nemerosa.graphql.kotlin.core.QueryDef
import net.nemerosa.graphql.kotlin.core.TypeDef
import net.nemerosa.graphql.kotlin.spring.DefaultGraphQLSchemaProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class OntrackSchemaProvider
@Autowired
constructor(
        queries: List<QueryDef<*>>,
        types: List<TypeDef<*>>
) : DefaultGraphQLSchemaProvider(queries, types)
