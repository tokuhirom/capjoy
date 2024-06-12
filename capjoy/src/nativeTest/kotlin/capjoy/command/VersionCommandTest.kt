package capjoy.command

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.refTo
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import platform.posix.STDOUT_FILENO
import platform.posix.close
import platform.posix.dup2
import platform.posix.execlp
import platform.posix.exit
import platform.posix.fork
import platform.posix.perror
import platform.posix.pipe
import platform.posix.read
import platform.posix.waitpid
import kotlin.experimental.ExperimentalNativeApi
import kotlin.test.Test

@OptIn(ExperimentalForeignApi::class)
fun waitForProcess(pid: Int): Int {
    memScoped {
        val status = alloc<IntVar>()
        waitpid(pid, status.ptr, 0)
        return (status.value and 0xff00) shr 8
    }
}

@OptIn(ExperimentalForeignApi::class)
fun runCommand(command: String): Pair<Int, String> {
    memScoped {
        val pipe = allocArray<IntVar>(2)
        if (pipe(pipe) != 0) {
            perror("pipe")
            return -1 to "Failed to create pipe"
        }

        val pid = fork()
        if (pid < 0) {
            perror("fork")
            return -1 to "Failed to fork process"
        } else if (pid == 0) {
            // child process
            close(pipe[0])
            dup2(pipe[1], STDOUT_FILENO)
            close(pipe[1])
            execlp("/bin/sh", "sh", "-c", command, null)
            perror("execlp")
            exit(1)
        } else {
            // parent process
            close(pipe[1])
            val buffer = ByteArray(1024)
            val output = StringBuilder()
            while (true) {
                val bytesRead = read(pipe[0], buffer.refTo(0), buffer.size.toULong())
                if (bytesRead <= 0) break
                output.append(buffer.toKString())
            }
            close(pipe[0])

            val exitCode = waitForProcess(pid)
            return exitCode to output.toString()
        }
    }
    error("Unreachable")
}

const val BINARY_PATH = "./build/bin/native/debugExecutable/capjoy.kexe"

class VersionCommandTest {
    @OptIn(ExperimentalNativeApi::class)
    @Test
    fun testVersion() {
        val (exitCode, output) = runCommand("$BINARY_PATH version")
        assert(exitCode == 0)
        assert(output.contains("Capjoy"))
    }
}
