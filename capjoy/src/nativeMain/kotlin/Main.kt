import capjoy.command.ApplicationsCommand
import capjoy.command.Capjoy
import capjoy.command.DisplaysCommand
import capjoy.command.WindowsCommand
import com.github.ajalt.clikt.core.subcommands


fun main(args: Array<String>) {
    Capjoy().subcommands(
        DisplaysCommand(),
        ApplicationsCommand(),
        WindowsCommand(),
    ).main(args)
}
