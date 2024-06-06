import capjoy.command.ApplicationsCommand
import capjoy.command.Capjoy
import capjoy.command.DisplaysCommand
import capjoy.command.ImageCommand
import capjoy.command.MixCommand
import capjoy.command.RecordAudioCommand
import capjoy.command.RecordMicCommand
import capjoy.command.RecordMixCommand
import capjoy.command.WindowsCommand
import com.github.ajalt.clikt.core.subcommands

fun main(args: Array<String>) {
    Capjoy().subcommands(
        DisplaysCommand(),
        ApplicationsCommand(),
        WindowsCommand(),
        ImageCommand(),
        RecordMicCommand(),
        RecordAudioCommand(),
        MixCommand(),
        RecordMixCommand()
    ).main(args)
}
