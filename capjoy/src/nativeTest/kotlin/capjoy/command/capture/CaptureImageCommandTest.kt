package capjoy.command.capture

import capjoy.BINARY_PATH
import capjoy.createTempFile
import capjoy.runCommand
import capjoy.runOnLocalOnly
import capjoy.utils.getFileSize
import kotlin.experimental.ExperimentalNativeApi
import kotlin.test.Test

class CaptureImageCommandTest {
    @OptIn(ExperimentalNativeApi::class)
    @Test
    fun test() =
        runOnLocalOnly {
            val tmpFile = createTempFile() + ".jpg"
            val (exitCode, output) = runCommand("$BINARY_PATH capture-image $tmpFile")
            assert(exitCode == 0)
            assert(output.contains("Image saved"))
            println(output)

            getFileSize(tmpFile).also {
                assert(it > 3000)
            }
        }
}
