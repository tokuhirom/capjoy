package capjoy.command

import capjoy.eprintln
import com.github.ajalt.clikt.core.CliktCommand
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import platform.AVFAudio.AVEncoderBitRateKey
import platform.AVFAudio.AVFormatIDKey
import platform.AVFAudio.AVNumberOfChannelsKey
import platform.AVFAudio.AVSampleRateKey
import platform.AVFoundation.AVAssetWriter
import platform.AVFoundation.AVAssetWriterInput
import platform.AVFoundation.AVFileTypeAppleM4A
import platform.AVFoundation.AVMediaType
import platform.AVFoundation.AVMediaTypeAudio
import platform.CoreAudioTypes.kAudioFormatMPEG4AAC
import platform.CoreMedia.CMSampleBufferIsValid
import platform.CoreMedia.CMSampleBufferRef
import platform.CoreMedia.CMTimeMake
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
import platform.posix.exit
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
                val assetWriter = AVAssetWriter(outputFileURL, fileType = AVFileTypeAppleM4A, error = null)
                val audioSettings: Map<Any?, *> = mapOf(
                    AVFormatIDKey to kAudioFormatMPEG4AAC,
                    AVNumberOfChannelsKey to 1,
                    AVSampleRateKey to 44100.0,
                    AVEncoderBitRateKey to 64000
                )
                val mediaType: AVMediaType? = AVMediaTypeAudio
                val assetWriterInput = AVAssetWriterInput(
                    mediaType = mediaType,
                    outputSettings = audioSettings,
                    sourceFormatHint = null,
                )
                assetWriter.addInput(assetWriterInput)

                if (!assetWriter.startWriting()) {
                    println("Failed to start writing: ${assetWriter.error?.localizedDescription}")
                    exit(1)
                }

                assetWriter.startSessionAtSourceTime(CMTimeMake(value = 0, timescale = 1))

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

                        if (assetWriterInput.readyForMoreMediaData) {
                            assetWriterInput.appendSampleBuffer(didOutputSampleBuffer!!)
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
                            assetWriterInput.markAsFinished()
                            assetWriter.finishWritingWithCompletionHandler {
                                println("Writing finished")
                                exit(0)
                            }
                        }
                    }
                }
            }

            // Run the main run loop to process the asynchronous callback
            NSRunLoop.mainRunLoop().run()
        }
    }
}
