package capjoy.command.capture

import capjoy.recorder.findTarget
import capjoy.recorder.startScreenRecord
import capjoy.utils.DURATION_HELP
import capjoy.utils.WAITING_HELP
import capjoy.utils.waitProcessing
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.boolean
import com.github.ajalt.clikt.parameters.types.uint
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.autoreleasepool
import kotlinx.cinterop.memScoped
import platform.AppKit.NSApplication
import platform.AppKit.NSApplicationActivationPolicy
import platform.CoreMedia.CMTimeMake
import platform.ScreenCaptureKit.SCContentFilter
import platform.ScreenCaptureKit.SCStreamConfiguration
import platform.posix.exit

@OptIn(ExperimentalForeignApi::class)
class CaptureVideoCommand :
    CliktCommand(
        "Capture video and audio from the screen",
        epilog = "$WAITING_HELP\n\nDisplay capture may not work on your environemnt." +
            " it's recommended to use window capture.\n\n" +
            "This command is experimental and may not work as expected(Patches welcome).",
    ) {
    private val fileName: String by argument()
    private val showsCursor: Boolean by option().boolean().default(false)
    private val audio: Boolean by option().boolean().help("Enable audio recording").default(true)
    private val displayID: UInt? by option().uint()
    private val windowID: UInt? by option().uint()
    private val duration: String? by option(help = DURATION_HELP)

    @OptIn(BetaInteropApi::class)
    override fun run() {
        autoreleasepool {
            val app = NSApplication.sharedApplication()
            app.setActivationPolicy(NSApplicationActivationPolicy.NSApplicationActivationPolicyRegular)

            memScoped {
                findTarget(displayID, windowID) { target ->
                    val optShowsCursor = showsCursor
                    val configuration = SCStreamConfiguration().apply {
                        println("Configuring SCStreamConfiguration(showsCursor=$optShowsCursor)...")
                        this.showsCursor = optShowsCursor
                        capturesAudio = audio
                        minimumFrameInterval = CMTimeMake(value = 1, timescale = 30) // 30 FPS
                        width = target.width
                        height = target.height
                    }
                    recordVideo(target.contentFilter, configuration)
                }
            }

            // Run the main run loop to process the asynchronous callback
            app.run()
        }
    }

    private fun recordVideo(
        contentFilter: SCContentFilter,
        captureConfiguration: SCStreamConfiguration,
    ) {
        startScreenRecord(
            fileName,
            contentFilter,
            enableVideo = true,
            enableAudio = audio,
            captureConfiguration,
        ) { screenRecorder ->
            waitProcessing(duration)

            screenRecorder.stop {
                println("Writing finished: $fileName")
                exit(0)
            }
        }
    }
}
