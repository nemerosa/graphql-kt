package net.nemerosa.ontrack.schema

import net.nemerosa.graphql.kotlin.core.*
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
        get() = objectType("Branch associated to a project") {
            fieldInt("id", "Branch ID", Branch::id)
            fieldString("name", "Branch name", Branch::name)
            fieldNullableString("description", "Branch description", Branch::description)
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
        get() = objectType("Project") {
            fieldInt("id", "Project ID", Project::id)
            fieldString("name", "Project name", Project::name)
            fieldNullableString("description", "Project description", Project::description)
            // TODO fieldBoolean(Project::disabled)
            listOf(
                    typeRef<Branch>(),
                    "branches",
                    "Project branches",
                    ProjectBranchListArguments::class,
                    { p: Project, arg: ProjectBranchListArguments ->
                        if (arg.name != null) {
                            structureService.getBranchesByProject(p)
                                    .filter { it.name.matches(Regex(arg.name)) }
                        } else {
                            structureService.getBranchesByProject(p)
                        }
                    }
            )
            // List of extra fields
            fields(listOf(
                    createFieldInt<Project>("age", "Age based on ID...") { 17 + id }
            ))
        }

    data class ProjectBranchListArguments(
            val name: String?
    )
}

/**
 * List of projects
 */
@Component
class Projects
@Autowired
constructor
(
        private val structureService: StructureService
) : QueryDef<Project> {
    override val field: Field<Unit, Project>
        get() = createListOf(
                "projects",
                "List of projects",
                { structureService.getProjects() }
        )

}
