package capjoy.recorder

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.autoreleasepool
import platform.CoreGraphics.CGRectGetHeight
import platform.CoreGraphics.CGRectGetWidth
import platform.ScreenCaptureKit.SCContentFilter
import platform.ScreenCaptureKit.SCDisplay
import platform.ScreenCaptureKit.SCShareableContent
import platform.ScreenCaptureKit.SCWindow
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

data class Target(
    val contentFilter: SCContentFilter,
    val width: ULong,
    val height: ULong,
)

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
suspend fun findTarget(
    displayId: UInt?,
    windowId: UInt?,
): Target {
    if (windowId != null) {
        val content = getSharableContent()
        val window = content.windows.firstOrNull { window ->
            window is SCWindow && window.windowID == windowId
        } as SCWindow? ?: error("")

        println("Window ${window.windowID}")
        val contentFilter = SCContentFilter(desktopIndependentWindow = window)
        val width = CGRectGetWidth(window.frame()).toULong()
        val height = CGRectGetHeight(window.frame()).toULong()
        return Target(contentFilter, width, height)
    } else {
        val content = getSharableContent()
        val display = if (displayId != null) {
            (
                content.displays.firstOrNull { display ->
                    display is SCDisplay && display.displayID.toLong() == displayId.toLong()
                } ?: error("No display found for display id: $displayId")
            ) as SCDisplay
        } else {
            content.defaultDisplay()
        }

        println("Display ${display.displayID}: ${display.width}x${display.height} - ${display.description}")
        // To capture the entire display with ScreenCaptureKit, use initWithDisplay:includingApplications:exceptingWindows:
        // instead of initWithDisplay:excludingWindows: with an empty list, as the latter does not work correctly.
        // This workaround involves listing all running applications.
        // For more details, see: https://federicoterzi.com/blog/screencapturekit-failing-to-capture-the-entire-display/
        val contentFilter = SCContentFilter(
            display,
            includingApplications = content.applications,
            emptyList<Any>(),
        )
        val width = display.width.toULong()
        val height = display.height.toULong()
        return Target(contentFilter, width, height)
    }
}

fun SCShareableContent.defaultDisplay(): SCDisplay = this.displays.firstOrNull() as? SCDisplay ?: error("No display found")

@BetaInteropApi
suspend fun getSharableContent(): SCShareableContent =
    suspendCoroutine { cont ->
        autoreleasepool {
            SCShareableContent.getShareableContentWithCompletionHandler { content: SCShareableContent?, error ->
                if (error != null) {
                    cont.resumeWithException(Exception("Error in getShareableContentWithCompletionHandler: ${error.localizedDescription}"))
                }
                if (content == null) {
                    cont.resumeWithException(NullPointerException("No content found."))
                }

                cont.resume(content!!)
            }
        }
    }
