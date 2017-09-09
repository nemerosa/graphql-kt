package net.nemerosa.ontrack.model

class ProjectNotFoundException(id: Int): RuntimeException("Project ID not found: $id")
