package capjoy.command

import capjoy.recorder.mix
import capjoy.recorder.startAudioRecording
import capjoy.recorder.startScreenRecord
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import platform.AVFoundation.AVFileTypeMPEG4
import platform.Foundation.NSRunLoop
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.run
import platform.posix.sleep
import platform.posix.unlink
import kotlin.random.Random

private fun createTempFile(prefix: String, suffix: String): String {
    val tempDir = NSTemporaryDirectory()
    val fileName = "$prefix${Random.nextInt()}$suffix"
    val filePath = tempDir + fileName
    return filePath
}

class RecordMixCommand : CliktCommand() {
    private val outFileName: String by argument()

    override fun run() {
        val micFile = createTempFile("capjoy-mix-mic-", ".m4a")
        val screenFile = createTempFile("capjoy-mix-screen-", ".m4a")

        println("Recording audio and screen to $micFile and $screenFile ...")

        val micRecorder = startAudioRecording(AVFileTypeMPEG4, micFile)
        println("Started micRecorder...")
        startScreenRecord(screenFile) { screenRecorder ->
            println("Recording... Press ENTER to stop.")

            // TODO replace with a proper way to wait for user input
            sleep(10u)

            println("Recording stopped.")

            micRecorder.stop()

            screenRecorder.stop {
                println("Writing finished")

                println("Starting mix...")

                mix(listOf(micFile, screenFile), outFileName)

                println("Created mix file: $outFileName")

                unlink(micFile)
                unlink(screenFile)
            }
        }
        NSRunLoop.mainRunLoop().run()
    }
}
