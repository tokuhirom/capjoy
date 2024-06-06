package capjoy.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureAudioFileOutput
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureFileOutput
import platform.AVFoundation.AVCaptureFileOutputRecordingDelegateProtocol
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVFileTypeMPEG4
import platform.AVFoundation.AVFileTypeWAVE
import platform.AVFoundation.AVMediaTypeAudio
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.darwin.NSObject
import platform.posix.sleep

@OptIn(ExperimentalForeignApi::class)
class MicCommand : CliktCommand() {
    private val fileName: String by argument()
    private val format by option().choice("m4a", "wav").default("m4a")

    override fun run() {
        val outFormat =
            when (format) {
                "m4a" -> AVFileTypeMPEG4
                "wav" -> AVFileTypeWAVE
                else -> error("Unsupported format: $format")
            }

        val captureSession = AVCaptureSession()
        val audioDevice = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeAudio)
        val audioInput = AVCaptureDeviceInput.deviceInputWithDevice(audioDevice!!, null)

        if (captureSession.canAddInput(audioInput!!)) {
            captureSession.addInput(audioInput)
        } else {
            println("Failed to add audio input")
            return
        }

        val outputFileURL = NSURL.fileURLWithPath(fileName)
        val audioOutput = AVCaptureAudioFileOutput()

        if (captureSession.canAddOutput(audioOutput)) {
            captureSession.addOutput(audioOutput)
        } else {
            println("Failed to add audio output")
            return
        }

        captureSession.startRunning()

        val delegate =
            object : NSObject(), AVCaptureFileOutputRecordingDelegateProtocol {
                override fun captureOutput(
                    captureOutput: AVCaptureFileOutput,
                    didFinishRecordingToOutputFileAtURL: NSURL,
                    fromConnections: List<*>,
                    error: NSError?,
                ) {
                    println("Recording finished")
                }

                override fun captureOutput(
                    output: AVCaptureFileOutput,
                    didPauseRecordingToOutputFileAtURL: NSURL,
                    fromConnections: List<*>,
                ) {
                    println("Recording started $didPauseRecordingToOutputFileAtURL")
                }
            }

        audioOutput.startRecordingToOutputFileURL(outputFileURL, outFormat, delegate)

        println("Recording... Press ENTER to stop.")
        sleep(10u)

        audioOutput.stopRecording()
        captureSession.stopRunning()

        println("Recording stopped.")
    }
}
