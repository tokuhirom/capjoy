package capjoy

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.autoreleasepool
import platform.CoreFoundation.CFRunLoopRun
import platform.ScreenCaptureKit.SCShareableContent
import platform.posix.exit
import platform.posix.fprintf
import platform.posix.stderr

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
