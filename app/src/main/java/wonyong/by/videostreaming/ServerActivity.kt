package wonyong.by.videostreaming

import android.content.Context
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_server.*
import java.lang.Thread.sleep
import java.net.*
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
    var deviceList = arrayListOf<DeviceInfo>()
    lateinit var wifiManager:WifiManager
    lateinit var wifiP2pManager : WifiP2pManager;
    lateinit var wifiP2pChannel : WifiP2pManager.Channel
    lateinit var broadcastReceiver : ServerBroadcastReceiver
    lateinit var intentFilter : IntentFilter
    lateinit var deviceNameArray:Array<String?>
    lateinit var deviceArray:Array<WifiP2pDevice?>
    lateinit var serverDeviceInfo: DeviceInfo
    var clientDeviceInfoList = ArrayList<DeviceInfo>()
    var resultPath : String? = null
    var fileName = ""
    var connectedDevice = 1
    var peers:ArrayList<WifiP2pDevice> = ArrayList<WifiP2pDevice>()
    var totalWidthPixel = 0


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
                if(DN.equals("streaming")){
                    callAsyncTask(CONST.N_FILE_STREAMING_START)
                    sleep(500)
                    streamingOrder++
                    callAsyncTask(CONST.N_FILE_STREAMING_START)
                    playEnable()
                }else{
                    callAsyncTask(CONST.N_REQUEST_READY_FILE_TRANSFER)
                    playEnable()
                }

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
                serverWifiDirectTitle.setText(resultPath)
                sendEnable()
            }
        }
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
        serverWifiDirectConnectionStatus.setText("파일전송이 완료되었습니다.")
    }

    override fun sendEnable() {
        serverWifiDirectSendVideoButton.isEnabled = true
    }

    override fun playEnable() {
        serverWifiDirectPlayVideoButton.isEnabled = true
    }

    override fun calcPixel() {
        for(di:DeviceInfo in clientDeviceInfoList){
            totalWidthPixel += di.widthPixel
        }
        totalWidthPixel += serverDeviceInfo.widthPixel
    }

}
