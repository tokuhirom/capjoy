package capjoy.command

import capjoy.model.CaptureDevice
import capjoy.model.CaptureDeviceFormat
import capjoy.model.CaptureDevices
import com.github.ajalt.clikt.core.CliktCommand
import kotlinx.cinterop.BetaInteropApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceFormat
import platform.AVFoundation.AVMediaTypeAudio

class CaptureDevicesCommand : CliktCommand() {
    private val json =
        Json {
            prettyPrint = true
        }

    @BetaInteropApi
    override fun run() {
        val defaultDevice = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeAudio)
        println("Default device is: ${defaultDevice?.localizedName}")

        val devices = AVCaptureDevice.devicesWithMediaType(AVMediaTypeAudio)
        val got = devices.map {
            it as AVCaptureDevice
        }.map {
            CaptureDevice(
                formats = it.formats.map { it as AVCaptureDeviceFormat }.map { captureDeviceFormat ->
                    CaptureDeviceFormat(captureDeviceFormat.description, captureDeviceFormat.mediaType)
                },
                localizedName = it.localizedName,
                manufacturer = it.manufacturer,
                uniqueID = it.uniqueID,
                suspended = it.suspended,
                transportType = it.transportType,
                modelID = it.modelID,
            )
        }
        println(json.encodeToString(CaptureDevices(got)))
    }
}
