package capjoy.model.command

import capjoy.model.entity.CaptureDevice
import kotlinx.serialization.Serializable

@Serializable
data class ListDevicesOutput(
    val devices: List<CaptureDevice>,
)
