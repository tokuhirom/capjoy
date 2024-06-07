package capjoy.command

import com.github.ajalt.clikt.core.CliktCommand

class VersionCommand : CliktCommand("Print the version of Capjoy.") {
    override fun run() {
        println("Capjoy __VERSION__")
    }
}
