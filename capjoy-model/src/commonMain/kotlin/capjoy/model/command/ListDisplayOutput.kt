package capjoy.model.command

import capjoy.model.entity.Display
import kotlinx.serialization.Serializable

@Serializable
data class ListDisplayOutput(val displays: List<Display>)
