package com.example.btqt_nhom3.Tools.Timelapse

import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.example.btqt_nhom3.R

class VideoPlayerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.videoPlayerToolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val video = findViewById<VideoView>(R.id.videoView)
        val path = intent.getStringExtra("video") ?: return

        val controller = MediaController(this)
        controller.setAnchorView(video)
        video.setMediaController(controller)

        video.setVideoURI(Uri.parse(path))

        video.setOnPreparedListener { mp ->
            mp.isLooping = false
            controller.show(5000)

            video.post {
                val videoW = mp.videoWidth.toFloat()
                val videoH = mp.videoHeight.toFloat()

                val viewW = video.width.toFloat()
                val viewH = video.height.toFloat()

                val videoRatio = videoW / videoH
                val viewRatio = viewW / viewH

                if (videoRatio > viewRatio) {
                    // Video rộng hơn khung → scale theo width
                    val scale = viewW / videoW
                    video.scaleX = 1f
                    video.scaleY = scale * (videoW / videoH)
                } else {
                    // Video cao hơn khung → scale theo height
                    val scale = viewH / videoH
                    video.scaleY = 1f
                    video.scaleX = scale * (videoH / videoW)
                }
            }

            video.start()
        }
    }
}
