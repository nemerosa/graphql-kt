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
        get() = objectType { }
}

/**
 * Project type
 */
@Component
class TypeProject
@Autowired
constructor(
        structureService: StructureService
) : TypeDef<Project> {
    override val type: Type<Project>
        get() = objectType {
            fieldInt("id", "Project ID", Project::id)
            // TODO fieldString(Project::name)
            // TODO fieldString(Project::description)
            // TODO fieldBoolean(Project::disabled)
            // TODO("Needs the branch type")
        }
}

/**
 * List of projects
 */
@Component
class RootProjects
@Autowired
constructor
(
        structureService: StructureService
) : RootQueryDef<Project> {
    override val field: Field<Unit, Project>
        get() = TODO("Need for project type")

}
