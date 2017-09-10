package net.nemerosa.ontrack.model

interface StructureService {

    fun getProjects(): List<Project>

    fun getProjectByID(id: Int): Project

    fun findProjectByName(name: String): Project?

    fun getBranchesByProject(project: Project): List<Branch>

    fun saveProject(id: Int?, name: String, description: String?, disabled: Boolean?): Project

}