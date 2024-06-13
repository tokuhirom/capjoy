package capjoy.command.list

import capjoy.command.list.utils.showTable
import capjoy.handleContent
import capjoy.model.command.ListApplicationsOutput
import capjoy.toModel
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import kotlinx.cinterop.BetaInteropApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.ScreenCaptureKit.SCRunningApplication

class ListApplicationsCommand : CliktCommand(
    "List all running applications",
) {
    private val format by option().choice("json", "table").default("table")
    private val json =
        Json {
            encodeDefaults = true
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
            when (format) {
                "json" -> println(json.encodeToString(ListApplicationsOutput(got)))
                "table" -> {
                    val headers = listOf(
                        "Process ID",
                        "Bundle Identifier",
                        "Application Name",
                    )
                    val data = got.map {
                        listOf(
                            it.processID.toString(),
                            it.bundleIdentifier,
                            it.applicationName,
                        )
                    }
                    showTable(headers, data)
                }
            }
        }
    }
}
