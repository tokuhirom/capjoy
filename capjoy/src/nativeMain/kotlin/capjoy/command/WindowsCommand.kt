package capjoy.command

import capjoy.handleContent
import capjoy.model.command.ListWindowsOutput
import capjoy.model.entity.Window
import capjoy.toModel
import com.github.ajalt.clikt.core.CliktCommand
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.ScreenCaptureKit.SCWindow

class WindowsCommand : CliktCommand() {
    private val json =
        Json {
            prettyPrint = true
        }

    @OptIn(ExperimentalForeignApi::class)
    @BetaInteropApi
    override fun run() {
        handleContent { content ->
            val got =
                content.windows.map { window ->
                    window as SCWindow
                }.map {
                    Window(
                        active = it.active,
                        frame = it.frame.toModel(),
                        onScreen = it.onScreen,
                        owningApplication = it.owningApplication?.toModel(),
                        title = it.title,
                        windowID = it.windowID,
                        windowLayer = it.windowLayer,
                    )
                }
            println(json.encodeToString(ListWindowsOutput(got)))
        }
    }
}
