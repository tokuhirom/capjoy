package capjoy.command

import capjoy.handleContent
import capjoy.model.Display
import capjoy.model.Displays
import capjoy.model.Rect
import com.github.ajalt.clikt.core.CliktCommand
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.ScreenCaptureKit.SCDisplay

class DisplaysCommand: CliktCommand() {
    val json = Json {
        prettyPrint = true
    }

    @OptIn(ExperimentalForeignApi::class)
    @BetaInteropApi
    override fun run() {
        handleContent { content ->
            val got = content.displays.map { display ->
                display as SCDisplay
            }.map {
                Display(
                    displayId = it.displayID.toString(),
                    frame = Rect(
                        size = it.frame.size,
                        align = it.frame.align,
                    ),
                    width = it.width.toInt(),
                    height = it.height.toInt(),
                    description = it.description,
                )
            }
            println(json.encodeToString(Displays(got)))
        }
    }
}
