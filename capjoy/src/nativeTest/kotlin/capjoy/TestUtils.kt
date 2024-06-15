package capjoy

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.json.Json
import platform.posix.getenv
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalForeignApi::class)
fun runOnLocalOnly(function: () -> Unit) {
    if (getenv("CI") == null) {
        function()
    } else {
        println("Skipping test on CI")
    }
}

@OptIn(ExperimentalNativeApi::class)
inline fun <reified T> getJsonData(cmd: String): T {
    val builder = ProcessBuilder(cmd)
    val process = builder.start()
    val stdout = process.stdout!!.slurpString()
    val stderr = process.stderr?.slurpString()
    val exitCode = process.wait()
    println(stderr)
    assert(exitCode == 0)
    return Json.decodeFromString<T>(stdout)
}
