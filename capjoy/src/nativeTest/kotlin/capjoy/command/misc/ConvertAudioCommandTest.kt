package capjoy.command.misc

import capjoy.BINARY_PATH
import capjoy.createTempFile
import capjoy.runCommand
import capjoy.runOnLocalOnly
import capjoy.utils.getFileSize
import platform.posix.system
import kotlin.experimental.ExperimentalNativeApi
import kotlin.test.Test

class ConvertAudioCommandTest {
    @OptIn(ExperimentalNativeApi::class)
    @Test
    fun test() =
        runOnLocalOnly {
            println("Starting capturing audio ...")
            val inputFile = createTempFile() + ".m4a"
            val (exitCode, output) = runCommand("$BINARY_PATH capture-audio --duration 1s $inputFile")
            assert(exitCode == 0)
            assert(output.contains("Capture stopped"))
            println(output)

            getFileSize(inputFile).also {
                assert(it > 100)
            }

            listOf("m4a").forEach { outputFormat ->
                val outFile = createTempFile() + "." + outputFormat
                val (exitCode, output) = runCommand(
                    "$BINARY_PATH mix $inputFile" +
                        " -o $outFile",
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
