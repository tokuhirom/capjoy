package capjoy.model.command

import capjoy.model.entity.Application
import kotlinx.serialization.Serializable

@Serializable
data class ListApplicationsOutput(
    val applications: List<Application>,
)
