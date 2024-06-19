package capjoy.command.list

import capjoy.command.list.utils.showTable
import capjoy.model.command.ListDisplayOutput
import capjoy.model.entity.Display
import capjoy.recorder.getSharableContent
import capjoy.toModel
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.ScreenCaptureKit.SCDisplay

class ListDisplaysCommand :
    CliktCommand(
        "List all displays",
    ) {
    private val format by option().choice("json", "table").default("table")
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
                content.displays
                    .map { display ->
                        display as SCDisplay
                    }.map {
                        Display(
                            displayId = it.displayID.toString(),
                            frame = it.frame.toModel(),
                            width = it.width.toInt(),
                            height = it.height.toInt(),
                            // FIXME: got "<SCDisplay: 0x600003a0ac00>"... maybe kotlin native's bug.
                            description = it.description,
                        )
                    }
            when (format) {
                "json" -> println(json.encodeToString(ListDisplayOutput(got)))
                "table" -> {
                    val headers = listOf(
                        "Display ID",
                        "Width",
                        "Height",
                    )
                    val data = got.map {
                        listOf(
                            it.displayId,
                            it.width.toString(),
                            it.height.toString(),
                        )
                    }
                    showTable(headers, data)
                }
            }
        }
}
