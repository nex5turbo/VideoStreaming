package wonyong.by.videostreaming

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.MediaController
import kotlinx.android.synthetic.main.activity_server_player.*
import kotlinx.android.synthetic.main.activity_video_test.*

class ServerPlayerActivity : AppCompatActivity() {

    var videoPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server_player)

        videoPath = intent.getStringExtra("videoPath")
        Log.d("####videopath", videoPath)
        var mc = MediaController(this@ServerPlayerActivity)
        serverVideoView.setMediaController(mc)
        serverVideoView.setVideoPath(videoPath)
        serverVideoView.requestFocus()
        serverVideoView.start()
    }
}
