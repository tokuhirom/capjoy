package capjoy.command.list

import capjoy.BINARY_PATH
import capjoy.getJsonData
import capjoy.model.command.ListDisplayOutput
import capjoy.runCommand
import capjoy.runOnLocalOnly
import kotlin.experimental.ExperimentalNativeApi
import kotlin.test.Test

class ListDisplaysCommandTest {
    @OptIn(ExperimentalNativeApi::class)
    @Test
    fun testListDisplays() =
        runOnLocalOnly {
            val (exitCode, output) = runCommand("$BINARY_PATH list-displays")
            assert(exitCode == 0)
            assert(output.contains("Display ID"))
        }

    @OptIn(ExperimentalNativeApi::class)
    @Test
    fun testJson() =
        runOnLocalOnly {
            val displays = getJsonData<ListDisplayOutput>("$BINARY_PATH list-displays --format=json")
            assert(displays.displays.isNotEmpty())
            displays.displays.forEach {
                println(it)
            }
        }
}
