package capjoy.command.capture

import capjoy.BINARY_PATH
import capjoy.createTempFile
import capjoy.runCommand
import capjoy.runOnLocalOnly
import capjoy.utils.getFileSize
import kotlin.experimental.ExperimentalNativeApi
import kotlin.test.Test

class CaptureAudioCommandTest {
    @OptIn(ExperimentalNativeApi::class)
    @Test
    fun test() =
        runOnLocalOnly {
            val tmpFile = createTempFile() + ".m4a"
            val (exitCode, output) = runCommand("$BINARY_PATH capture-audio --duration 1s $tmpFile")
            assert(exitCode == 0)
            assert(output.contains("Capture stopped"))
            println(output)

            getFileSize(tmpFile).also {
                assert(it > 3000)
            }
        }
}
