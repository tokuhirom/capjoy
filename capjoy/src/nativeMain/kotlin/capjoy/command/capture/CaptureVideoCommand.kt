package capjoy.command.capture

import capjoy.recorder.findWindowByWindowId
import capjoy.recorder.startScreenRecord
import capjoy.waitProcessing
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.boolean
import com.github.ajalt.clikt.parameters.types.long
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.autoreleasepool
import kotlinx.cinterop.memScoped
import platform.AppKit.NSApplication
import platform.AppKit.NSApplicationActivationPolicy
import platform.CoreGraphics.CGRectGetHeight
import platform.CoreGraphics.CGRectGetWidth
import platform.CoreMedia.CMTimeMake
import platform.ScreenCaptureKit.SCContentFilter
import platform.ScreenCaptureKit.SCStreamConfiguration
import platform.posix.exit

@OptIn(ExperimentalForeignApi::class)
class CaptureVideoCommand : CliktCommand("Capture video and audio from the screen") {
    private val fileName: String by argument()
    private val showsCursor: Boolean by option().boolean().default(false)
    private val windowID: Long by argument().long()

    @OptIn(BetaInteropApi::class)
    override fun run() {
        autoreleasepool {
            val app = NSApplication.sharedApplication()
            app.setActivationPolicy(NSApplicationActivationPolicy.NSApplicationActivationPolicyRegular)

            memScoped {
                findWindowByWindowId(windowID) { window ->
                    println("Window found: $window, title=${window.title}, ${window.owningApplication?.bundleIdentifier}")

                    val contentFilter = SCContentFilter(desktopIndependentWindow = window)
                    val captureConfiguration = SCStreamConfiguration().apply {
                        println("Configuring SCStreamConfiguration(showsCursor=$showsCursor)...")
                        this.showsCursor = showsCursor
                        capturesAudio = true
                        width = CGRectGetWidth(window.frame()).toULong()
                        height = CGRectGetHeight(window.frame()).toULong()
                        println("Width: $width, Height: $height")
                        minimumFrameInterval = CMTimeMake(value = 1, timescale = 30) // 30 FPS
                    }

                    startScreenRecord(
                        fileName,
                        contentFilter,
                        isVideo = true,
                        captureConfiguration
                    ) { screenRecorder ->
                        waitProcessing()

                        screenRecorder.stop {
                            println("Writing finished: $fileName")
                            exit(0)
                        }
                    }
                }
            }

            // Run the main run loop to process the asynchronous callback
            app.run()
        }
    }
}
