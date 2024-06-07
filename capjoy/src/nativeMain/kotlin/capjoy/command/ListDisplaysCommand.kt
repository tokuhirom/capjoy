package capjoy.command

import capjoy.handleContent
import capjoy.model.command.ListDisplayOutput
import capjoy.model.entity.Display
import capjoy.toModel
import com.github.ajalt.clikt.core.CliktCommand
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.ScreenCaptureKit.SCDisplay

class ListDisplaysCommand : CliktCommand(
    "List all displays",
) {
    private val json =
        Json {
            prettyPrint = true
        }

    @OptIn(ExperimentalForeignApi::class)
    @BetaInteropApi
    override fun run() {
        handleContent { content ->
            val got =
                content.displays.map { display ->
                    display as SCDisplay
                }.map {
                    Display(
                        displayId = it.displayID.toString(),
                        frame = it.frame.toModel(),
                        width = it.width.toInt(),
                        height = it.height.toInt(),
                        description = it.description,
                    )
                }
            println(json.encodeToString(ListDisplayOutput(got)))
        }
    }
}