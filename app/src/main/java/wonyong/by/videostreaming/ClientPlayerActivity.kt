package wonyong.by.videostreaming

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.MediaController
import kotlinx.android.synthetic.main.activity_client_player.*
import android.view.ViewGroup
import android.view.LayoutInflater
import android.media.MediaPlayer
import android.os.AsyncTask
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.VideoView

class ClientPlayerActivity : AppCompatActivity() {

    lateinit var deviceInfo : DeviceInfo
    lateinit var vv : VideoView
    lateinit var flc : FrameLayout
    lateinit var lp : FrameLayout.LayoutParams
    lateinit var waitTask : AsyncTask<Void, Void, String>
    var videoPath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_player)

        var intent = getIntent()
        deviceInfo = intent.getSerializableExtra("deviceInfo") as DeviceInfo

        var W = deviceInfo.widthMM
        var H = deviceInfo.heightMM
        var displyaMetrics = getApplicationContext().getResources().getDisplayMetrics()

        W = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, W, displyaMetrics)
        H = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, H, displyaMetrics)

        Log.v("ServerPlayerActivity", "afterAD : W = "+W+" / H = "+H)

        var ax = deviceInfo.widthPixel
        var ay = deviceInfo.heightPixel

        ax = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, ax.toFloat(), displyaMetrics).toInt()

        flc = findViewById(R.id.flc)
        vv = findViewById(R.id.clientVideoView)

        videoPath = intent.getStringExtra("videoPath")
        var mc = MediaController(this@ClientPlayerActivity)

        vv.setVideoPath(videoPath)
        vv.seekTo(0)

        vv.layoutParams.width = W.toInt()
        vv.layoutParams.height = H.toInt()
        vv.setX(ax.toFloat())
        lp = FrameLayout.LayoutParams(W.toInt(), H.toInt())
        lp.leftMargin = 0
        lp.topMargin = 0
        lp.rightMargin = 0
        lp.bottomMargin = 0
        vv.layoutParams = lp
        vv.requestLayout()
        flc.requestLayout()

        vv.setOnPreparedListener(MediaPlayer.OnPreparedListener {
            @Override
            fun onPrepared(mediaPlayer: MediaPlayer){
                mediaPlayer.isLooping = true
            }
        })

        //waitTask = callClientTask

        clientVideoView.setMediaController(mc)
        clientVideoView.setVideoPath(videoPath)
        clientVideoView.requestFocus()
        vv.start()
    }


}

