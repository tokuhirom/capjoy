package capjoy.command.capture

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.perror
import platform.posix.stat
import kotlin.random.Random

fun createTempFile(): String {
    val l = Random.nextLong()
    return "/tmp/capjoy-tmp-$l"
}

@OptIn(ExperimentalForeignApi::class)
fun getFileSize(fileName: String): Long {
    memScoped {
        val fileStat = alloc<stat>()
        if (stat(fileName, fileStat.ptr) != 0) {
            perror("stat")
            throw RuntimeException("Failed to get file size")
        }
        return fileStat.st_size
    }
}
