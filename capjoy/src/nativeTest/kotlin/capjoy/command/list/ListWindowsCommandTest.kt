package capjoy.command.list

import capjoy.BINARY_PATH
import capjoy.getJsonData
import capjoy.model.command.ListWindowsOutput
import capjoy.runCommand
import capjoy.runOnLocalOnly
import kotlin.experimental.ExperimentalNativeApi
import kotlin.test.Test

class ListWindowsCommandTest {
    @OptIn(ExperimentalNativeApi::class)
    @Test
    fun testListWindows() =
        runOnLocalOnly {
            val (exitCode, output) = runCommand("$BINARY_PATH list-windows")
            assert(exitCode == 0)
            assert(output.contains("Window ID"))
        }

    @OptIn(ExperimentalNativeApi::class)
    @Test
    fun testJson() =
        runOnLocalOnly {
            val data = getJsonData<ListWindowsOutput>("$BINARY_PATH list-windows --format=json")
            assert(data.windows.isNotEmpty())
            data.windows.forEach {
                println(it)
            }
        }
}
