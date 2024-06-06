package capjoy.command

import capjoy.handleContent
import capjoy.model.Application
import capjoy.model.Applications
import capjoy.model.Displays
import com.github.ajalt.clikt.core.CliktCommand
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.ScreenCaptureKit.SCRunningApplication

class ApplicationsCommand: CliktCommand() {
    private val json = Json {
        prettyPrint = true
    }

    @BetaInteropApi
    override fun run() {
        handleContent { content ->
            val got = content.applications.map { application ->
                application as SCRunningApplication
            }.map {
                Application(
                    applicationName = it.applicationName,
                    bundleIdentifier = it.bundleIdentifier,
                    processID = it.processID.toLong(),
                )
            }
            println(json.encodeToString(Applications(got)))
        }
    }
}
