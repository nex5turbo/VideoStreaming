package wonyong.by.videostreaming

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.net.wifi.p2p.*
import android.os.AsyncTask
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_server.*
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.lang.Thread.sleep
import java.net.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import kotlin.collections.ArrayList


class ServerActivity : AppCompatActivity(), ServerTaskListener{

    //위젯 정보 시작
    lateinit var widgetConnectButton : Button
    lateinit var widgetDisconnectButton : Button
    lateinit var widgetRefreshButton : Button
    lateinit var widgetTitle : TextView
    lateinit var widgetStatusTextView : TextView
    //위젯정보 끝

    var DN = ""
    var streamingOrder = 0
    val CONST = Consts()
    var serverSocket : ServerSocket? = null
    var dataServerSocket : ServerSocket? = null
    var dataSocket : Socket? = null
    var socket : Socket? = null
    lateinit var task :ServerNetworkTask
    lateinit var wifiManager:WifiManager
    lateinit var wifiP2pManager : WifiP2pManager;
    lateinit var wifiP2pChannel : WifiP2pManager.Channel
    lateinit var broadcastReceiver : ServerBroadcastReceiver
    lateinit var intentFilter : IntentFilter
    lateinit var serverDeviceInfo: DeviceInfo
    var clientDeviceInfoList = ArrayList<DeviceInfo>()
    var resultPath : String? = null
    var fileName = ""
    var fileSize : Long = 0
    var moovSize = 0
    var totalWidthMM = 0f
    var aX = 0f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server)

        init()
        buttonListener()
        getInfo()

    }

    private fun getInfo() {
        serverDeviceInfo = intent.getSerializableExtra("deviceInfo") as DeviceInfo
        DN = intent.getStringExtra("DS")
    }


    private fun buttonListener() {
        serverWifiDirectConnectButton.setOnClickListener {
            callAsyncTask(CONST.L_ON_CONNECT)
        }

        serverWifiDirectPlayVideoButton.setOnClickListener {
            callAsyncTask(CONST.N_PLAY_VIDEO)
        }


        serverWifiDirectRefreshButton.setOnClickListener {//현재 접속한 기기 목록을 띄우도록
            if(ContextCompat.checkSelfPermission(this@ServerActivity,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this@ServerActivity,
                    arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),
                    100)
            }

            else {
                wifiP2pManager.discoverPeers(wifiP2pChannel, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        serverWifiDirectConnectionStatus.setText("검색중")
                    }

                    override fun onFailure(i: Int) {
                        serverWifiDirectConnectionStatus.setText("검색실패")
                    }
                })
            }
        }


        serverWifiDirectDisconnectButton.setOnClickListener {
            //TODO Group Owner일 경우 연결끊으면 어떻게 해야하는지
            wifiP2pManager.cancelConnect(wifiP2pChannel, object : WifiP2pManager.ActionListener{
                override fun onSuccess() {
                }

                override fun onFailure(p0: Int) {

                }
            })

            wifiP2pManager.removeGroup(wifiP2pChannel, object : WifiP2pManager.ActionListener{
                override fun onSuccess() {
                    Toast.makeText(this@ServerActivity, "연결이 해제됩니다.", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(p0: Int) {

                }
            })
        }
        serverWifiDirectSendVideoButton.setOnClickListener {
            if(resultPath == null){
                Toast.makeText(this, "Video not selected", Toast.LENGTH_SHORT).show()

            }else {
                callAsyncTask(CONST.N_FILE_STREAMING_START)
                sleep(500)
                streamingOrder++
                callAsyncTask(CONST.N_FILE_STREAMING_START)
                playEnable()
            }

        }
        serverWifiDirectFindVideoButton.setOnClickListener {
            var i = Intent(Intent.ACTION_GET_CONTENT)
            i.setType("video/*")
            startActivityForResult(i, 2)
        }
    }

    override fun playVideo() {
        val i = Intent(this, ServerPlayerActivity::class.java)
        i.putExtra("videoPath", resultPath + "/" + fileName)
        i.putExtra("videoSize", totalWidthMM)
        i.putExtra("aX", aX)
        startActivity(i)
    }

    private fun init() {
        wifiManager = application.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiP2pManager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        wifiP2pChannel = wifiP2pManager.initialize(this, Looper.getMainLooper(), null)
        broadcastReceiver = ServerBroadcastReceiver(wifiP2pManager, wifiP2pChannel, this)
        intentFilter = IntentFilter()
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        widgetStatusTextView = serverWifiDirectConnectionStatus
        widgetConnectButton = serverWifiDirectConnectButton
        widgetDisconnectButton = serverWifiDirectDisconnectButton
        widgetRefreshButton = serverWifiDirectRefreshButton
        widgetTitle = serverWifiDirectTitle
        serverWifiDirectPlayVideoButton.isEnabled = false
        serverWifiDirectSendVideoButton.isEnabled = false


    }

    var peerListListener : WifiP2pManager.PeerListListener = object : WifiP2pManager.PeerListListener{
        override fun onPeersAvailable(peerList: WifiP2pDeviceList){

        }
    }

    var connectInfoListener : WifiP2pManager.ConnectionInfoListener = object : WifiP2pManager.ConnectionInfoListener{
        override fun onConnectionInfoAvailable(wifiP2pInfo: WifiP2pInfo) {
            val groupOwnerAddress : InetAddress = wifiP2pInfo.groupOwnerAddress
            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner){
                widgetStatusTextView.setText("server")
                var group = WifiP2pGroup()
                var clientNameArray = arrayOfNulls<String>(group.clientList.size)
                var index = 0
                for(clientGroup:WifiP2pDevice in group.clientList) {
                    clientNameArray[index] = clientGroup.deviceName
                    index++
                }
                var nameAdapter: ArrayAdapter<String> =
                    ArrayAdapter<String>(applicationContext, android.R.layout.simple_list_item_1, clientNameArray)!!
                serverWifiDirectListView.setAdapter(nameAdapter)
            }
            else if(wifiP2pInfo.groupFormed){
                widgetStatusTextView.setText("Client")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(broadcastReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        //TODO 마찬가지로 그룹오너인 경우 연결끊으면 어떻게 해야할지
        wifiP2pManager.cancelConnect(wifiP2pChannel, object : WifiP2pManager.ActionListener{
            override fun onSuccess() {
            }

            override fun onFailure(p0: Int) {

            }
        })

        wifiP2pManager.removeGroup(wifiP2pChannel, object : WifiP2pManager.ActionListener{
            override fun onSuccess() {
                Toast.makeText(this@ServerActivity, "연결이 해제됩니다.", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(p0: Int) {

            }
        })


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK){
            if(requestCode == 2){
                Log.e("###", "resultPath")
                var realPath = RealPath()
                var uri = data?.data
                resultPath = realPath.getRealPath(this, uri!!)
                Log.e("###", resultPath)
                splitPathName()
                Log.e("###", resultPath)
                Log.e("###", fileName)
                Toast.makeText(this, resultPath, Toast.LENGTH_SHORT).show()
                checkmoovSize()
                sendEnable()
            }
        }
    }

    private fun checkmoovSize() {
        var selectFile = File(resultPath+"/"+fileName)
        fileSize = selectFile.length()

        if(!selectFile.exists()){
            Toast.makeText(this, "존재하지 않는 파일입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        var fis = FileInputStream(selectFile)
        var bis = BufferedInputStream(fis)
        var buf = ByteArray(4)
        bis.read(buf)

        var moovSizeByte = ByteArray(4)
        var ftypSizeByte = ByteArray(4)

        ftypSizeByte[0] = buf[0]
        ftypSizeByte[1] = buf[1]
        ftypSizeByte[2] = buf[2]
        ftypSizeByte[3] = buf[3]

        var ftypSize = ByteBuffer.wrap(ftypSizeByte).order(ByteOrder.BIG_ENDIAN).getInt()
        Log.d("###", ftypSize.toString())

        var buf2 = ByteArray(ftypSize-4)
        Log.d("###", (ftypSize-4).toString())

        bis.read(buf2)
        Log.d("###", selectFile.length().toString())

        var meg = String(buf2)
        if(!meg.contains("ftyp")) {
            Log.d("###ftyp", "Not available to Stream")
            return
        }

        var buf3 = ByteArray(4)
        bis.read(buf3)

        moovSizeByte[0] = buf3[0]
        moovSizeByte[1] = buf3[1]
        moovSizeByte[2] = buf3[2]
        moovSizeByte[3] = buf3[3]
        moovSize = ByteBuffer.wrap(moovSizeByte).order(ByteOrder.BIG_ENDIAN).getInt() + ftypSize
        var moovBuff = ByteArray(moovSize)
        bis.read(moovBuff)
        var moovMsg = String(moovBuff)
        if(!moovMsg.contains("moov")){
            Log.d("###moov", "Not available to Stream")
            val builder = AlertDialog.Builder(this)

            builder.setTitle("파일변환")
                .setMessage("스트리밍용 파일이 아닙니다.\n스트리밍용으로 변환하시겠습니까?\n(원본파일은 보존됩니다.)")
                .setPositiveButton("예", object : DialogInterface.OnClickListener{
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        Toast.makeText(this@ServerActivity, "파일 변환중입니다...", Toast.LENGTH_SHORT).show()
                        var thread = Thread(object : Runnable{
                            override fun run() {
                                var outFile = File(resultPath+"/moov"+fileName)
                                if(!outFile.exists()){
                                    outFile.createNewFile()
                                }
                                QtFastStartMy.fastStart(selectFile, outFile, this@ServerActivity)
                            }
                        })
                        thread.start()
                    }
                })
                .setNegativeButton("아니오", object : DialogInterface.OnClickListener{
                    override fun onClick(p0: DialogInterface?, p1: Int) {

                    }
                })

            val alertDialog = builder.create()

            alertDialog.show()
            return
        }
        Log.d("###moov", moovSize.toString())
        Toast.makeText(this, "moov size : "+moovSize.toString()+"B, mdat size : "+(fileSize-moovSize).toString()+"B", Toast.LENGTH_LONG).show()
    }

    fun splitPathName(){
        var returnPath = ""
        var tempString = ""
        var token = StringTokenizer(resultPath, "/")
        tempString = token.nextToken()
        returnPath = "/" + tempString + "/"
        while(true){
            tempString = token.nextToken()
            if(!token.hasMoreTokens()){
                fileName = tempString
                break
            }
            returnPath = returnPath + tempString + "/"
        }
        resultPath = returnPath.substring(0,returnPath.length-1)
    }

    fun callAsyncTask(mode:String){
        task = ServerNetworkTask(mode, this, null)
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    override fun addClientDeviceInfo(
        heightPixel: Int,
        widthPixel: Int,
        widthMM: Float,
        heightMM: Float,
        deviceOrder: Int,
        sock : Socket,
        dataSock : Socket
    ) {
        var di = DeviceInfo(heightPixel, widthPixel, widthMM, heightMM, deviceOrder, sock, dataSock)
        clientDeviceInfoList.add(di)
    }

    override fun filetransferOver() {
    }

    override fun sendEnable() {
        serverWifiDirectSendVideoButton.isEnabled = true
    }

    override fun playEnable() {
        serverWifiDirectPlayVideoButton.isEnabled = true
    }

    override fun calcPixel() {
        for(di:DeviceInfo in clientDeviceInfoList){
            totalWidthMM += di.widthMM
        }
        aX = totalWidthMM
        totalWidthMM += serverDeviceInfo.widthMM
    }

}
