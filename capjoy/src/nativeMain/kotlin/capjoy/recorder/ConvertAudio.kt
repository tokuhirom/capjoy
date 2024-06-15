package capjoy.recorder

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.refTo
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.value
import platform.AudioToolbox.AudioConverterComplexInputDataProc
import platform.AudioToolbox.AudioConverterDispose
import platform.AudioToolbox.AudioConverterFillComplexBuffer
import platform.AudioToolbox.AudioConverterNew
import platform.AudioToolbox.AudioConverterRefVar
import platform.AudioToolbox.AudioFileClose
import platform.AudioToolbox.AudioFileCreateWithURL
import platform.AudioToolbox.AudioFileIDVar
import platform.AudioToolbox.AudioFileOpenURL
import platform.AudioToolbox.AudioFileWritePackets
import platform.AudioToolbox.kAudioFileFlags_EraseFile
import platform.AudioToolbox.kAudioFileReadPermission
import platform.CoreAudioTypes.AudioBufferList
import platform.CoreAudioTypes.AudioStreamBasicDescription
import platform.CoreAudioTypes.kAudioFormatFlagIsPacked
import platform.CoreAudioTypes.kAudioFormatFlagIsSignedInteger
import platform.CoreAudioTypes.kAudioFormatLinearPCM
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.CFURLCreateWithFileSystemPath
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFStringEncodingUTF8
import platform.CoreFoundation.kCFURLPOSIXPathStyle
import platform.darwin.OSStatus
import platform.darwin.UInt32
import platform.darwin.UInt32Var
import platform.darwin.noErr

@OptIn(ExperimentalForeignApi::class)
fun convertAudioFile(inputPath: String, outputPath: String, outputFormatID: UInt32): Boolean {
    memScoped {
        val inputCFStr = CFStringCreateWithCString(kCFAllocatorDefault, inputPath, kCFStringEncodingUTF8)
        val outputCFStr = CFStringCreateWithCString(kCFAllocatorDefault, outputPath, kCFStringEncodingUTF8)

        val inputURL =
            CFURLCreateWithFileSystemPath(kCFAllocatorDefault, inputCFStr, kCFURLPOSIXPathStyle, false)
        val outputURL =
            CFURLCreateWithFileSystemPath(kCFAllocatorDefault, outputCFStr, kCFURLPOSIXPathStyle, false)

        val inputFile = alloc<AudioFileIDVar>()
        val outputFile = alloc<AudioFileIDVar>()

        val status: OSStatus = AudioFileOpenURL(inputURL, kAudioFileReadPermission, 0u, inputFile.ptr)
        if (status.toUInt() != noErr) {
            println("Failed to open input file: ${status.toUInt()}")
            return false
        }

        val audioFileID = inputFile.value ?: return false

        val outputFormat = alloc<AudioStreamBasicDescription>().apply {
            mFormatID = outputFormatID
            mSampleRate = 44100.0
            mChannelsPerFrame = 2u
            mBitsPerChannel = 16u
            mBytesPerPacket = 4u
            mBytesPerFrame = 4u
            mFramesPerPacket = 1u
            mFormatFlags = when (outputFormatID) {
                kAudioFormatLinearPCM -> kAudioFormatFlagIsSignedInteger or kAudioFormatFlagIsPacked
                else -> 0u
            }
        }

        val outputStatus: OSStatus = AudioFileCreateWithURL(
            outputURL,
            outputFormatID,
            outputFormat.ptr,
            kAudioFileFlags_EraseFile,
            outputFile.ptr
        )
        if (outputStatus.toUInt() != noErr) {
            println("Failed to create output file: ${outputStatus.toUInt()}")
            return false
        }

        val outputFileID = outputFile.value ?: return false

        // AudioConverterのセットアップ
        val converter = alloc<AudioConverterRefVar>()
        val converterStatus = AudioConverterNew(
            null, // インプットフォーマットの設定が必要
            outputFormat.ptr,
            converter.ptr
        )
        if (converterStatus.toUInt() != noErr) {
            println("Failed to create audio converter: ${converterStatus.toUInt()}")
            return false
        }

        // 変換処理の実行
        val buffer = ByteArray(4096)
        val ioOutputDataPacketSize = alloc<UInt32Var>().apply { value = 4096u }

        val inputDataProc: AudioConverterComplexInputDataProc =
            staticCFunction { _, ioNumberDataPackets, ioData, _, _ ->
                // ここで入力データの取得と設定を行う必要があります。
                ioData!!.pointed.mBuffers[0].mData = null
                ioData!!.pointed.mBuffers[0].mDataByteSize = 0u
                ioNumberDataPackets!!.pointed.value = 0u
                return@staticCFunction noErr.toInt()
            }

        while (true) {
            val data = alloc<AudioBufferList>().apply {
                mNumberBuffers = 1u
                mBuffers[0].apply {
                    mNumberChannels = outputFormat.mChannelsPerFrame
                    mDataByteSize = ioOutputDataPacketSize.value * outputFormat.mBytesPerPacket
                    mData = buffer.refTo(0).getPointer(memScope)
                }
            }

            val ioOutputDataSize = ioOutputDataPacketSize.value

            val conversionStatus = AudioConverterFillComplexBuffer(
                converter.value,
                inputDataProc,
                null,
                ioOutputDataPacketSize.ptr,
                data.ptr,
                null
            )

            if (conversionStatus.toUInt() != noErr || ioOutputDataPacketSize.value == 0u) {
                break
            }

            val writeStatus = AudioFileWritePackets(
                outputFileID,
                false,
                ioOutputDataSize,
                null,
                0,
                ioOutputDataPacketSize.ptr,
                buffer.refTo(0).getPointer(memScope)
            )
            if (writeStatus.toUInt() != noErr) {
                println("Failed to write audio data: ${writeStatus.toUInt()}")
                return false
            }
        }

        AudioConverterDispose(converter.value)
        AudioFileClose(audioFileID)
        AudioFileClose(outputFileID)
    }
    return true
}
