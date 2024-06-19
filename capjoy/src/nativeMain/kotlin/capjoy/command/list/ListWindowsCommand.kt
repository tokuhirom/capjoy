package capjoy.command.list

import capjoy.command.list.utils.filterTableCols
import capjoy.command.list.utils.showTable
import capjoy.model.command.ListWindowsOutput
import capjoy.model.entity.Window
import capjoy.recorder.getSharableContent
import capjoy.toModel
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.boolean
import com.github.ajalt.clikt.parameters.types.choice
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.ScreenCaptureKit.SCWindow

class ListWindowsCommand :
    CliktCommand(
        "List all windows",
    ) {
    private val showInactive: Boolean by option()
        .boolean()
        .help("Show inactive windows")
        .default(false)
    private val format by option().choice("json", "table").default("table")
    private val verbose by option().boolean().default(false)
    private val json =
        Json {
            prettyPrint = true
        }

    @OptIn(ExperimentalForeignApi::class)
    @BetaInteropApi
    override fun run() =
        runBlocking {
            val content = getSharableContent()
            val got =
                content.windows
                    .map { window ->
                        window as SCWindow
                    }.filter {
                        showInactive || it.active
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
            when (format) {
                "json" -> println(json.encodeToString(ListWindowsOutput(got)))
                "table" -> {
                    val headers = listOf(
                        "Window ID",
                        "Active",
                        "onScreen",
                        "Process ID",
                        "Bundle ID",
                        "App Name",
                        "Title",
                    )
                    val rows = got.map {
                        listOf(
                            it.windowID.toString(),
                            it.active.toString(),
                            it.onScreen.toString(),
                            it.owningApplication?.processID.toString(),
                            it.owningApplication?.bundleIdentifier.toString(),
                            it.owningApplication?.applicationName.toString(),
                            it.title ?: "",
                        )
                    }
                    if (verbose) {
                        showTable(headers, rows)
                    } else {
                        val (filteredHeaders, filteredRows) = filterTableCols(
                            listOf(
                                "Window ID",
                                "Bundle ID",
                                "App Name",
                                "Title",
                            ),
                            headers,
                            rows,
                        )
                        showTable(filteredHeaders, filteredRows)
                    }
                }
            }
        }
}
