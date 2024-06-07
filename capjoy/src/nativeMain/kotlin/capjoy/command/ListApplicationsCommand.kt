package capjoy.command

import capjoy.handleContent
import capjoy.model.command.ListApplicationsOutput
import capjoy.toModel
import com.github.ajalt.clikt.core.CliktCommand
import kotlinx.cinterop.BetaInteropApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.ScreenCaptureKit.SCRunningApplication

class ListApplicationsCommand : CliktCommand(
    "List all running applications",
) {
    private val json =
        Json {
            prettyPrint = true
        }

    @BetaInteropApi
    override fun run() {
        handleContent { content ->
            val got =
                content.applications.map { application ->
                    application as SCRunningApplication
                }.map {
                    it.toModel()
                }
            println(json.encodeToString(ListApplicationsOutput(got)))
        }
    }
}
