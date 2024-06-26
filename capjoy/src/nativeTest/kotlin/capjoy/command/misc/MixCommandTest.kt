package capjoy.command.misc

import capjoy.BINARY_PATH
import capjoy.createTempFile
import capjoy.runCommand
import capjoy.runOnLocalOnly
import capjoy.utils.getFileSize
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
                if (exitCode != 0) {
                    println("exitCode: $exitCode output: $output")
                }
                assert(exitCode == 0)
                assert(output.contains("Capture stopped"))
                println(output)

                getFileSize(tmpFile).also {
                    assert(it > 100)
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
                    assert(it > 100)
                }
                system("file $outFile")
            }
        }
}
