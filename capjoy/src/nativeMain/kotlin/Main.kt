import capjoy.command.Capjoy
import capjoy.command.CaptureImageCommand
import capjoy.command.ListApplicationsCommand
import capjoy.command.ListCaptureDevicesCommand
import capjoy.command.ListDisplaysCommand
import capjoy.command.ListWindowsCommand
import capjoy.command.MixCommand
import capjoy.command.RecordAudioCommand
import capjoy.command.RecordMicCommand
import capjoy.command.RecordMixCommand
import com.github.ajalt.clikt.core.subcommands

fun main(args: Array<String>) {
    Capjoy().subcommands(
        ListDisplaysCommand(),
        ListApplicationsCommand(),
        ListWindowsCommand(),
        ListCaptureDevicesCommand(),
        CaptureImageCommand(),
        RecordMicCommand(),
        RecordAudioCommand(),
        MixCommand(),
        RecordMixCommand(),
    ).main(args)
}
