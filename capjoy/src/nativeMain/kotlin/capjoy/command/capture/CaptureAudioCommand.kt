package capjoy.command.capture

import capjoy.recorder.defaultDisplay
import capjoy.recorder.getSharableContent
import capjoy.recorder.startScreenRecord
import capjoy.utils.DURATION_HELP
import capjoy.utils.WAITING_HELP
import capjoy.utils.waitProcessing
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.coroutines.runBlocking
import platform.Foundation.NSRunLoop
import platform.Foundation.run
import platform.ScreenCaptureKit.SCContentFilter
import platform.ScreenCaptureKit.SCStreamConfiguration
import platform.posix.exit

@OptIn(ExperimentalForeignApi::class)
class CaptureAudioCommand :
    CliktCommand(
        "Capture audio from the screen",
        epilog = WAITING_HELP,
    ) {
    private val fileName: String by argument()
    private val duration: String? by option(help = DURATION_HELP)

    @OptIn(BetaInteropApi::class)
    override fun run() =
        runBlocking {
            memScoped {
                val content = getSharableContent()
                val display = content.defaultDisplay()
                println("Display found: $display")

                val contentFilter = SCContentFilter(
                    display,
                    excludingWindows = emptyList<Any>(),
                )
                val captureConfiguration = SCStreamConfiguration().apply {
                    capturesAudio = true
                }
                val screenRecorder = startScreenRecord(
                    fileName,
                    contentFilter,
                    enableVideo = false,
                    enableAudio = true,
                    captureConfiguration,
                )

                waitProcessing(duration)

                screenRecorder.stop()

                println("Writing finished: $fileName")
                exit(0)
            }

            // Run the main run loop to process the asynchronous callback
            NSRunLoop.mainRunLoop().run()
        }
}
