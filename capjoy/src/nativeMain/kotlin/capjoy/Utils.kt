package capjoy

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.autoreleasepool
import platform.CoreFoundation.CFRunLoopRun
import platform.Foundation.NSTemporaryDirectory
import platform.ScreenCaptureKit.SCShareableContent
import platform.posix.exit
import platform.posix.fprintf
import platform.posix.getenv
import platform.posix.sleep
import platform.posix.stderr
import kotlin.random.Random

fun createTempFile(
    prefix: String,
    suffix: String,
): String {
    val tempDir = NSTemporaryDirectory()
    val fileName = "$prefix${Random.nextInt()}$suffix"
    val filePath = tempDir + fileName
    return filePath
}

@OptIn(ExperimentalForeignApi::class)
fun waitProcessing() {
    if (getenv("CAPJOY_GRADLE_RUN_DEBUG") != null) {
        // on gradle, stdin is not usable. sleep for a while
        println("CAPJOY_GRADLE_RUN_DEBUG is set... sleeping 10 seconds...")
        sleep(10u)
        println("Finished sleeping...")
    } else {
        println("waiting for user input...")
        readlnOrNull()
        println("User input received.")
    }
}

@OptIn(ExperimentalForeignApi::class)
fun eprintln(message: String) {
    // print to stderr
    fprintf(stderr, "%s\n", message)
}

@BetaInteropApi
fun handleContent(callback: (SCShareableContent) -> Unit) {
    autoreleasepool {
        SCShareableContent.getShareableContentWithCompletionHandler { content: SCShareableContent?, error ->
            if (error != null) {
                eprintln("Error in getShareableContentWithCompletionHandler: ${error.localizedDescription}")
                exit(1)
            }

            if (content == null) {
                eprintln("No content found.")
                exit(1)
            } else {
                callback(content)
            }

            exit(0)
        }

        CFRunLoopRun()
    }
}
