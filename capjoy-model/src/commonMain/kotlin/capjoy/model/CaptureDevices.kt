package capjoy.model

import kotlinx.serialization.Serializable

@Serializable
data class CaptureDevices(
    val devices: List<CaptureDevice>,
)
