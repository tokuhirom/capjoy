import com.github.ajalt.clikt.core.subcommands
import command.Capjoy
import command.DisplaysCommand


fun main(args: Array<String>) {
    Capjoy().subcommands(DisplaysCommand()).main(args)
}
