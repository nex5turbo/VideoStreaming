package wonyong.by.videostreaming

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_client.*

class ClientBroadcastReceiver(var mManager : WifiP2pManager,
                              var mChannel : WifiP2pManager.Channel,
                              var mActivity:ClientActivity):BroadcastReceiver() {
    override fun onReceive(context : Context, intent: Intent) {
        var action:String? = intent.getAction()

        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){
            var state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
            if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                Toast.makeText(context, "Wifi is on", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(context, "Wifi is off", Toast.LENGTH_SHORT).show()
            }
        }
        else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
            if(mManager != null){
                Log.e("PEERS_CHANGED", "inner")
                mManager.requestPeers(mChannel, mActivity.peerListListener)
            }
        }
        else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
            if(mManager != null){
                var networkInfo : NetworkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO)
                if(networkInfo.isConnected){
                    mManager.requestConnectionInfo(mChannel, mActivity.connectInfoListener)
                }
                else{
                    mActivity.clientWifiDirectConnectionStatus.setText("연결끊김")
                }
            }
        }
        else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){
            //TODO
        }
    }

}