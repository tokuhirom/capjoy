package capjoy.command.capture

import capjoy.createTempFile
import capjoy.recorder.findDefaultDisplay
import capjoy.recorder.mix
import capjoy.recorder.startAudioRecording
import capjoy.recorder.startScreenRecord
import capjoy.waitProcessing
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import platform.AVFoundation.AVFileTypeMPEG4
import platform.Foundation.NSRunLoop
import platform.Foundation.run
import platform.ScreenCaptureKit.SCContentFilter
import platform.ScreenCaptureKit.SCStreamConfiguration
import platform.posix.unlink

class CaptureMixCommand : CliktCommand("Capture and mix mic audio and screen audio into a single file") {
    private val outFileName: String by argument()

    override fun run() {
        val micFile = createTempFile("capjoy-mix-mic-", ".m4a")
        val screenFile = createTempFile("capjoy-mix-screen-", ".m4a")

        println("Recording audio and screen to $micFile and $screenFile ...")

        val micRecorder = startAudioRecording(AVFileTypeMPEG4, micFile)
        println("Started micRecorder...")
        findDefaultDisplay { display ->
            println("Display found: $display")

            val contentFilter = SCContentFilter(
                display,
                excludingWindows = emptyList<Any>(),
            )
            val captureConfiguration = SCStreamConfiguration().apply {
                capturesAudio = true
            }
            startScreenRecord(
                screenFile,
                contentFilter,
                enableVideo = false,
                enableAudio = true,
                captureConfiguration,
            ) { screenRecorder ->
                waitProcessing()

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
        }
        NSRunLoop.mainRunLoop().run()
    }
}
