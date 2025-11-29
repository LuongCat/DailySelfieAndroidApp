package com.example.btqt_nhom3.Tools.Timelapse

import android.content.Context
import android.graphics.BitmapFactory
import android.media.*
import android.view.Surface
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object TimeLapseGenerator {

    fun generate(
        context: Context,
        imagePaths: List<String>,
        startDate: Date,
        endDate: Date,
        framesPerImage: Int
    ): File {

        val folder = File(context.filesDir, "selfies_timelapse")
        if (!folder.exists()) folder.mkdirs()

        val dateFmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val baseName = "${dateFmt.format(startDate)}-${dateFmt.format(endDate)}"
        var fileName = "$baseName.mp4"
        var output = File(folder, fileName)

        var index = 1
        while (output.exists()) {
            fileName = "$baseName ($index).mp4"
            output = File(folder, fileName)
            index++
        }

        val width = 1080
        val height = 1920
        val frameRate = 30


        val codec = MediaCodec.createEncoderByType("video/avc")
        val format = MediaFormat.createVideoFormat("video/avc", width, height).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            setInteger(MediaFormat.KEY_BIT_RATE, 4_000_000)
            setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        }

        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val inputSurface: Surface = codec.createInputSurface()
        codec.start()

        val muxer = MediaMuxer(output.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

        val egl = EglCore()
        val eglSurface = egl.createWindowSurface(inputSurface)
        egl.makeCurrent(eglSurface)
        val renderer = SurfaceRenderer(width, height)

        var trackIndex = -1
        var muxerStarted = false

        var pts = 0L
        val frameDuration = 1_000_000L / frameRate

        for (path in imagePaths) {

            val bmp = BitmapFactory.decodeFile(path)

            repeat(framesPerImage) {

                renderer.drawFrame(bmp)
                egl.swapBuffers(eglSurface)

                pts += frameDuration

                val bufferInfo = MediaCodec.BufferInfo()

                while (true) {
                    val outIndex = codec.dequeueOutputBuffer(bufferInfo, 0)

                    if (outIndex == MediaCodec.INFO_TRY_AGAIN_LATER) break

                    if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        trackIndex = muxer.addTrack(codec.outputFormat)
                        muxer.start()
                        muxerStarted = true
                    } else if (outIndex >= 0) {
                        if (muxerStarted && bufferInfo.size > 0) {
                            val outputBuffer = codec.getOutputBuffer(outIndex)!!
                            outputBuffer.position(bufferInfo.offset)
                            outputBuffer.limit(bufferInfo.offset + bufferInfo.size)

                            bufferInfo.presentationTimeUs = pts
                            muxer.writeSampleData(trackIndex, outputBuffer, bufferInfo)
                        }
                        codec.releaseOutputBuffer(outIndex, false)
                    }
                }
            }
        }

        codec.signalEndOfInputStream()
        codec.stop()
        codec.release()
        muxer.stop()
        muxer.release()

        return output
    }
}
