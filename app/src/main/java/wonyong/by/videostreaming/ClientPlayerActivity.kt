package wonyong.by.videostreaming

import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View.GONE
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.MediaController
import android.widget.VideoView
import kotlinx.android.synthetic.main.activity_client_player.*
import java.io.File
import java.net.InetAddress
import java.net.Socket

class ClientPlayerActivity : AppCompatActivity(), PlayerListener {

    var videoPath = ""
    var hostAddress : InetAddress? = null
    lateinit var deviceInfo:DeviceInfo
    var socket : Socket? = null
    var bufferSocket : Socket? = null
    var CONST = Consts()
    var mediaController : MediaController? = null
    var timeRate : Long = 0
    lateinit var vv : VideoView
    lateinit var lp : FrameLayout.LayoutParams
    lateinit var flc : FrameLayout
    lateinit var retriever: MediaMetadataRetriever

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_client_player)

        buttonListener()
        init()
    }

    private fun buttonListener() {
        socketButton.setOnClickListener {
            Log.d("###", "Before Task")
            callAsyncTask(CONST.L_PLAYER_ON_CONNECT)
            socketButton.visibility = GONE
        }

    }

    private fun init() {
        videoPath = intent.getStringExtra("videoPath")
        hostAddress = intent.getSerializableExtra("hostAddress") as InetAddress
        deviceInfo = intent.getSerializableExtra("deviceInfo") as DeviceInfo
        retriever = MediaMetadataRetriever()
        retriever.setDataSource(videoPath)
        socketButton.gravity = Gravity.CENTER
        setVideo()
    }



    private fun setVideo() {
        var W = deviceInfo?.widthPixel
        var H = deviceInfo?.heightPixel
        var displayMetrics = getApplicationContext().getResources().getDisplayMetrics()

//
//        var videoHeight = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))
//        var videoWidth = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))

//        W = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, W, displayMetrics)
//        H = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, H, displayMetrics)
//
//        Log.v("ClientPlayerActivity", "afterAD : W = "+W+" / H = "+H)
//        Log.d("###", videoHeight.toString()+videoWidth.toString())


        //픽셀단위로 옮기는 변수
        var aX = -(deviceInfo.deviceOrder-1)*1080
        Log.v("ClientPlayerActivity", "afterAD2 : "+aX)

        //aX = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, aX.toFloat(), displayMetrics).toInt()

        flc = findViewById(R.id.flc)
        vv = findViewById(R.id.clientVideoView)

        var PreparedListener = MediaPlayer.OnPreparedListener {
            it.setVolume(0f, 0f)
        }
        vv.setOnPreparedListener(PreparedListener)
        vv.setVideoURI(Uri.parse(videoPath))
        vv.layoutParams.width = 3960
        vv.layoutParams.height = H.toInt()
        vv.setX(aX.toFloat())
        lp = FrameLayout.LayoutParams(3960, vv.layoutParams.height)
        lp.leftMargin = 0
        lp.topMargin = 0
        lp.rightMargin = 0
        lp.bottomMargin = 0
        Log.v("ClientPlayerActivity", "afterAD3 : "+aX)
        vv.layoutParams = lp
        vv.requestLayout()
        vv.setOnErrorListener(object : MediaPlayer.OnErrorListener{
            override fun onError(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
                callAsyncTask(CONST.N_PLAYER_BUFFER)
                vv.setVideoURI(Uri.parse(videoPath))
                vv.start()
                return true
            }
        })
        flc.requestLayout()
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
        callAsyncTask(CONST.L_PLAYER_CLIENT_WAITING_RECEIVE)
    }

    override fun forward(position: Int) {
        clientVideoView.seekTo(position)
        clientVideoView.start()
    }

    override fun backward(position: Int) {
        clientVideoView.seekTo(position)
        clientVideoView.start()
    }

    fun exitPlayer(){
        var file = File(videoPath)
        if(file.exists())
            file.delete()
        finish()
    }

    override fun serverOnWait1() {

    }

    override fun serverOnWait2() {

    }
}
