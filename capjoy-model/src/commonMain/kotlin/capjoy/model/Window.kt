package capjoy.model

import kotlinx.serialization.Serializable

@Serializable
data class Window(
    val active: Boolean,
    val frame: Rect,
    val onScreen: Boolean,
    val owningApplication: Application?,
    val title: String?,
    val windowID: UInt,
    val windowLayer: Long,
)
