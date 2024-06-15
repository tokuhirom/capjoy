package capjoy.command.misc

import capjoy.recorder.convertAudioFile
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import platform.CoreAudioTypes.kAudioFormatLinearPCM

class ConvertAudioCommand : CliktCommand("Convert audio files") {
    private val inputFile: String by argument()
    private val outputFile: String by argument()

    override fun run() {
        if (convertAudioFile(inputFile, outputFile, kAudioFormatLinearPCM)) {
            println("Conversion successful: $outputFile")
        } else {
            println("Conversion failed")
        }
    }
}
