package wonyong.by.videostreaming

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.MediaController
import kotlinx.android.synthetic.main.activity_client_player.*
import java.net.Socket

class ClientPlayerActivity : AppCompatActivity() {

    var videoPath = ""
    lateinit var deviceInfo: DeviceInfo
    var socket : Socket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_player)

        videoPath = intent.getStringExtra("videoPath")
        deviceInfo = intent.getSerializableExtra("deviceInfo") as DeviceInfo
        var mc = MediaController(this@ClientPlayerActivity)
        clientVideoView.setMediaController(mc)
        clientVideoView.setVideoPath(videoPath)
        clientVideoView.requestFocus()
        clientVideoView.start()
    }
}
