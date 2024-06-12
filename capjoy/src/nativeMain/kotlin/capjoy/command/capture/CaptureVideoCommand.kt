package capjoy.command.capture

import capjoy.recorder.findDefaultDisplay
import capjoy.recorder.findDisplayByDisplayId
import capjoy.recorder.findWindowByWindowId
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
import com.github.ajalt.clikt.parameters.types.long
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.autoreleasepool
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import platform.AppKit.NSApplication
import platform.AppKit.NSApplicationActivationPolicy
import platform.CoreGraphics.CGRectGetHeight
import platform.CoreGraphics.CGRectGetWidth
import platform.CoreMedia.CMTimeMake
import platform.ScreenCaptureKit.SCContentFilter
import platform.ScreenCaptureKit.SCDisplay
import platform.ScreenCaptureKit.SCStreamConfiguration
import platform.ScreenCaptureKit.SCWindow
import platform.posix.exit

@OptIn(ExperimentalForeignApi::class)
class CaptureVideoCommand : CliktCommand(
    "Capture video and audio from the screen",
    epilog = WAITING_HELP,
) {
    private val fileName: String by argument()
    private val showsCursor: Boolean by option().boolean().default(false)
    private val audio: Boolean by option().boolean().help("Enable audio recording").default(true)
    private val displayID: Long? by option().long()
    private val windowID: Long? by option().long()
    private val duration: String? by option(help = DURATION_HELP)

    @OptIn(BetaInteropApi::class)
    override fun run() {
        autoreleasepool {
            val app = NSApplication.sharedApplication()
            app.setActivationPolicy(NSApplicationActivationPolicy.NSApplicationActivationPolicyRegular)

            memScoped {
                if (windowID != null) {
                    findWindowByWindowId(windowID!!) { window ->
                        recordVideoFromWindow(window)
                    }
                } else if (displayID != null) {
                    findDisplayByDisplayId(displayID!!) { display, applications ->
                        recordVideoFromDisplay(display, applications)
                    }
                } else {
                    findDefaultDisplay { display, applications ->
                        recordVideoFromDisplay(display, applications)
                    }
                }
            }

            // Run the main run loop to process the asynchronous callback
            app.run()
        }
    }

    private fun buildConfiguration(): SCStreamConfiguration {
        val optShowsCursor = showsCursor
        return SCStreamConfiguration().apply {
            println("Configuring SCStreamConfiguration(showsCursor=$optShowsCursor)...")
            this.showsCursor = optShowsCursor
            capturesAudio = audio
            minimumFrameInterval = CMTimeMake(value = 1, timescale = 30) // 30 FPS
        }
    }

    private fun recordVideoFromWindow(window: SCWindow) {
        println("Window found: $window, title=${window.title}, ${window.owningApplication?.bundleIdentifier}")

        val contentFilter = SCContentFilter(desktopIndependentWindow = window)
        val captureConfiguration = buildConfiguration().apply {
            width = CGRectGetWidth(window.frame()).toULong()
            height = CGRectGetHeight(window.frame()).toULong()
            println("Width: $width, Height: $height")
        }
        recordVideo(contentFilter, captureConfiguration)
    }

    // To capture the entire display with ScreenCaptureKit, use initWithDisplay:includingApplications:exceptingWindows:
    // instead of initWithDisplay:excludingWindows: with an empty list, as the latter does not work correctly.
    // This workaround involves listing all running applications.
    // For more details, see: https://federicoterzi.com/blog/screencapturekit-failing-to-capture-the-entire-display/
    private fun recordVideoFromDisplay(
        display: SCDisplay,
        applications: List<*>,
    ) {
        println("Display found: ${display.displayID}, ${display.width}x${display.height} - ${display.description}")
        val contentFilter = SCContentFilter(
            display = display,
            includingApplications = applications,
            exceptingWindows = emptyList<Any>(),
        )
        val captureConfiguration = buildConfiguration().apply {
            width = display.width.convert()
            height = display.height.convert()
            println("Width: $width, Height: $height")
        }
        recordVideo(contentFilter, captureConfiguration)
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
