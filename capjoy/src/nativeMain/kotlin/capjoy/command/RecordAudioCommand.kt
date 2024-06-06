package capjoy.command

import capjoy.eprintln
import com.github.ajalt.clikt.core.CliktCommand
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import platform.CoreMedia.CMBlockBufferCopyDataBytes
import platform.CoreMedia.CMBlockBufferGetDataLength
import platform.CoreMedia.CMSampleBufferGetDataBuffer
import platform.CoreMedia.CMSampleBufferIsValid
import platform.CoreMedia.CMSampleBufferRef
import platform.Foundation.NSData
import platform.Foundation.NSFileHandle
import platform.Foundation.NSFileManager
import platform.Foundation.NSRunLoop
import platform.Foundation.closeFile
import platform.Foundation.create
import platform.Foundation.fileHandleForWritingAtPath
import platform.Foundation.run
import platform.ScreenCaptureKit.SCContentFilter
import platform.ScreenCaptureKit.SCDisplay
import platform.ScreenCaptureKit.SCShareableContent
import platform.ScreenCaptureKit.SCStream
import platform.ScreenCaptureKit.SCStreamConfiguration
import platform.ScreenCaptureKit.SCStreamOutputProtocol
import platform.ScreenCaptureKit.SCStreamOutputType
import platform.darwin.NSObject
import platform.posix.exit
import platform.posix.sleep

@OptIn(ExperimentalForeignApi::class)
class RecordAudioCommand : CliktCommand() {
    @BetaInteropApi
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
                val fileHandle = memScoped {
                    val file = "/tmp/output.m4a"
                    if (!NSFileManager.defaultManager.fileExistsAtPath(file)) {
                        NSFileManager.defaultManager.createFileAtPath(file, null, null)
                    }
                    val fileHandle = NSFileHandle.fileHandleForWritingAtPath(file)
                    if (fileHandle == null) {
                        println("Failed to create file handle")
                        exit(1)
                    }
                    fileHandle
                }

                val streamOutput = object : NSObject(), SCStreamOutputProtocol {
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
                            val nsData = NSData.create(
                                bytes = data.refTo(0).getPointer(this@memScoped),
                                length = data.size.convert()
                            )
                            println("Writing $nsData to file $fileHandle")
                            fileHandle?.writeData(
                                nsData,
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

                    println("Recording... Press ENTER to stop.")
                    // Uncomment this line if you want to stop recording manually by pressing ENTER
                    // readLine()

                    sleep(10u)
                    println("Sleeped...")

                    stream.stopCaptureWithCompletionHandler { error ->
                        if (error != null) {
                            println("Failed to stop capture: ${error.localizedDescription}")
                        } else {
                            println("Capture stopped")

                            fileHandle?.closeFile()

                            exit(0)
                        }
                    }
                }
            }

            // Run the main run loop to process the asynchronous callback
            NSRunLoop.mainRunLoop().run()
        }
    }
}
