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

    val projects = mapOf(
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