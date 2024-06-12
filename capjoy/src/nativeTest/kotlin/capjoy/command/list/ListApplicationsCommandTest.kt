package capjoy.command.list

import capjoy.BINARY_PATH
import capjoy.runCommand
import capjoy.runOnLocalOnly
import kotlin.experimental.ExperimentalNativeApi
import kotlin.test.Test

class ListApplicationsCommandTest {
    @OptIn(ExperimentalNativeApi::class)
    @Test
    fun test() =
        runOnLocalOnly {
            val (exitCode, output) = runCommand("$BINARY_PATH list-applications")
            assert(exitCode == 0)
            assert(output.contains("Process ID"))
        }
}
