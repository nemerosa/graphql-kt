package net.nemerosa.ontrack.schema

import net.nemerosa.ontrack.graphql.*
import net.nemerosa.ontrack.model.Branch
import net.nemerosa.ontrack.model.Project
import net.nemerosa.ontrack.model.StructureService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Branch type
 */
@Component
class TypeBranch
    : TypeDef<Branch> {
    override val type: Type<Branch>
        get() = objectType {
            fieldInt("id", "Branch ID", Branch::id)
            fieldString("name", "Branch name", Branch::name)
            // TODO fieldString(Branch::description)
            // TODO fieldBoolean(Branch::disabled)
            fieldOf("project", "Associated project", Branch::project)
        }
}

/**
 * Project type
 */
@Component
class TypeProject
@Autowired
constructor(
        private val structureService: StructureService
) : TypeDef<Project> {
    override val type: Type<Project>
        get() = objectType {
            fieldInt("id", "Project ID", Project::id)
            fieldString("name", "Project name", Project::name)
            // TODO fieldString(Project::description)
            // TODO fieldBoolean(Project::disabled)
            listOf(
                    typeRef<Branch>(),
                    "branches",
                    "Project branches",
                    ProjectBranchListArguments::class,
                    { p: Project, _: ProjectBranchListArguments -> structureService.getBranchesByProject(p) }
            )
        }

    data class ProjectBranchListArguments(
            val name: String?
    )
}

/**
 * List of projects
 */
@Component
class RootProjects
@Autowired
constructor
(
        private val structureService: StructureService
) : RootQueryDef<Project> {
    override val field: Field<Unit, Project>
        get() = createListOf(
                "projects",
                "List of projects",
                { structureService.getProjects() }
        )

}
