package capjoy.command.capture

import capjoy.recorder.captureScreenshot
import capjoy.recorder.findDefaultDisplay
import capjoy.recorder.findDisplayByDisplayId
import capjoy.recorder.findWindowByWindowId
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.long
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.autoreleasepool
import kotlinx.cinterop.convert
import platform.AppKit.NSApplication
import platform.AppKit.NSApplicationActivationPolicy
import platform.AppKit.NSBitmapImageFileType
import platform.AppKit.NSBitmapImageRep
import platform.AppKit.NSImage
import platform.AppKit.representationUsingType
import platform.CoreGraphics.CGDirectDisplayID
import platform.CoreGraphics.CGWindowID
import platform.CoreMedia.CMTimeMake
import platform.Foundation.writeToFile
import platform.ScreenCaptureKit.SCContentFilter
import platform.ScreenCaptureKit.SCStreamConfiguration
import platform.posix.exit

class CaptureImageCommand : CliktCommand(
    "Capture an image of a window or the entire display",
) {
    private val windowID: Long? by option(help = "Window ID to capture").long()
    private val displayID: Long? by option(help = "Display ID to capture").long()
    private val fileName: String by argument()
    private val format by option().choice("png", "jpg").default("png")

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    override fun run() {
        autoreleasepool {
            val app = NSApplication.sharedApplication()
            app.setActivationPolicy(NSApplicationActivationPolicy.NSApplicationActivationPolicyRegular)
            val fileType = when (format) {
                "png" -> NSBitmapImageFileType.NSBitmapImageFileTypePNG
                "jpg" -> NSBitmapImageFileType.NSBitmapImageFileTypeJPEG
                else -> error("Unsupported format: $format")
            }

            println("Start capturing image to $fileName")

            when {
                windowID != null -> {
                    captureWindow(windowID!!.convert(), fileName, fileType)
                }

                displayID != null -> {
                    captureDisplay(displayID!!.convert(), fileName, fileType)
                }

                else -> {
                    captureDefaultDisplay(fileName, fileType)
                }
            }

            println("Starting the main run loop to process the asynchronous callback")
            app.run()
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    fun captureWindow(
        windowID: CGWindowID,
        filePath: String,
        fileType: NSBitmapImageFileType,
    ) {
        findWindowByWindowId(windowID) { window ->
            val filter = SCContentFilter(desktopIndependentWindow = window)
            val configuration = SCStreamConfiguration().apply {
                minimumFrameInterval = CMTimeMake(value = 1, timescale = 30)
                showsCursor = false
            }
            startScreenCapture(filePath, filter, configuration, fileType)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    fun captureDisplay(
        displayID: CGDirectDisplayID,
        filePath: String,
        fileType: NSBitmapImageFileType,
    ) {
        findDisplayByDisplayId(displayID.toLong()) { display, apps ->
            val filter = SCContentFilter(
                display = display,
                includingApplications = apps,
                exceptingWindows = emptyList<Any>(),
            )
            val configuration = SCStreamConfiguration().apply {
                minimumFrameInterval = CMTimeMake(value = 1, timescale = 30)
                showsCursor = false
            }
            startScreenCapture(filePath, filter, configuration, fileType)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    fun captureDefaultDisplay(
        filePath: String,
        fileType: NSBitmapImageFileType,
    ) {
        findDefaultDisplay { display, apps ->
            val filter = SCContentFilter(
                display = display,
                includingApplications = apps,
                exceptingWindows = emptyList<Any>(),
            )
            val configuration = SCStreamConfiguration().apply {
                minimumFrameInterval = CMTimeMake(value = 1, timescale = 30)
                showsCursor = false
            }
            startScreenCapture(filePath, filter, configuration, fileType)
        }
    }

    private fun startScreenCapture(
        filePath: String,
        contentFilter: SCContentFilter,
        scStreamConfiguration: SCStreamConfiguration,
        fileType: NSBitmapImageFileType,
    ) {
        captureScreenshot(contentFilter, scStreamConfiguration) { image ->
            println("Image saved to $filePath")
            saveImageToFile(image, filePath, fileType)
            exit(0)
        }
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
