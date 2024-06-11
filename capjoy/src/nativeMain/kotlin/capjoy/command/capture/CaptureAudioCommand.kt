package capjoy.command.capture

import capjoy.recorder.findDefaultDisplay
import capjoy.recorder.startScreenRecord
import capjoy.waitProcessing
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import platform.Foundation.NSRunLoop
import platform.Foundation.run
import platform.ScreenCaptureKit.SCContentFilter
import platform.posix.exit

@OptIn(ExperimentalForeignApi::class)
class CaptureAudioCommand : CliktCommand("Capture audio from the screen") {
    private val fileName: String by argument()

    override fun run() {
        memScoped {
            findDefaultDisplay { display ->
                println("Display found: $display")

                val contentFilter = SCContentFilter(
                    display,
                    excludingWindows = emptyList<Any>(),
                )
                startScreenRecord(fileName, contentFilter) { screenRecorder ->
                    waitProcessing()

                    screenRecorder.stop {
                        println("Writing finished")
                        exit(0)
                    }
                }
            }
        }

        // Run the main run loop to process the asynchronous callback
        NSRunLoop.mainRunLoop().run()
    }
}
