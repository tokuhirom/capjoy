package capjoy.model.entity

import kotlinx.serialization.Serializable

@Serializable
data class Application(
    val applicationName: String,
    val bundleIdentifier: String,
    val processID: Long,
)
