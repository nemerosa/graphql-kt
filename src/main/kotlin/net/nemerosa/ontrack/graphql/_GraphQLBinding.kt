package net.nemerosa.ontrack.graphql

import graphql.schema.GraphQLFieldDefinition

val <F> RootQueryDef<F>.graphQLField: GraphQLFieldDefinition
    get() = field.run {
        GraphQLFieldDefinition.newFieldDefinition()
                .name(name)
                .description(description)
                .build()
    }
