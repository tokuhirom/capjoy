package capjoy.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.long
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import kotlinx.cinterop.readValue
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFURLCreateWithFileSystemPath
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFStringEncodingUTF8
import platform.CoreFoundation.kCFURLPOSIXPathStyle
import platform.CoreGraphics.CGRectNull
import platform.CoreGraphics.CGWindowID
import platform.CoreGraphics.CGWindowListCreateImage
import platform.CoreGraphics.kCGWindowImageDefault
import platform.CoreGraphics.kCGWindowListOptionIncludingWindow
import platform.CoreServices.kUTTypeJPEG
import platform.CoreServices.kUTTypePNG
import platform.ImageIO.CGImageDestinationAddImage
import platform.ImageIO.CGImageDestinationCreateWithURL
import platform.ImageIO.CGImageDestinationFinalize

class CaptureImageCommand : CliktCommand() {
    private val windowID: Long by argument().long()
    private val path: String by argument()
    private val format by option().choice("png", "jpg")
        .default("png")

    @OptIn(ExperimentalForeignApi::class)
    override fun run() {
        captureWindow(
            windowID.convert(),
            path,
            when (format) {
                "png" -> kUTTypePNG
                "jpg" -> kUTTypeJPEG
                else -> error("Unsupported format: $format")
            },
        )
    }

    @OptIn(ExperimentalForeignApi::class)
    fun captureWindow(
        windowID: CGWindowID,
        filePath: String,
        imageFormat: CFStringRef?,
    ) {
        val image =
            CGWindowListCreateImage(
                CGRectNull.readValue(),
                kCGWindowListOptionIncludingWindow,
                windowID,
                kCGWindowImageDefault,
            )

        if (image != null) {
            val filePathCFString =
                CFStringCreateWithCString(kCFAllocatorDefault, filePath, kCFStringEncodingUTF8)
            val url =
                CFURLCreateWithFileSystemPath(
                    kCFAllocatorDefault,
                    filePathCFString,
                    kCFURLPOSIXPathStyle,
                    false,
                )

            val destination = CGImageDestinationCreateWithURL(url, imageFormat, 1.convert(), null)
            if (destination != null) {
                CGImageDestinationAddImage(destination, image, null)
                if (CGImageDestinationFinalize(destination)) {
                    println("Image saved to $filePath")
                } else {
                    println("Failed to finalize image destination")
                }
            } else {
                println("Failed to create image destination")
            }
        } else {
            println("Failed to create image")
        }
    }
}
