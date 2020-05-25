package wonyong.by.videostreaming

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.MediaController
import kotlinx.android.synthetic.main.activity_client_player.*
import java.net.InetAddress
import java.net.Socket

class ClientPlayerActivity : AppCompatActivity(), PlayerListener {

    var videoPath = ""
    var hostAddress : InetAddress? = null
    lateinit var deviceInfo: DeviceInfo
    var socket : Socket? = null
    var CONST = Consts()
    var mediaController : MediaController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_player)


        buttonListener()
        init()
    }

    private fun buttonListener() {
        socketButton.setOnClickListener {
            Log.d("###", "Before Task")
            callAsyncTask(CONST.L_PLAYER_ON_CONNECT)
        }

    }

    private fun init() {
        videoPath = intent.getStringExtra("videoPath")
        hostAddress = intent.getSerializableExtra("hostAddress") as InetAddress
        setVideo()
    }



    private fun setVideo() {
        mediaController = MediaController(this@ClientPlayerActivity)
        clientVideoView.setMediaController(mediaController)
        clientVideoView.setVideoPath(videoPath)
        clientVideoView.requestFocus()
        clientVideoView.start()

    }

    fun callAsyncTask(mode:String){
        var task = ClientNetworkTask(mode, null, this)
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }
    override fun playVideo() {

    }

    override fun pauseVideo() {

    }
}
