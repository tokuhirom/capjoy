package capjoy.command.misc

import capjoy.BINARY_PATH
import capjoy.command.capture.createTempFile
import capjoy.command.capture.getFileSize
import capjoy.runCommand
import capjoy.runOnLocalOnly
import platform.posix.system
import kotlin.experimental.ExperimentalNativeApi
import kotlin.test.Test

class MixCommandTest {
    @OptIn(ExperimentalNativeApi::class)
    @Test
    fun test() =
        runOnLocalOnly {
            val inputFiles = (0..1).map { i ->
                println("Starting capturing audio $i ...")
                val tmpFile = createTempFile() + ".m4a"
                val (exitCode, output) = runCommand("$BINARY_PATH capture-audio --duration 1s $tmpFile")
                assert(exitCode == 0)
                assert(output.contains("Capture stopped"))
                println(output)

                getFileSize(tmpFile).also {
                    assert(it > 3000)
                }
                tmpFile
            }

            listOf("m4a").forEach { outputFormat ->
                val outFile = createTempFile() + "." + outputFormat
                val (exitCode, output) = runCommand(
                    "$BINARY_PATH mix ${inputFiles.joinToString(" ")}" +
                        " --format=$outputFormat -o $outFile",
                )
                assert(output.contains("Mixing completed successfully"))
                assert(exitCode == 0)
                println(output)

                getFileSize(outFile).also {
                    assert(it > 3000)
                }
                system("file $outFile")
            }
        }
}
