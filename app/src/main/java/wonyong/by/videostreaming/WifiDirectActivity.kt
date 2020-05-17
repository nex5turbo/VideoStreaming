package wonyong.by.videostreaming

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.*
import android.os.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_wifi_direct.*
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

class WifiDirectActivity : AppCompatActivity() {
    lateinit var wifiManager:WifiManager
    lateinit var wifiP2pManager : WifiP2pManager;
    lateinit var wifiP2pChannel : WifiP2pManager.Channel
    lateinit var broadcastReceiver : BroadcastReceiver
    lateinit var intentFilter : IntentFilter
    lateinit var deviceNameArray:Array<String?>
    lateinit var deviceArray:Array<WifiP2pDevice?>
    lateinit var wifiStatus : TextView
    val CONST = Consts()
    var connectedDevice = 0

    var peers:ArrayList<WifiP2pDevice> = ArrayList<WifiP2pDevice>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi_direct)
        init()
        buttonListener()
        getInfo()
    }

    private fun getInfo() {
    }


    private fun buttonListener() {
        wifiDirectConnectButton.setOnClickListener {
            if(wifiManager.isWifiEnabled()){
                wifiManager.setWifiEnabled(false)
                wifiDirectConnectButton.setText("Wifi-On")
            }
            else{
                wifiManager.setWifiEnabled(true)
                wifiDirectConnectButton.setText("Wifi-Off")
            }


        }

        wifiDirectRefreshButton.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this@WifiDirectActivity,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this@WifiDirectActivity,
                    arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),
                    100)
            }

            else {
                wifiP2pManager.discoverPeers(wifiP2pChannel, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        wifiDirectConnectionStatus.setText("검색중")
                    }

                    override fun onFailure(i: Int) {
                        wifiDirectConnectionStatus.setText("검색실패")
                    }
                })
            }
        }

        wifiDirectListView.setOnItemClickListener(object : AdapterView.OnItemClickListener{
            override fun onItemClick(adapterView: AdapterView<*>, view : View?, i: Int, l: Long) {
                val device:WifiP2pDevice? = deviceArray[i]
                var config : WifiP2pConfig = WifiP2pConfig()
                config.deviceAddress = device?.deviceAddress
                config.wps.setup = WpsInfo.PBC
                config.groupOwnerIntent = 0

                wifiP2pManager.connect(wifiP2pChannel, config, object :WifiP2pManager.ActionListener{
                    override fun onSuccess() {
                        Toast.makeText(applicationContext, device?.deviceName+" 로 연결합니다.", Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(p0: Int) {
                        Toast.makeText(applicationContext, "연결불가", Toast.LENGTH_SHORT).show()
                    }
                })

//                AlertDialog.Builder(this@WifiDirectActivity)
//                    .setTitle("Wifi-Direct Connection")
//                    .setMessage("연결하시겠습니까?")
//                    .setIcon(R.drawable.abc_ic_menu_share_mtrl_alpha)
//                    .setPositiveButton(android.R.string.yes, object : DialogInterface.OnClickListener{
//                        override fun onClick(p0: DialogInterface?, p1: Int) {
//
//
//                        }
//                    })
//                    .setNegativeButton(android.R.string.no, object : DialogInterface.OnClickListener{
//                        override fun onClick(p0: DialogInterface?, p1: Int) {
//                            Toast.makeText(this@WifiDirectActivity, "취소했습니다.", Toast.LENGTH_SHORT).show()
//                        }
//                    })
//                    .show()
            }
        })


        wifiDirectDisconnectButton.setOnClickListener {
            //TODO Group Owner일 경우 연결끊으면 어떻게 해야하는지
            wifiP2pManager.cancelConnect(wifiP2pChannel, object :WifiP2pManager.ActionListener{
                override fun onSuccess() {
                }

                override fun onFailure(p0: Int) {

                }
            })

            wifiP2pManager.removeGroup(wifiP2pChannel, object :WifiP2pManager.ActionListener{
                override fun onSuccess() {
                    Toast.makeText(this@WifiDirectActivity, "연결이 해제됩니다.", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(p0: Int) {

                }
            })
        }

        wifiDirectTestButton.setOnClickListener {
        }
    }

    private fun init() {
        wifiManager = application.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiP2pManager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        wifiP2pChannel = wifiP2pManager.initialize(this, Looper.getMainLooper(), null)
        broadcastReceiver = ServerBroadcastReceiver(wifiP2pManager, wifiP2pChannel, ServerActivity())
        intentFilter = IntentFilter()
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        wifiStatus = wifiDirectConnectionStatus


    }

    var peerListListener : WifiP2pManager.PeerListListener = object : WifiP2pManager.PeerListListener{
        override fun onPeersAvailable(peerList: WifiP2pDeviceList){

            if(!peerList.deviceList.equals(peers)){

                peers.clear()
                peers.addAll(peerList.deviceList)

                deviceNameArray = arrayOfNulls<String>(peerList.deviceList.size)
                deviceArray = arrayOfNulls<WifiP2pDevice>(peerList.deviceList.size)
                var index = 0
                for(device : WifiP2pDevice in peerList.deviceList){
                    deviceNameArray[index] = device.deviceName
                    deviceArray[index] = device
                    index++
                }

                var nameAdapter : ArrayAdapter<String> = ArrayAdapter<String>(applicationContext, android.R.layout.simple_list_item_1, deviceNameArray)
                wifiDirectListView.setAdapter(nameAdapter)
            }
            if(peers.size == 0){

                return
            }
        }
    }

    var connectInfoListener : WifiP2pManager.ConnectionInfoListener = object : WifiP2pManager.ConnectionInfoListener{
        override fun onConnectionInfoAvailable(wifiP2pInfo: WifiP2pInfo) {
            val groupOwnerAddress : InetAddress = wifiP2pInfo.groupOwnerAddress
            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner){

            }
            else if(wifiP2pInfo.groupFormed){

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
        wifiP2pManager.cancelConnect(wifiP2pChannel, object :WifiP2pManager.ActionListener{
            override fun onSuccess() {
            }

            override fun onFailure(p0: Int) {

            }
        })

        wifiP2pManager.removeGroup(wifiP2pChannel, object :WifiP2pManager.ActionListener{
            override fun onSuccess() {
                Toast.makeText(this@WifiDirectActivity, "연결이 해제됩니다.", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(p0: Int) {

            }
        })

    }
}
