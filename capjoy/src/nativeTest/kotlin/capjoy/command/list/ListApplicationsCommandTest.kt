package capjoy.command.list

import capjoy.BINARY_PATH
import capjoy.getJsonData
import capjoy.model.command.ListApplicationsOutput
import capjoy.runCommand
import capjoy.runOnLocalOnly
import kotlinx.cinterop.ExperimentalForeignApi
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

    @OptIn(ExperimentalNativeApi::class, ExperimentalForeignApi::class)
    @Test
    fun testJson() = runOnLocalOnly {
        val data = getJsonData<ListApplicationsOutput>("$BINARY_PATH list-applications --format=json")
        assert(data.applications.isNotEmpty())
        data.applications.forEach {
            println(it)
        }
    }
}
