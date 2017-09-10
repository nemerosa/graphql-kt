package net.nemerosa.ontrack.schema

import net.nemerosa.graphql.kotlin.core.Input
import net.nemerosa.graphql.kotlin.core.InputField

@Input
data class ProjectInput(
        @InputField("ID of the project - null for a creation")
        val id: Int?,
        @InputField("Name of the project - required")
        val name: String,
        @InputField("Description of the project")
        val description: String?,
        @InputField("State of the project")
        val disabled: Boolean?
)
