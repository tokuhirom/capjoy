package capjoy.model

import kotlinx.serialization.Serializable

@Serializable
data class Applications(
    val applications: List<Application>
)
