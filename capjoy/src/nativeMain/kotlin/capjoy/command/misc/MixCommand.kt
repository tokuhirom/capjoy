package capjoy.command.misc

import capjoy.recorder.mix
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice
import kotlinx.coroutines.runBlocking
import platform.AVFoundation.AVFileTypeAppleM4A
import platform.Foundation.NSRunLoop
import platform.Foundation.run

class MixCommand :
    CliktCommand(
        "Mix audio files",
    ) {
    private val outputFile: String by option("-o", "--output", help = "Output file").required()
    private val inputFiles: List<String> by argument().multiple()
    private val outputFileType: String by option("-f", "--format", help = "Output file type")
        .choice("m4a")
        .default("m4a")

    override fun run() =
        runBlocking {
            val avFileType = when (outputFileType) {
                "m4a" -> AVFileTypeAppleM4A
                else -> error("Unsupported format: $outputFileType")
            }

            mix(inputFiles, outputFile, avFileType)

            NSRunLoop.mainRunLoop().run()
        }
}
