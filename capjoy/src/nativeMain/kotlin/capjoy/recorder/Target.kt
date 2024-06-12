package capjoy.recorder

import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectGetHeight
import platform.CoreGraphics.CGRectGetWidth
import platform.ScreenCaptureKit.SCContentFilter
import platform.ScreenCaptureKit.SCDisplay
import platform.ScreenCaptureKit.SCShareableContent
import platform.ScreenCaptureKit.SCWindow
import platform.posix.exit

data class Target(
    val contentFilter: SCContentFilter,
    val width: ULong,
    val height: ULong,
)

@OptIn(ExperimentalForeignApi::class)
fun findTarget(displayId: UInt?, windowId: UInt?, targetCallback: (Target) -> Unit) {
    if (windowId != null) {
        findWindowByWindowId(windowId) { window ->
            println("Window ${window.windowID}")
            val contentFilter = SCContentFilter(desktopIndependentWindow = window)
            val width = CGRectGetWidth(window.frame()).toULong()
            val height = CGRectGetHeight(window.frame()).toULong()
            targetCallback(Target(contentFilter, width, height))
        }
    } else {
        fun buildTarget(display: SCDisplay, apps: List<*>): Target {
            println("Display ${display.displayID}")
            // To capture the entire display with ScreenCaptureKit, use initWithDisplay:includingApplications:exceptingWindows:
            // instead of initWithDisplay:excludingWindows: with an empty list, as the latter does not work correctly.
            // This workaround involves listing all running applications.
            // For more details, see: https://federicoterzi.com/blog/screencapturekit-failing-to-capture-the-entire-display/
            val contentFilter = SCContentFilter(display, includingApplications = apps, emptyList<Any>())
            val width = display.width.toULong()
            val height = display.height.toULong()
            return Target(contentFilter, width, height)
        }

        if (displayId != null) {
            findDisplayByDisplayId(displayId.toLong()) { display, applications ->
                println("Display ${display.displayID}: ${display.width}x${display.height} - ${display.description}")
                targetCallback(buildTarget(display, applications))
            }
        } else {
            findDefaultDisplay { display, applications ->
                println("Display ${display.displayID}: ${display.width}x${display.height} - ${display.description}")
                targetCallback(buildTarget(display, applications))
            }
        }
    }
}

fun findDefaultDisplay(displayCallback: (SCDisplay, List<*>) -> Unit) {
    SCShareableContent.getShareableContentWithCompletionHandler { content, error ->
        if (error != null) {
            println("Error getting shareable content: ${error.localizedDescription}")
            return@getShareableContentWithCompletionHandler
        }

        val display: SCDisplay? = content?.displays?.firstOrNull() as? SCDisplay
        if (display == null) {
            println("No display found")
            return@getShareableContentWithCompletionHandler
        }

        displayCallback(display, content.applications)
    }
}

fun findDisplayByDisplayId(
    displayId: Long,
    displayCallback: (SCDisplay, List<*>) -> Unit,
) {
    SCShareableContent.getShareableContentWithCompletionHandler { content, error ->
        if (error != null) {
            println("Error getting shareable content: ${error.localizedDescription}")
            return@getShareableContentWithCompletionHandler
        }

        val display = content?.displays?.firstOrNull { display ->
            display is SCDisplay && display.displayID.toLong() == displayId
        }
        if (display != null && display is SCDisplay) {
            displayCallback(display, content.applications)
        } else {
            println("No display found for display id: $displayId")
            exit(1)
        }
    }
}

fun findWindowByWindowId(
    windowId: UInt,
    windowCallback: (SCWindow) -> Unit,
) {
    SCShareableContent.getShareableContentWithCompletionHandler { content, error ->
        if (error != null) {
            println("Error getting shareable content: ${error.localizedDescription}")
            return@getShareableContentWithCompletionHandler
        }

        val window = content?.windows?.firstOrNull { window ->
            window is SCWindow && window.windowID == windowId
        }
        if (window != null && window is SCWindow) {
            windowCallback(window)
        } else {
            println("No window found for window id: $windowId")
            exit(1)
        }
    }
}
