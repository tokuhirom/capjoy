package capjoy.command.capture

import capjoy.BINARY_PATH
import capjoy.runCommand
import capjoy.runOnLocalOnly
import kotlin.experimental.ExperimentalNativeApi
import kotlin.test.Test

class CaptureVideoCommandTest {
    @OptIn(ExperimentalNativeApi::class)
    @Test
    fun testDefaultDisplay() =
        runOnLocalOnly {
            val tmpFile = createTempFile() + ".mov"
            val (exitCode, output) = runCommand("$BINARY_PATH capture-video --duration 1s $tmpFile")
            assert(exitCode == 0)
            assert(output.contains("Capture stopped"))
            println(output)

            getFileSize(tmpFile).also {
                assert(it > 3000)
            }
        }
}
