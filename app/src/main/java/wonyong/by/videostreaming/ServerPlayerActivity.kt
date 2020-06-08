package wonyong.by.videostreaming

import android.content.Context
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.MediaController
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_server_player.*
import java.lang.Thread.sleep
import java.net.ServerSocket
import java.net.Socket

class ServerPlayerActivity : AppCompatActivity(), PlayerListener {

    var videoPath: String? = null
    var socketList = arrayListOf<Socket>()
    var bufferSocketList = arrayListOf<Socket>()
    var playerServerSocket : ServerSocket? = null
    var playerBufferSocket : ServerSocket? = null
    var bufferReady = false
    var isForwarding = false
    var isPlaying = false
    val CONST = Consts()
    var nowPosition = 0
    var bufferPosition = 0
    var totalWidthMM = 0f
    var aX = 0f
    var videoWidthPixel = 0
    var videoHeightPixel = 0
    lateinit var retriever : MediaMetadataRetriever

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_server_player)

        videoPath = intent.getStringExtra("videoPath")
        totalWidthMM = intent.getFloatExtra("videoSize", 0f)
        aX = -(intent.getFloatExtra("aX", 0f))
        init()
        setVideo()
    }

    private fun init() {
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

        var dm = applicationContext.resources.displayMetrics
//        mediaController = MediaController(this@ServerPlayerActivity)
//        serverVideoView.setMediaController(mediaController)
        retriever = MediaMetadataRetriever()
        retriever.setDataSource(videoPath)
        videoHeightPixel = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT).toInt()
        videoWidthPixel = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH).toInt()
        serverVideoView.setVideoPath(videoPath)
        serverVideoView.layoutParams.width = (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, totalWidthMM, dm)).toInt()
        serverVideoView.layoutParams.height = (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, totalWidthMM*videoHeightPixel/videoWidthPixel, dm)).toInt()
        retriever.release()
        serverVideoView.x = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, aX, dm)-48
        serverVideoView.requestFocus()
        controllerPlayButton.setOnClickListener {
            Log.d("###", "clicked")
            isPlaying = true
            callAsyncTask(CONST.N_PLAYER_PLAY)
        }
        controllerPauseButton.setOnClickListener {
            isPlaying = false
            nowPosition = serverVideoView.currentPosition
            Log.d("###", nowPosition.toString())
            callAsyncTask(CONST.N_PLAYER_PAUSE)
        }
        forwardButton.setOnClickListener {
            if(isForwarding){
                return@setOnClickListener
            }
            isForwarding = true
            serverVideoView.pause()
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
    }



    fun callAsyncTask(mode:String){
        Log.d("###", "before")
        ServerNetworkTask(mode, null, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        Log.d("###", "after")
    }

    override fun playVideo() {
        serverVideoView.start()
    }

    override fun pauseVideo() {
        serverVideoView.pause()
    }

    override fun onWait() {

    }

    override fun forward(position : Int) {
        serverVideoView.seekTo(nowPosition)
    }

    override fun backward(position : Int) {
        serverVideoView.seekTo(nowPosition)
//        serverVideoView.start()
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
    override fun bufferOver(position: Int) {

    }
}
