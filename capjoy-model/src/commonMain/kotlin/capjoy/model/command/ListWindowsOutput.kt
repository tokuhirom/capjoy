package capjoy.model.command

import capjoy.model.entity.Window
import kotlinx.serialization.Serializable

@Serializable
data class ListWindowsOutput(val windows: List<Window>)
