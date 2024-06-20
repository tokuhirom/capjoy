package capjoy.recorder

import capjoy.eprintln
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AppKit.NSCIImageRep
import platform.AppKit.NSImage
import platform.CoreImage.CIImage
import platform.CoreMedia.CMSampleBufferGetImageBuffer
import platform.CoreMedia.CMSampleBufferIsValid
import platform.CoreMedia.CMSampleBufferRef
import platform.CoreVideo.CVImageBufferRef
import platform.ScreenCaptureKit.SCContentFilter
import platform.ScreenCaptureKit.SCStream
import platform.ScreenCaptureKit.SCStreamConfiguration
import platform.ScreenCaptureKit.SCStreamOutputProtocol
import platform.ScreenCaptureKit.SCStreamOutputType
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalForeignApi::class)
suspend fun captureScreenshot(
    contentFilter: SCContentFilter,
    scStreamConfiguration: SCStreamConfiguration,
    callback: (NSImage) -> Unit,
) {
    val stream = SCStream(contentFilter, scStreamConfiguration, null)

    val streamOutput = object : NSObject(), SCStreamOutputProtocol {
        override fun stream(
            stream: SCStream,
            didOutputSampleBuffer: CMSampleBufferRef?,
            ofType: SCStreamOutputType,
        ) {
            if (!CMSampleBufferIsValid(didOutputSampleBuffer)) {
                eprintln("Invalid sample buffer")
                return
            }

            if (ofType == SCStreamOutputType.SCStreamOutputTypeScreen) {
                val imageBuffer: CVImageBufferRef = CMSampleBufferGetImageBuffer(didOutputSampleBuffer)!!
                val ciImage = CIImage(cVImageBuffer = imageBuffer)
                val rep = NSCIImageRep(ciImage)
                val nsImage = NSImage(size = rep.size)
                nsImage.addRepresentation(rep)
                callback(nsImage)
                stream.stopCaptureWithCompletionHandler { error ->
                    if (error != null) {
                        println("Failed to stop capture: ${error.localizedDescription}")
                    } else {
                        println("Capture stopped")
                    }
                }
            }
        }
    }

    stream.addStreamOutput(
        streamOutput,
        SCStreamOutputType.SCStreamOutputTypeScreen,
        sampleHandlerQueue = null,
        error = null,
    )

    stream.startCapture()
}

suspend fun SCStream.startCapture() =
    suspendCoroutine { cont ->
        this.startCaptureWithCompletionHandler { error ->
            if (error != null) {
                cont.resumeWithException(Exception("Failed to start capture: ${error.localizedDescription}"))
            } else {
                cont.resume(Unit)
            }
        }
    }
