import capjoy.command.CapjoyCommand
import capjoy.command.VersionCommand
import capjoy.command.capture.CaptureAudioCommand
import capjoy.command.capture.CaptureImageCommand
import capjoy.command.capture.CaptureMicCommand
import capjoy.command.capture.CaptureMixCommand
import capjoy.command.list.ListApplicationsCommand
import capjoy.command.list.ListCaptureDevicesCommand
import capjoy.command.list.ListDisplaysCommand
import capjoy.command.list.ListWindowsCommand
import capjoy.command.misc.MixCommand
import com.github.ajalt.clikt.core.subcommands

fun main(args: Array<String>) {
    CapjoyCommand().subcommands(
        ListDisplaysCommand(),
        ListApplicationsCommand(),
        ListWindowsCommand(),
        ListCaptureDevicesCommand(),
        CaptureImageCommand(),
        CaptureMicCommand(),
        CaptureAudioCommand(),
        MixCommand(),
        CaptureMixCommand(),
        VersionCommand(),
    ).main(args)
}
