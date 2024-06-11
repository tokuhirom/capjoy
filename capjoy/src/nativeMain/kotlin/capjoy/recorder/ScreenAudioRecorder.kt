package capjoy.recorder

import capjoy.eprintln
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVEncoderBitRateKey
import platform.AVFAudio.AVFormatIDKey
import platform.AVFAudio.AVNumberOfChannelsKey
import platform.AVFAudio.AVSampleRateKey
import platform.AVFoundation.AVAssetWriter
import platform.AVFoundation.AVAssetWriterInput
import platform.AVFoundation.AVFileTypeAppleM4A
import platform.AVFoundation.AVMediaTypeAudio
import platform.CoreAudioTypes.kAudioFormatMPEG4AAC
import platform.CoreMedia.CMSampleBufferIsValid
import platform.CoreMedia.CMSampleBufferRef
import platform.CoreMedia.CMTimeMake
import platform.Foundation.NSURL
import platform.ScreenCaptureKit.SCContentFilter
import platform.ScreenCaptureKit.SCDisplay
import platform.ScreenCaptureKit.SCShareableContent
import platform.ScreenCaptureKit.SCStream
import platform.ScreenCaptureKit.SCStreamConfiguration
import platform.ScreenCaptureKit.SCStreamOutputProtocol
import platform.ScreenCaptureKit.SCStreamOutputType
import platform.darwin.NSObject
import platform.posix.exit

fun findDefaultDisplay(displayCallback: (SCDisplay) -> Unit) {
    SCShareableContent.getShareableContentWithCompletionHandler { content, error ->
        if (error != null) {
            println("Error getting shareable content: ${error.localizedDescription}")
            return@getShareableContentWithCompletionHandler
        }

        val display: SCDisplay? = content?.displays?.firstOrNull() as? SCDisplay
        if (display == null) {
            println("No display found")
            return@getShareableContentWithCompletionHandler
        }

        displayCallback(display)
    }
}

@OptIn(ExperimentalForeignApi::class)
fun createAssetWriter(fileName: String, audioWriterInput: AVAssetWriterInput): AVAssetWriter {
    val outputFileURL = NSURL.fileURLWithPath(fileName)
    println("Output file: ${outputFileURL.path}")

    val assetWriter =
        AVAssetWriter(outputFileURL, fileType = AVFileTypeAppleM4A, error = null)
    assetWriter.addInput(audioWriterInput)

    if (!assetWriter.startWriting()) {
        println("Failed to start writing: ${assetWriter.error?.localizedDescription}")
        exit(1)
    }

    assetWriter.startSessionAtSourceTime(CMTimeMake(value = 0, timescale = 1))
    return assetWriter
}

@OptIn(ExperimentalForeignApi::class)
fun createAudioWriterInput(): AVAssetWriterInput {
    val audioSettings = mapOf<Any?, Any?>(
        AVFormatIDKey to kAudioFormatMPEG4AAC,
        AVNumberOfChannelsKey to 1,
        AVSampleRateKey to 44100.0,
        AVEncoderBitRateKey to 64000,
    )
    return AVAssetWriterInput(
        mediaType = AVMediaTypeAudio,
        outputSettings = audioSettings,
        sourceFormatHint = null,
    )
}

@OptIn(ExperimentalForeignApi::class)
fun startScreenRecord(
    fileName: String,
    contentFilter: SCContentFilter,
    callback: (ScreenRecorder) -> Unit,
) {
    val captureConfiguration = SCStreamConfiguration().apply {
        showsCursor = false
        capturesAudio = true
    }

    val stream = SCStream(contentFilter, captureConfiguration, null)

    val audioWriterInput = createAudioWriterInput()
    val assetWriter = createAssetWriter(fileName, audioWriterInput)

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

            when (ofType) {
                SCStreamOutputType.SCStreamOutputTypeAudio -> {
                    if (audioWriterInput.readyForMoreMediaData) {
                        audioWriterInput.appendSampleBuffer(didOutputSampleBuffer!!)
                    }
                }

                SCStreamOutputType.SCStreamOutputTypeScreen -> TODO()
            }
        }
    }

    stream.addStreamOutput(
        streamOutput,
        SCStreamOutputType.SCStreamOutputTypeAudio,
        sampleHandlerQueue = null,
        error = null,
    )

    stream.startCaptureWithCompletionHandler { error ->
        if (error != null) {
            error("Failed to start capture: ${error.localizedDescription}")
        }

        callback(ScreenRecorder(stream, audioWriterInput, assetWriter))
    }
}

data class ScreenRecorder(
    val stream: SCStream,
    val audioWriterInput: AVAssetWriterInput,
    val assetWriter: AVAssetWriter,
) {
    fun stop(callback: () -> Unit) {
        stream.stopCaptureWithCompletionHandler { error ->
            if (error != null) {
                println("Failed to stop capture: ${error.localizedDescription}")
            } else {
                println("Capture stopped")
                audioWriterInput.markAsFinished()
                assetWriter.finishWritingWithCompletionHandler {
                    callback()
                }
            }
        }
    }
}
