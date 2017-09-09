package net.nemerosa.ontrack.model

class Branch(
        val project: Project,
        val id: Int,
        val name: String,
        val description: String?,
        val disabled: Boolean = false
)