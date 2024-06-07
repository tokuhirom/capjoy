package capjoy.model.entity

import kotlinx.serialization.Serializable

@Serializable
data class CaptureDeviceFormat(
    val description: String?,
    val mediaType: String?,
)

@Serializable
data class CaptureDevice(
    /*
        public final var activeFormat: platform.AVFoundation.AVCaptureDeviceFormat /* compiled code */
        public final external @kotlinx.cinterop.ObjCMethod get
        public final external @kotlinx.cinterop.ObjCMethod set(value: platform.AVFoundation.AVCaptureDeviceFormat) {/* compiled code */ }

    public final var activeInputSource: platform.AVFoundation.AVCaptureDeviceInputSource? /* compiled code */
        public final external @kotlinx.cinterop.ObjCMethod get
        public final external @kotlinx.cinterop.ObjCMethod set(value: platform.AVFoundation.AVCaptureDeviceInputSource?) {/* compiled code */ }

    public final var activeVideoMaxFrameDuration: kotlinx.cinterop.CValue<platform.CoreMedia.CMTime> /* compiled code */
        public final external @kotlinx.cinterop.ObjCMethod get
        public final external @kotlinx.cinterop.ObjCMethod set(value: kotlinx.cinterop.CValue<platform.CoreMedia.CMTime>) {/* compiled code */ }

    public final var activeVideoMinFrameDuration: kotlinx.cinterop.CValue<platform.CoreMedia.CMTime> /* compiled code */
        public final external @kotlinx.cinterop.ObjCMethod get
        public final external @kotlinx.cinterop.ObjCMethod set(value: kotlinx.cinterop.CValue<platform.CoreMedia.CMTime>) {/* compiled code */ }

    public final val connected: kotlin.Boolean /* compiled code */
        public final external @kotlinx.cinterop.ObjCMethod get

    public final val formats: kotlin.collections.List<*> /* compiled code */
        public final external @kotlinx.cinterop.ObjCMethod get

    public final val inUseByAnotherApplication: kotlin.Boolean /* compiled code */
        public final external @kotlinx.cinterop.ObjCMethod get

    public final val inputSources: kotlin.collections.List<*> /* compiled code */
        public final external @kotlinx.cinterop.ObjCMethod get

    public final val linkedDevices: kotlin.collections.List<*> /* compiled code */
        public final external @kotlinx.cinterop.ObjCMethod get
     */
    val formats: List<CaptureDeviceFormat>,
    val localizedName: String,
    val manufacturer: String,
    val modelID: String,
    val suspended: Boolean,
    val transportType: Int,
    val uniqueID: String,
)
