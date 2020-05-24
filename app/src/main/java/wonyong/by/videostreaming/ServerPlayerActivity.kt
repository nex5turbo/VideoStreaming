package wonyong.by.videostreaming

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.MediaController
import kotlinx.android.synthetic.main.activity_server_player.*
import kotlinx.android.synthetic.main.activity_video_test.*
import java.net.ServerSocket
import java.net.Socket

class ServerPlayerActivity : AppCompatActivity(), PlayerListener {

    var videoPath: String? = null
    var socketList = arrayListOf<Socket>()
    var playerServerSocket : ServerSocket? = null
    var mediaController : MediaController? = null
    val CONST = Consts()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server_player)

        videoPath = intent.getStringExtra("videoPath")
        init()
        setVideo()
    }

    private fun init() {
        callAsyncTask(CONST.L_PLAYER_ON_CONNECT)
    }

    private fun setVideo() {
        mediaController = MediaController(this@ServerPlayerActivity)
        serverVideoView.setMediaController(mediaController)
        serverVideoView.setVideoPath(videoPath)
        serverVideoView.requestFocus()
        serverVideoView.start()

    }

    fun callAsyncTask(mode:String){
        var task = ServerNetworkTask(mode, null, this)
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    override fun playVideo() {

    }

    override fun pauseVideo() {

    }
}
