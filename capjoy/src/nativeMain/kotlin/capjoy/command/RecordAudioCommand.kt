package capjoy.command

import capjoy.eprintln
import com.github.ajalt.clikt.core.CliktCommand
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import platform.CoreMedia.CMBlockBufferCopyDataBytes
import platform.CoreMedia.CMBlockBufferGetDataLength
import platform.CoreMedia.CMSampleBufferGetDataBuffer
import platform.CoreMedia.CMSampleBufferIsValid
import platform.CoreMedia.CMSampleBufferRef
import platform.Foundation.NSFileHandle
import platform.Foundation.NSMutableData
import platform.Foundation.NSRunLoop
import platform.Foundation.NSURL
import platform.Foundation.closeFile
import platform.Foundation.create
import platform.Foundation.fileHandleForWritingToURL
import platform.Foundation.run
import platform.ScreenCaptureKit.SCContentFilter
import platform.ScreenCaptureKit.SCDisplay
import platform.ScreenCaptureKit.SCShareableContent
import platform.ScreenCaptureKit.SCStream
import platform.ScreenCaptureKit.SCStreamConfiguration
import platform.ScreenCaptureKit.SCStreamOutputProtocol
import platform.ScreenCaptureKit.SCStreamOutputType
import platform.darwin.NSObject
import platform.posix.sleep

@OptIn(ExperimentalForeignApi::class)
class RecordAudioCommand : CliktCommand() {
    override fun run() {
        memScoped {
            // Screencapturekitのセットアップ
            val captureConfiguration = SCStreamConfiguration().apply {
                showsCursor = false
                capturesAudio = true
            }

            SCShareableContent.getShareableContentWithCompletionHandler { content, error ->
                if (error != null) {
                    println("Error getting shareable content: ${error.localizedDescription}")
                    return@getShareableContentWithCompletionHandler
                }

                val display: SCDisplay? = content?.displays?.firstOrNull() as SCDisplay?
                if (display == null) {
                    println("No display found")
                    return@getShareableContentWithCompletionHandler
                }

                val contentFilter = SCContentFilter(display, excludingWindows = emptyList<Any>())
                val stream = SCStream(contentFilter, captureConfiguration, null)
                val outputFileURL = NSURL.fileURLWithPath("/tmp/output.m4a")
                val fileHandle = NSFileHandle.fileHandleForWritingToURL(outputFileURL, error = null)

                val streamOutput = object : NSObject(), SCStreamOutputProtocol {
                    //     @kotlinx.cinterop.ObjCMethod public open fun stream(
                    //     stream: platform.ScreenCaptureKit.SCStream,
                    //     didOutputSampleBuffer: platform.CoreMedia.CMSampleBufferRef? /* = kotlinx.cinterop.CPointer<cnames.structs.opaqueCMSampleBuffer>? */,
                    //     ofType: platform.ScreenCaptureKit.SCStreamOutputType
                    //     ): kotlin.Unit { /* compiled code */ }
                    override fun stream(
                        stream: SCStream,
                        didOutputSampleBuffer: CMSampleBufferRef?,
                        ofType: SCStreamOutputType
                    ) {
                        if (!CMSampleBufferIsValid(didOutputSampleBuffer)) {
                            eprintln("Invalid sample buffer")
                            return
                        }

                        // Handle the sample buffer, e.g., write to file
                        println("Sample buffer received $ofType")

                        val blockBufferRef = CMSampleBufferGetDataBuffer(didOutputSampleBuffer)
                        if (blockBufferRef != null) {
                            val dataLength = CMBlockBufferGetDataLength(blockBufferRef)
                            val data = ByteArray(dataLength.convert())
                            CMBlockBufferCopyDataBytes(blockBufferRef, 0.convert(), dataLength, data.refTo(0))
                            fileHandle?.writeData(
                                NSMutableData.create(data.refTo(0), data.size.convert()),
                                null
                            )
                        }
                    }
                }

                stream.addStreamOutput(
                    streamOutput,
                    SCStreamOutputType.SCStreamOutputTypeAudio,
                    sampleHandlerQueue = null,
                    error = null
                )

                stream.startCaptureWithCompletionHandler { error ->
                    if (error != null) {
                        println("Failed to start capture: ${error.localizedDescription}")
                        return@startCaptureWithCompletionHandler
                    }
                    println("Capture started")
                }

                println("Recording... Press ENTER to stop.")
//                readLine()

                sleep(10u)

                stream.stopCaptureWithCompletionHandler { error ->
                    if (error != null) {
                        println("Failed to stop capture: ${error.localizedDescription}")
                    } else {
                        println("Capture stopped")
                    }
                }

                println("Recording stopped.")

                fileHandle?.closeFile()
            }

            // Run the main run loop to process the asynchronous callback
            NSRunLoop.mainRunLoop().run()
        }
    }
}
