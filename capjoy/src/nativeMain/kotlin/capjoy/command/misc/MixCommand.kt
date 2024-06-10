package capjoy.command.misc

import capjoy.recorder.mix
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required

class MixCommand : CliktCommand(
    "Mix audio files",
) {
    private val outputFile: String by option("-o", "--output", help = "Output file").required()
    private val inputFiles: List<String> by argument().multiple()

    override fun run() {
        mix(inputFiles, outputFile)
    }
}
