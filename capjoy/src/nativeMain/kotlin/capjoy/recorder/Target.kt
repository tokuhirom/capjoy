package capjoy.recorder

import platform.ScreenCaptureKit.SCDisplay
import platform.ScreenCaptureKit.SCShareableContent
import platform.ScreenCaptureKit.SCWindow
import platform.posix.exit

fun findTarget(displayId: UInt?, windowId: UInt?) {
    if (windowId != null) {
        findWindowByWindowId(windowId) { window ->
            println("Window ${window.windowID}")
        }
    } else {
        if (displayId != null) {
            findDisplayByDisplayId(displayId.toLong()) { display, applications ->
                println("Display ${display.displayID}: ${display.width}x${display.height} - ${display.description}")
            }
        } else {
            findDefaultDisplay { display, applications ->
                println("Display ${display.displayID}: ${display.width}x${display.height} - ${display.description}")
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
