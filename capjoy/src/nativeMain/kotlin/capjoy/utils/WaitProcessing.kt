package capjoy.utils

import kotlinx.coroutines.delay
import kotlin.time.Duration

const val WAITING_HELP =
    "Without the --duration option, the recording will continue until the Enter key is pressed."
const val DURATION_HELP = "Recording duration(e.g. 10s, 1h 30)"

suspend fun waitProcessing(durationString: String?) {
    if (durationString != null) {
        val duration = try {
            Duration.parse(durationString)
        } catch (e: Exception) {
            error(
                "Invalid duration format: $durationString\n" +
                    "Supported format is: 10s, 1h 30m, 1h 30m 30s, etc.",
            )
        }
        val seconds = duration.inWholeSeconds.toUInt()
        println("Waiting for $seconds seconds...")
        delay(duration)
        println("Time's up!")
    } else {
        println("Please press Enter to stop capturing process.")
        readlnOrNull()
        println("User input received.")
    }
}
