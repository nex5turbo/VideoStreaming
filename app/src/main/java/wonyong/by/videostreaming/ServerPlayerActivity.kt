package wonyong.by.videostreaming

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.MediaController
import android.widget.SeekBar
import android.widget.VideoView
import kotlinx.android.synthetic.main.activity_server_player.*
import kotlinx.android.synthetic.main.activity_video_test.*
import java.lang.Thread.sleep
import java.net.ServerSocket
import java.net.Socket

class ServerPlayerActivity : AppCompatActivity(), PlayerListener {

    var videoPath: String? = null
    var socketList = arrayListOf<Socket>()
    var bufferSocketList = arrayListOf<Socket>()
    var playerServerSocket : ServerSocket? = null
    var playerBufferSocket : ServerSocket? = null
    var mediaController : MediaController? = null
    val CONST = Consts()
    var timeRateArray = arrayListOf<Long>()
    var nowPosition = 0
    var bufferPosition = 0
    var bufferReady = false
    lateinit var deviceInfo:DeviceInfo
    lateinit var deviceInfo2:DeviceInfo
    lateinit var deviceInfo3:DeviceInfo
    lateinit var vv : VideoView
    lateinit var lp : FrameLayout.LayoutParams
    lateinit var fls : FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_server_player)

        videoPath = intent.getStringExtra("videoPath")
        init()
        setVideo()
    }

    private fun init() {
        deviceInfo = intent.getSerializableExtra("deviceInfo") as DeviceInfo
        deviceInfo2 = intent.getSerializableExtra("deviceInfo2") as DeviceInfo
        deviceInfo3 = intent.getSerializableExtra("deviceInfo3") as DeviceInfo
        callAsyncTask(CONST.L_PLAYER_ON_CONNECT)
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val nMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val nCurrentVolumn = audioManager
            .getStreamVolume(AudioManager.STREAM_MUSIC)

        seekBar.setMax(nMax)
        seekBar.setProgress(nCurrentVolumn)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // TODO Auto-generated method stub

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // TODO Auto-generated method stub

            }

            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {
                // TODO Auto-generated method stub
                audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    progress, 0
                )
            }
        })
    }

    private fun setVideo() {
//        mediaController = MediaController(this@ServerPlayerActivity)
//        serverVideoView.setMediaController(mediaController)

        var W = deviceInfo?.widthPixel
        var H = deviceInfo?.heightPixel
        var W2 = deviceInfo2?.widthPixel
        var W3 = deviceInfo3?.widthPixel

        var aX = -(W+W2)


        fls = findViewById(R.id.fls)
        serverVideoView.setVideoURI(Uri.parse(videoPath))
        serverVideoView.layoutParams.width = W+W2+W3
        serverVideoView.layoutParams.height = H
        serverVideoView.setX(aX.toFloat())
        lp = FrameLayout.LayoutParams(vv.layoutParams.width, serverVideoView.layoutParams.height)
        lp.leftMargin = 0
        lp.topMargin = 0
        lp.rightMargin = 0
        lp.bottomMargin = 0
        Log.v("ClientPlayerActivity", "afterAD3 : "+aX)
        serverVideoView.layoutParams = lp
        serverVideoView.requestLayout()

        controllerPlayButton.setOnClickListener {
            Log.d("###", "clicked")
            callAsyncTask(CONST.N_PLAYER_PLAY)
        }
        controllerPauseButton.setOnClickListener {
            callAsyncTask(CONST.N_PLAYER_PAUSE)
        }
        forwardButton.setOnClickListener {
            nowPosition = serverVideoView.currentPosition+10000
            callAsyncTask(CONST.N_PLAYER_FORWARD)
        }
        backwardButton.setOnClickListener {
            nowPosition = serverVideoView.currentPosition-10000
            callAsyncTask(CONST.N_PLAYER_BACKWARD)
        }
        serverVideoView.setOnTouchListener(object : View.OnTouchListener{
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                if(seekBar.visibility == View.VISIBLE){
                    controllerPlayButton.visibility = View.GONE
                    controllerPauseButton.visibility = View.GONE
                    forwardButton.visibility = View.GONE
                    backwardButton.visibility = View.GONE
                    seekBar.visibility = View.GONE
                }else{
                    controllerPlayButton.visibility = View.VISIBLE
                    controllerPauseButton.visibility = View.VISIBLE
                    forwardButton.visibility = View.VISIBLE
                    backwardButton.visibility = View.VISIBLE
                    seekBar.visibility = View.VISIBLE
                }
                return false
            }
        })
        fls.requestLayout()
    }



    fun callAsyncTask(mode:String){
        Log.d("###", "before")
        ServerNetworkTask(mode, null, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        Log.d("###", "after")
    }

    override fun playVideo() {
        var cur = System.currentTimeMillis()
        serverVideoView.start()
        var after = System.currentTimeMillis()
        var delay = after - cur
        Log.d("###", delay.toString())
    }

    override fun pauseVideo() {
        serverVideoView.pause()
    }

    override fun onWait() {

    }

    override fun forward(position : Int) {
        serverVideoView.seekTo(nowPosition)
        serverVideoView.start()
    }

    override fun backward(position : Int) {
        serverVideoView.seekTo(nowPosition)
        serverVideoView.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        callAsyncTask(CONST.N_PLAYER_EXIT)
        serverVideoView.pause()
        finish()
    }

    override fun serverOnWait1() {
        ServerBufferThread(CONST.L_PLAYER_SERVER_WAITING_RECEIVE, this).start()
    }

    override fun serverOnWait2() {
        ServerBufferThread(CONST.L_PLAYER_SERVER_WAITING_RECEIVE_2, this).start()
    }

    override fun setAfterBuffered(position: Int) {

    }
}
