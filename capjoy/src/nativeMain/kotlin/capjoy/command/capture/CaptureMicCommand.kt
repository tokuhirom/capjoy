package capjoy.command.capture

import capjoy.recorder.startAudioRecording
import capjoy.utils.DURATION_HELP
import capjoy.utils.WAITING_HELP
import capjoy.utils.waitProcessing
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import platform.AVFoundation.AVFileType
import platform.AVFoundation.AVFileTypeMPEG4
import platform.AVFoundation.AVFileTypeWAVE

class CaptureMicCommand : CliktCommand(
    "Capture audio from the default input device",
    epilog = WAITING_HELP,
) {
    private val fileName: String by argument()
    private val format by option().choice("m4a", "wav").default("m4a")
    private val duration: String? by option(help = DURATION_HELP)

    override fun run() {
        val outFormat: AVFileType? =
            when (format) {
                "m4a" -> AVFileTypeMPEG4
                "wav" -> AVFileTypeWAVE
                else -> error("Unsupported format: $format")
            }

        val recorder = startAudioRecording(outFormat, fileName)

        waitProcessing(duration)

        recorder.stop()
    }
}
