package wonyong.by.videostreaming

import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.View.GONE
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.MediaController
import android.widget.VideoView
import kotlinx.android.synthetic.main.activity_client_player.*
import java.io.File
import java.lang.Thread.sleep
import java.net.InetAddress
import java.net.Socket

class ClientPlayerActivity : AppCompatActivity(), PlayerListener {

    var moovSize: Long = 0
    var videoPath = ""
    var hostAddress : InetAddress? = null
    lateinit var deviceInfo:DeviceInfo
    var socket : Socket? = null
    var bufferSocket : Socket? = null
    var CONST = Consts()
    var timeRate : Long = 0
    var fileSize : Long = 0
    var secmdatSize : Long = 0
    var videoLength : Long = 0
    var totalVideoWidthMM = 0f
    var aX = 0f
    var videoWidthPixel = 0
    var videoHeightPixel = 0
    var fileOver = false
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
        fileSize = intent.getLongExtra("fileSize", 0)
        deviceInfo = intent.getSerializableExtra("deviceInfo") as DeviceInfo
        moovSize = intent.getLongExtra("moovSize", 0)
        totalVideoWidthMM = intent.getFloatExtra("videoSize", 0f)
        aX = -(intent.getFloatExtra("aX", 0f))
        socketButton.visibility = View.GONE
        var checkMoov = File(videoPath).length()
        while(checkMoov < moovSize){
            checkMoov = File(videoPath).length()
        }
        socketButton.visibility = View.VISIBLE
        retriever = MediaMetadataRetriever()
        retriever.setDataSource(videoPath)
        videoLength = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toLong()
        videoLength = videoLength / 1000
        videoWidthPixel = (retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)).toInt()
        videoHeightPixel = (retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)).toInt()
        retriever.release()
        Log.d("###", videoHeightPixel.toString()+" "+videoWidthPixel.toString())
        socketButton.gravity = Gravity.CENTER
        setVideo()
    }



    private fun setVideo() {
        var W = 0
        var H = 0
        var displayMetrics = getApplicationContext().getResources().getDisplayMetrics()

//
//        var videoHeight = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))
//        var videoWidth = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))

//        W = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, W, displayMetrics).toInt()
        W = (totalVideoWidthMM*Math.round(displayMetrics.xdpi)*(1f/25.4f)).toInt()
        H = ((totalVideoWidthMM*videoHeightPixel/videoWidthPixel)*Math.round(displayMetrics.ydpi)*(1f/25.4f)).toInt()
//
//        Log.v("ClientPlayerActivity", "afterAD : W = "+W+" / H = "+H)
//        Log.d("###", videoHeight.toString()+videoWidth.toString())


        //픽셀단위로 옮기는 변수
//        var aX = -(deviceInfo.deviceOrder-1)*(deviceInfo.widthMM*displayMetrics.xdpi*(1f/25.4f)).toInt()
        aX = if(aX == 0f) 0f else TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, aX, displayMetrics)
        Log.v("ClientPlayerActivity", "afterAD2 : "+aX)
        Log.d("###aX", aX.toString())
        Log.d("###W", W.toString())
        Log.d("###H", H.toString())

        //aX = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, aX.toFloat(), displayMetrics).toInt()


        var PreparedListener = MediaPlayer.OnPreparedListener {
            it.setVolume(0f, 0f)
        }

        clientVideoView.setOnPreparedListener(PreparedListener)
        clientVideoView.setVideoURI(Uri.parse(videoPath))
        clientVideoView.layoutParams.width = W
        clientVideoView.layoutParams.height = H
        clientVideoView.setX(aX - 24)
        Log.v("ClientPlayerActivity", "afterAD3 : "+aX)
        clientVideoView.requestLayout()
        clientVideoView.setOnErrorListener(object : MediaPlayer.OnErrorListener{
            override fun onError(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
                callAsyncTask(CONST.N_PLAYER_BUFFER)
                if(fileOver){
                    Log.d("###", fileOver.toString())
                }else {
                    Log.d("####", "bufferStart")
                    secmdatSize = (fileSize - moovSize) / videoLength
                    var file = File(videoPath)
                    var filelen = file.length()
                    var afterFile = File(videoPath)
                    var releaseLong = secmdatSize * 10
                    //extract mpeg4 moov data for releaseLong
                    while (afterFile.length() - filelen < releaseLong) {
                        afterFile = File(videoPath)
                        Log.d("###filesize", afterFile.length().toString())
                        if (afterFile.length() == fileSize)
                            break
                    }
                    if(afterFile.length() == fileSize){
                        fileOver = true
                    }
                    sleep(500)
                }
                ClientBufferThread(CONST.N_PLAYER_READY_BUFFER, this@ClientPlayerActivity).start()
                Log.d("####", "bufferOver")
                clientVideoView.setVideoPath(videoPath)
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
        clientVideoView.pause()
        if(fileOver){
            clientVideoView.seekTo(position)
            callAsyncTask(CONST.N_READY_FORWARD)
        }else {
            var nowFileSize = File(videoPath).length()
            if (nowFileSize == fileSize) {
                clientVideoView.seekTo(position)
                callAsyncTask(CONST.N_READY_FORWARD)
                fileOver = true
            } else {
                while (secmdatSize * (position / 1000) + moovSize > nowFileSize) {
                    nowFileSize = File(videoPath).length()
                }
                clientVideoView.seekTo(position)
                callAsyncTask(CONST.N_READY_FORWARD)
            }
        }

    }

    override fun backward(position: Int) {
        clientVideoView.seekTo(position)
//        clientVideoView.start()
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

    override fun bufferOver(position: Int) {
        clientVideoView.seekTo(position)
    }

    override fun setAfterBuffered(position: Int) {
        secmdatSize = (fileSize - moovSize)/videoLength
        var file = File(videoPath)
        var filelen = file.length()
        var afterFile = File(videoPath)
        var releaseLong = secmdatSize*10
        //extract mpeg4 moov data for releaseLong
        while(afterFile.length() - filelen < releaseLong){
            afterFile = File(videoPath)
            Log.d("###filesize", afterFile.length().toString())
            if(afterFile.length() == fileSize)
                break
        }
        if(afterFile.length() == fileSize){
            fileOver = true
        }
        ClientBufferThread(CONST.N_PLAYER_READY_BUFFER, this).start()
    }
}
