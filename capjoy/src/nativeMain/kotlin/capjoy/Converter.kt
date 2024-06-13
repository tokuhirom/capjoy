package capjoy

import capjoy.model.entity.Application
import capjoy.model.entity.Rect
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectGetHeight
import platform.CoreGraphics.CGRectGetWidth
import platform.ScreenCaptureKit.SCRunningApplication

fun SCRunningApplication.toModel(): Application {
    return Application(
        applicationName = this.applicationName,
        bundleIdentifier = this.bundleIdentifier,
        processID = this.processID.toLong(),
    )
}

@OptIn(ExperimentalForeignApi::class)
fun CValue<CGRect>.toModel(): Rect {
    return Rect(
        width = CGRectGetWidth(this),
        height = CGRectGetHeight(this),
    )
}
