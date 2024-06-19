package capjoy.recorder

import capjoy.utils.fileExists
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVAssetExportPresetAppleM4A
import platform.AVFoundation.AVAssetExportSession
import platform.AVFoundation.AVAssetExportSessionStatus
import platform.AVFoundation.AVAssetExportSessionStatusCancelled
import platform.AVFoundation.AVAssetExportSessionStatusCompleted
import platform.AVFoundation.AVAssetExportSessionStatusFailed
import platform.AVFoundation.AVAssetTrack
import platform.AVFoundation.AVFileType
import platform.AVFoundation.AVFileTypeAppleM4A
import platform.AVFoundation.AVMediaTypeAudio
import platform.AVFoundation.AVMutableComposition
import platform.AVFoundation.AVURLAsset
import platform.AVFoundation.addMutableTrackWithMediaType
import platform.AVFoundation.tracksWithMediaType
import platform.CoreMedia.CMTimeMake
import platform.CoreMedia.CMTimeRangeMake
import platform.CoreMedia.kCMPersistentTrackID_Invalid
import platform.Foundation.NSURL
import platform.posix.exit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalForeignApi::class)
suspend fun mix(
    inputFileNames: List<String>,
    outputFileName: String,
    outputFileType: AVFileType = AVFileTypeAppleM4A,
    exportPresetName: String = AVAssetExportPresetAppleM4A,
) {
    if (fileExists(outputFileName)) {
        println("Output file already exists: $outputFileName")
        exit(1)
    }

    val audioFiles = inputFileNames.map { NSURL.fileURLWithPath(it) }

    val composition = AVMutableComposition()

    audioFiles.forEachIndexed { _, fileURL ->
        val asset = AVURLAsset(fileURL, options = null)
        val assetTrack: AVAssetTrack? =
            asset.tracksWithMediaType(AVMediaTypeAudio).firstOrNull() as AVAssetTrack?
        if (assetTrack != null) {
            val compositionTrack = composition.addMutableTrackWithMediaType(
                mediaType = AVMediaTypeAudio,
                preferredTrackID = kCMPersistentTrackID_Invalid,
            )
            val timeRange = CMTimeRangeMake(start = CMTimeMake(0, 1), duration = asset.duration)
            compositionTrack?.insertTimeRange(
                timeRange,
                ofTrack = assetTrack,
                atTime = CMTimeMake(0, 1),
                error = null,
            )
        } else {
            println("Failed to get audio track from asset: $fileURL")
            exit(1)
        }
    }

    val outputFileURL = NSURL.fileURLWithPath(outputFileName)
    val exporter = AVAssetExportSession(asset = composition, presetName = exportPresetName)
    exporter.outputURL = outputFileURL
    exporter.outputFileType = outputFileType

    when (val status = exporter.exportAsynchronously()) {
        AVAssetExportSessionStatusCompleted -> {
            println("Mixing completed successfully!")
            exit(0)
        }

        AVAssetExportSessionStatusFailed, AVAssetExportSessionStatusCancelled -> {
            println("Failed to mix audio files: ${exporter.error?.localizedDescription}")
            exit(1)
        }

        else -> {
            println("Unknown export status: $status")
            exit(1)
        }
    }
}

suspend fun AVAssetExportSession.exportAsynchronously(): AVAssetExportSessionStatus =
    suspendCoroutine { cont ->
        this.exportAsynchronouslyWithCompletionHandler {
            cont.resume(this.status)
        }
    }
