package capjoy

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
import platform.posix.STDERR_FILENO
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

const val BINARY_PATH = "./build/bin/native/debugExecutable/capjoy.kexe"

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
    println("Running '$command'")
    memScoped {
        val stdoutPipe = allocArray<IntVar>(2)
        val stderrPipe = allocArray<IntVar>(2)

        if (pipe(stdoutPipe) != 0) {
            perror("pipe")
            return -1 to "Failed to create stdout pipe"
        }
        if (pipe(stderrPipe) != 0) {
            perror("pipe")
            return -1 to "Failed to create stderr pipe"
        }

        val pid = fork()
        if (pid < 0) {
            perror("fork")
            return -1 to "Failed to fork process"
        } else if (pid == 0) {
            // child process
            close(stdoutPipe[0])
            close(stderrPipe[0])
            dup2(stdoutPipe[1], STDOUT_FILENO)
            dup2(stderrPipe[1], STDERR_FILENO)
            close(stdoutPipe[1])
            close(stderrPipe[1])
            execlp("/bin/sh", "sh", "-c", command, null)
            perror("execlp")
            exit(1)
        } else {
            // parent process
            close(stdoutPipe[1])
            close(stderrPipe[1])
            val buffer = ByteArray(1024)
            val output = StringBuilder()
            val errorOutput = StringBuilder()

            while (true) {
                val bytesRead = read(stdoutPipe[0], buffer.refTo(0), buffer.size.toULong())
                if (bytesRead <= 0) break
                output.append(buffer.toKString())
            }
            close(stdoutPipe[0])

            while (true) {
                val bytesRead = read(stderrPipe[0], buffer.refTo(0), buffer.size.toULong())
                if (bytesRead <= 0) break
                errorOutput.append(buffer.toKString())
            }
            close(stderrPipe[0])

            val exitCode = waitForProcess(pid)
            return exitCode to "stdout: $output\nstderr: $errorOutput"
        }
    }
    error("Unreachable")
}