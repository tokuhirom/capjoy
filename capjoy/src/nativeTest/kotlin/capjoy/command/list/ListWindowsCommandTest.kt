package capjoy.command.list

import capjoy.BINARY_PATH
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
}
