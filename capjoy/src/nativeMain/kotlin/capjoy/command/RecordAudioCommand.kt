package capjoy.command

import capjoy.eprintln
import com.github.ajalt.clikt.core.CliktCommand
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import platform.AVFoundation.AVCaptureAudioFileOutput
import platform.AVFoundation.AVCaptureFileOutput
import platform.AVFoundation.AVCaptureFileOutputRecordingDelegateProtocol
import platform.AVFoundation.AVCaptureScreenInput
import platform.AVFoundation.AVCaptureSession
import platform.CoreMedia.CMSampleBufferIsValid
import platform.CoreMedia.CMSampleBufferRef
import platform.Foundation.NSError
import platform.Foundation.NSRunLoop
import platform.Foundation.NSURL
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

            val display = SCShareableContent.getShareableContentWithCompletionHandler { content, error ->
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
                val outputFileURL = NSURL.fileURLWithPath("output.m4a")
                val audioOutput = AVCaptureAudioFileOutput()

                val captureSession = AVCaptureSession()
                val audioInput = AVCaptureScreenInput(display.displayID)

                if (captureSession.canAddInput(audioInput)) {
                    captureSession.addInput(audioInput)
                } else {
                    println("Failed to add audio input")
                    return@getShareableContentWithCompletionHandler
                }

                if (captureSession.canAddOutput(audioOutput)) {
                    captureSession.addOutput(audioOutput)
                } else {
                    println("Failed to add audio output")
                    return@getShareableContentWithCompletionHandler
                }
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
                    }
                }

                //     @kotlinx.cinterop.ObjCMethod public open external fun
                //     addStreamOutput(output: platform.ScreenCaptureKit.SCStreamOutputProtocol,
                //     type: platform.ScreenCaptureKit.SCStreamOutputType,
                //     sampleHandlerQueue: platform.darwin.dispatch_queue_t?
                //     /* = platform.darwin.NSObject? */,
                //     error: kotlinx.cinterop.CPointer<kotlinx.cinterop.ObjCObjectVar<platform.Foundation.NSError?>>?
                //     ): kotlin.Boolean { /* compiled code */ }
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

                captureSession.startRunning()

                val delegate = object : NSObject(), AVCaptureFileOutputRecordingDelegateProtocol {
                    override fun captureOutput(
                        output: AVCaptureFileOutput,
                        didFinishRecordingToOutputFileAtURL: NSURL,
                        fromConnections: List<*>,
                        error: NSError?
                    ) {
                        println("Recording finished")
                    }
                }

                println("Recording... Press ENTER to stop.")
//                readLine()

                sleep(7u)

                audioOutput.stopRecording()
                captureSession.stopRunning()

                println("Recording stopped.")
            }

            // Run the main run loop to process the asynchronous callback
            NSRunLoop.mainRunLoop().run()
        }
    }
}
