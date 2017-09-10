package net.nemerosa.ontrack.service

import net.nemerosa.ontrack.model.Branch
import net.nemerosa.ontrack.model.Project
import net.nemerosa.ontrack.model.ProjectNotFoundException
import net.nemerosa.ontrack.model.StructureService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class StructureServiceImpl : StructureService {
    override fun saveProject(id: Int?, name: String, description: String?, disabled: Boolean?): Project {
        if (id == null) {
            // TODO Unique id
            val project = Project(0, name, description, disabled ?: false)
            projects[0] = project
            return project
        } else {
            val project = projects[id] ?: throw ProjectNotFoundException(id)
            // TODO Update
            projects[id] = project
            return project
        }
    }

    val projects = mutableMapOf(
            1 to Project(1, "ontrack", "Ontrack Core", false)
    )

    override fun getProjects(): List<Project> =
            projects.values.toList().sortedBy { it.name }

    override fun getProjectByID(id: Int): Project =
            projects[id] ?: throw ProjectNotFoundException(id)

    override fun findProjectByName(name: String): Project? =
            projects.values.find { it.name == name }

    override fun getBranchesByProject(project: Project): List<Branch> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}