package capjoy.command

import capjoy.recorder.startAudioRecording
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import platform.AVFoundation.AVFileType
import platform.AVFoundation.AVFileTypeMPEG4
import platform.AVFoundation.AVFileTypeWAVE
import platform.posix.sleep

class MicCommand : CliktCommand() {
    private val fileName: String by argument()
    private val format by option().choice("m4a", "wav").default("m4a")

    override fun run() {
        val outFormat: AVFileType? =
            when (format) {
                "m4a" -> AVFileTypeMPEG4
                "wav" -> AVFileTypeWAVE
                else -> error("Unsupported format: $format")
            }

        val recorder = startAudioRecording(outFormat, fileName)

        println("Recording... Press ENTER to stop.")
        sleep(10u)

        println("Recording stopped.")

        recorder.stop()
    }
}
