package capjoy.command

import capjoy.recorder.startScreenRecord
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import platform.Foundation.NSRunLoop
import platform.Foundation.run
import platform.posix.exit
import platform.posix.sleep

@OptIn(ExperimentalForeignApi::class)
class RecordAudioCommand : CliktCommand() {
    private val fileName: String by argument()

    override fun run() {
        memScoped {
            startScreenRecord(fileName) { screenRecorder ->
                println("Recording... Press ENTER to stop.")
                sleep(10u)
                println("Sleeped...")

                screenRecorder.stop {
                    println("Writing finished")
                    exit(0)
                }
            }
        }

        // Run the main run loop to process the asynchronous callback
        NSRunLoop.mainRunLoop().run()
    }
}
