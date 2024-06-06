package capjoy

import capjoy.model.Application
import capjoy.model.Rect
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRect
import platform.ScreenCaptureKit.SCRunningApplication

fun SCRunningApplication.toModel() : Application {
    return Application(
        applicationName = this.applicationName,
        bundleIdentifier = this.bundleIdentifier,
        processID = this.processID.toLong(),
    )
}

@OptIn(ExperimentalForeignApi::class)
fun CValue<CGRect>.toModel(): Rect {
    return Rect(
        size = this.size,
        align = this.align,
    )
}
