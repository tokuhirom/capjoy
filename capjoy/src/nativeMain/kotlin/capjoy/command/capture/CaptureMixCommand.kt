package capjoy.command.capture

import capjoy.createTempFile
import capjoy.recorder.defaultDisplay
import capjoy.recorder.getSharableContent
import capjoy.recorder.mix
import capjoy.recorder.startAudioRecording
import capjoy.recorder.startScreenRecord
import capjoy.utils.DURATION_HELP
import capjoy.utils.WAITING_HELP
import capjoy.utils.waitProcessing
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import kotlinx.cinterop.BetaInteropApi
import kotlinx.coroutines.runBlocking
import platform.AVFoundation.AVFileTypeMPEG4
import platform.Foundation.NSRunLoop
import platform.Foundation.run
import platform.ScreenCaptureKit.SCContentFilter
import platform.ScreenCaptureKit.SCStreamConfiguration
import platform.posix.unlink

class CaptureMixCommand :
    CliktCommand(
        "Capture and mix mic audio and screen audio into a single file",
        epilog = WAITING_HELP,
    ) {
    private val outFileName: String by argument()
    private val duration: String? by option(help = DURATION_HELP)

    @OptIn(BetaInteropApi::class)
    override fun run() {
        runBlocking {
            val micFile = createTempFile("capjoy-mix-mic-", ".m4a")
            val screenFile = createTempFile("capjoy-mix-screen-", ".m4a")

            println("Recording audio and screen to $micFile and $screenFile ...")

            val micRecorder = startAudioRecording(AVFileTypeMPEG4, micFile)
            println("Started micRecorder...")

            val content = getSharableContent()
            val display = content.defaultDisplay()
            println("Display found: $display")

            // 逆にここかも??
            val contentFilter = SCContentFilter(
                display,
                includingApplications = content.applications,
                exceptingWindows = emptyList<Any>(),
            )
            val captureConfiguration = SCStreamConfiguration().apply {
                capturesAudio = true
            }
            val screenRecorder = startScreenRecord(
                screenFile,
                contentFilter,
                enableVideo = false,
                enableAudio = true,
                captureConfiguration,
            )

            waitProcessing(duration)

            micRecorder.stop()

            screenRecorder.stop()

            println("Starting mix from $micFile and $screenFile to $outFileName...")
            mix(listOf(micFile, screenFile), outFileName)

            println("Created mix file: $outFileName")

            unlink(micFile)
            unlink(screenFile)

            NSRunLoop.mainRunLoop().run()
        }
    }
}
