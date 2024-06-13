package capjoy.command.list

import capjoy.BINARY_PATH
import capjoy.getJsonData
import capjoy.model.command.ListDevicesOutput
import capjoy.runCommand
import capjoy.runOnLocalOnly
import kotlin.experimental.ExperimentalNativeApi
import kotlin.test.Test

class ListDevicesCommandTest {
    @OptIn(ExperimentalNativeApi::class)
    @Test
    fun testVersion() =
        runOnLocalOnly {
            val (exitCode, output) = runCommand("$BINARY_PATH list-devices")
            assert(exitCode == 0)
            assert(output.contains("Manufacturer"))
        }

    @OptIn(ExperimentalNativeApi::class)
    @Test
    fun testJson() = runOnLocalOnly {
        val devices = getJsonData<ListDevicesOutput>("$BINARY_PATH list-devices --format=json")
        assert(devices.devices.isNotEmpty())
        devices.devices.forEach {
            println(it)
        }
    }
}
