package capjoy.command.list

import capjoy.command.list.utils.showTable
import capjoy.model.command.ListCaptureDevicesOutput
import capjoy.model.entity.CaptureDevice
import capjoy.model.entity.CaptureDeviceFormat
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import kotlinx.cinterop.BetaInteropApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceFormat
import platform.AVFoundation.AVMediaTypeAudio
import platform.AVFoundation.AVMediaTypeVideo

class ListDevicesCommand : CliktCommand(
    "List all capture devices",
) {
    private val format by option().choice("json", "table").default("table")
    private val json =
        Json {
            prettyPrint = true
        }

    @BetaInteropApi
    override fun run() {
        val defaultAudioDevice = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeAudio)
        val defaultVideoDevice = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)

        val devices = AVCaptureDevice.devices()
        val got = devices.map {
            it as AVCaptureDevice
        }.map {
            CaptureDevice(
                formats = it.formats
                    .map { format -> format as AVCaptureDeviceFormat }
                    .map { captureDeviceFormat ->
                        CaptureDeviceFormat(captureDeviceFormat.description, captureDeviceFormat.mediaType)
                    },
                localizedName = it.localizedName,
                manufacturer = it.manufacturer,
                uniqueID = it.uniqueID,
                suspended = it.suspended,
                transportType = it.transportType,
                modelID = it.modelID,
                default = it == defaultAudioDevice || it == defaultVideoDevice,
            )
        }
        when (format) {
            "json" -> println(json.encodeToString(ListCaptureDevicesOutput(got)))
            "table" -> {
                val headers = listOf(
                    "LocalizedName",
                    "Manufacturer",
                    "UniqueID",
                    "Suspended",
                    "TransportType",
                    "ModelID",
                    "Default",
                )
                val data = got.map {
                    listOf(
                        it.localizedName,
                        it.manufacturer,
                        it.uniqueID,
                        it.suspended.toString(),
                        it.transportType.toString(),
                        it.modelID,
                        if (it.default) "Default" else "",
                    )
                }
                showTable(headers, data)
            }
        }
    }
}
