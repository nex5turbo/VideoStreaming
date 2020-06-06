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
    lateinit var deviceInfo2:DeviceInfo
    lateinit var deviceInfo3:DeviceInfo
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
        deviceInfo2 = intent.getSerializableExtra("deviceInfo2") as DeviceInfo
        deviceInfo3 = intent.getSerializableExtra("deviceInfo3") as DeviceInfo
        retriever = MediaMetadataRetriever()
        retriever.setDataSource(videoPath)
        socketButton.gravity = Gravity.CENTER
        setVideo()
    }



    private fun setVideo() {
        var W = deviceInfo?.widthPixel
        var H = deviceInfo?.heightPixel
        var displayMetrics = getApplicationContext().getResources().getDisplayMetrics()
        var W2 = deviceInfo2?.widthPixel
        var W3 = deviceInfo3?.widthPixel
        var aX = 0

//        var videoHeight = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))
//        var videoWidth = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))

//        W = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, W, displayMetrics)
//        H = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, H, displayMetrics)
//
//        Log.v("ClientPlayerActivity", "afterAD : W = "+W+" / H = "+H)
//        Log.d("###", videoHeight.toString()+videoWidth.toString())


        //픽셀단위로 옮기는 변수
        if(deviceInfo.deviceOrder==0) {
            aX = -(W + W2 + W3)
            Log.v("ClientPlayerActivity", "afterAD2 : " + aX)
        } else if(deviceInfo2.deviceOrder == 1){
            aX = -(W2 + W3)
            Log.v("ClientPlayerActivity", "after Ad2 : "+ aX)
        }

        //aX = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, aX.toFloat(), displayMetrics).toInt()

        flc = findViewById(R.id.flc)
        vv = findViewById(R.id.clientVideoView)

        var PreparedListener = MediaPlayer.OnPreparedListener {
            it.setVolume(0f, 0f)
        }
        vv.setOnPreparedListener(PreparedListener)
        vv.setVideoURI(Uri.parse(videoPath))
        vv.layoutParams.width = W+W2+W3
        vv.layoutParams.height = H
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

    override fun setAfterBuffered(position: Int) {
        clientVideoView.seekTo(position)
        ClientBufferThread(CONST.N_PLAYER_READY_BUFFER, this).start()
    }
}
