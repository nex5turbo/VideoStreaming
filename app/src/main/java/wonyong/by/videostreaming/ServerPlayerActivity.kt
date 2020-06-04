package wonyong.by.videostreaming

import android.content.Context
import android.media.AudioManager
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.MediaController
import android.widget.SeekBar
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
    var timeRateArray = arrayListOf<Long>()
    var nowPosition = 0

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
        serverVideoView.setVideoPath(videoPath)
        serverVideoView.requestFocus()
        controllerPlayButton.setOnClickListener {
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
    }



    fun callAsyncTask(mode:String){
        var task = ServerNetworkTask(mode, null, this)
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
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
}
