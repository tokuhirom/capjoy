package capjoy.command.capture

import capjoy.recorder.captureScreenshot
import capjoy.recorder.findTarget
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.long
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import kotlinx.coroutines.runBlocking
import platform.AppKit.NSApplication
import platform.AppKit.NSApplicationActivationPolicy
import platform.AppKit.NSBitmapImageFileType
import platform.AppKit.NSBitmapImageRep
import platform.AppKit.NSImage
import platform.AppKit.representationUsingType
import platform.CoreMedia.CMTimeMake
import platform.Foundation.writeToFile
import platform.ScreenCaptureKit.SCStreamConfiguration
import platform.posix.exit

class CaptureImageCommand :
    CliktCommand(
        "Capture an image of a window or the entire display",
    ) {
    private val windowID: Long? by option(help = "Window ID to capture").long()
    private val displayID: Long? by option(help = "Display ID to capture").long()
    private val fileName: String by argument()
    private val format by option().choice("png", "jpg").default("png")

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    override fun run() =
        runBlocking {
            val app = NSApplication.sharedApplication()
            app.setActivationPolicy(NSApplicationActivationPolicy.NSApplicationActivationPolicyRegular)
            val fileType = when (format) {
                "png" -> NSBitmapImageFileType.NSBitmapImageFileTypePNG
                "jpg" -> NSBitmapImageFileType.NSBitmapImageFileTypeJPEG
                else -> error("Unsupported format: $format")
            }

            println("Start capturing image to $fileName")

            val target = findTarget(displayID?.convert(), windowID?.convert())

            val configuration = SCStreamConfiguration().apply {
                minimumFrameInterval = CMTimeMake(value = 1, timescale = 30)
                showsCursor = false
            }
            captureScreenshot(target.contentFilter, configuration) { image ->
                println("Image saved to $fileName")
                saveImageToFile(image, fileName, fileType)
                exit(0)
            }

            println("Starting the main run loop to process the asynchronous callback")
            app.run()
        }

    private fun saveImageToFile(
        image: NSImage,
        filePath: String,
        fileFormat: NSBitmapImageFileType,
    ): Boolean {
        val imageData = image.TIFFRepresentation ?: return false
        val bitmapImageRep = NSBitmapImageRep(data = imageData) ?: return false
        val pngData = bitmapImageRep.representationUsingType(
            fileFormat,
            properties = emptyMap<Any?, Any>(),
        )

        return pngData?.writeToFile(filePath, atomically = true) ?: false
    }
}
