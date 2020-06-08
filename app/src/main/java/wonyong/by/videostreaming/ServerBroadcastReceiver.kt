package wonyong.by.videostreaming

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_server.*

class ServerBroadcastReceiver(var mManager : WifiP2pManager,
                              var mChannel : WifiP2pManager.Channel,
                              var mActivity:ServerActivity):BroadcastReceiver() {
    var connectedDevice = 1
    override fun onReceive(context : Context, intent: Intent) {
        var action:String? = intent.getAction()

        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){
            var state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
            if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){

            }
            else{

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
                    mActivity.serverWifiDirectConnectionStatus.setText("연결끊김")
                }
            }
        }
        else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){
            //TODO
        }
    }

}