package capjoy.model.entity

import kotlinx.serialization.Serializable

@Serializable
data class Display(
    val displayId: String,
    val frame: Rect,
    val width: Int,
    val height: Int,
    val description: String?,
)
