package capjoy.model.entity

import kotlinx.serialization.Serializable

@Serializable
data class Rect(val size: Int, val align: Int)