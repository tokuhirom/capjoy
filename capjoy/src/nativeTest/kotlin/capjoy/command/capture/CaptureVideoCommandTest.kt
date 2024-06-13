package capjoy.command.capture

import capjoy.BINARY_PATH
import capjoy.ProcessBuilder
import capjoy.createTempFile
import capjoy.model.command.ListWindowsOutput
import capjoy.runOnLocalOnly
import capjoy.utils.getFileSize
import kotlinx.serialization.json.Json
import kotlin.experimental.ExperimentalNativeApi
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class CaptureVideoCommandTest {
    // this test case is flaky...
    @OptIn(ExperimentalNativeApi::class)
    @Test
    fun test() =
        runOnLocalOnly {
            val windows = readWindowList()
            val filteredWindows = windows.windows.filter {
                it.active && it.onScreen && it.owningApplication?.applicationName?.isNotEmpty() == true
            }.sortedByDescending { it.frame.width }
            val window = filteredWindows.firstOrNull {
                it.owningApplication?.bundleIdentifier == "com.google.Chrome"
            } ?: filteredWindows.firstOrNull()!!
            println("Capturing window: $window")

            val tmpFile = createTempFile() + ".mov"
            val builder = ProcessBuilder(
                "$BINARY_PATH capture-video" +
                    " --window-id=${window.windowID} --duration 3s $tmpFile",
            )
            val process = builder.start()
            val stdout = process.readStdout()
            val stderr = process.readStderr()
            val exitCode = process.waitUntil(30.seconds)
            val output = "stdout: $stdout\nstderr: $stderr"
            if (exitCode != 0) {
                println("exitCode: $exitCode output: $output")
            }
            assert(exitCode == 0)
            assert(output.contains("Capture stopped"))
            println(output)

            getFileSize(tmpFile).also {
                assert(it > 3000)
            }
        }

    @OptIn(ExperimentalNativeApi::class)
    fun readWindowList(): ListWindowsOutput {
        val builder = ProcessBuilder("$BINARY_PATH list-windows --format=json")
        val process = builder.start()
        val stdout = process.readStdout()
        val stderr = process.readStderr()
        val exitCode = process.wait()
        if (exitCode != 0) {
            println("exitCode: $exitCode stdout: $stdout stderr: $stderr")
        }

        assert(exitCode == 0)
        return Json.decodeFromString<ListWindowsOutput>(stdout)
    }
}
