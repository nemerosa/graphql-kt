package net.nemerosa.ontrack.model

class Project(
        val id: Int,
        val name: String,
        val description: String?,
        val disabled: Boolean = false
)
