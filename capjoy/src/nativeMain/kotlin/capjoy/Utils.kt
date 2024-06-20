package capjoy

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSTemporaryDirectory
import platform.posix.fprintf
import platform.posix.stderr
import kotlin.random.Random

fun createTempFile(
    prefix: String,
    suffix: String,
): String {
    val tempDir = NSTemporaryDirectory()
    val fileName = "$prefix${Random.nextInt()}$suffix"
    val filePath = tempDir + fileName
    return filePath
}

@OptIn(ExperimentalForeignApi::class)
fun eprintln(message: String) {
    // print to stderr
    fprintf(stderr, "%s\n", message)
}
