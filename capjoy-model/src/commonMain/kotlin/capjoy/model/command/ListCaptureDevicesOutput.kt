package capjoy.model.command

import capjoy.model.entity.CaptureDevice
import kotlinx.serialization.Serializable

@Serializable
data class ListCaptureDevicesOutput(
    val devices: List<CaptureDevice>,
)
