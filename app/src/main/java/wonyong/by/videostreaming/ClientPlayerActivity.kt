package wonyong.by.videostreaming

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.MediaController
import kotlinx.android.synthetic.main.activity_client_player.*

class ClientPlayerActivity : AppCompatActivity() {

    var videoPath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_player)

        videoPath = intent.getStringExtra("videoPath")
        var mc = MediaController(this@ClientPlayerActivity)
        clientVideoView.setMediaController(mc)
        clientVideoView.setVideoPath(videoPath)
        clientVideoView.requestFocus()
        clientVideoView.start()
    }
}
