package wonyong.by.videostreaming

import android.content.Context
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
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
    var timeRate : Long = 0

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
        clientVideoView.setVideoPath(videoPath)
        clientVideoView.requestFocus()
    }

    fun callAsyncTask(mode:String){
        var task = ClientNetworkTask(mode, null, this)
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }
    override fun playVideo() {
        clientVideoView.start()
    }

    override fun pauseVideo() {
        clientVideoView.pause()
    }

    override fun onWait() {
        callAsyncTask(CONST.L_PLAYER_WAITING_RECEIVE)
    }

    override fun forward(position: Int) {
        clientVideoView.seekTo(position)
        clientVideoView.start()
    }

    override fun backward(position: Int) {
        clientVideoView.seekTo(position)
        clientVideoView.start()
    }

}
