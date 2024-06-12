package capjoy.command

import capjoy.BINARY_PATH
import capjoy.runCommand
import kotlin.experimental.ExperimentalNativeApi
import kotlin.test.Test

class VersionCommandTest {
    @OptIn(ExperimentalNativeApi::class)
    @Test
    fun testVersion() {
        val (exitCode, output) = runCommand("$BINARY_PATH version")
        assert(exitCode == 0)
        assert(output.contains("Capjoy"))
    }
}
