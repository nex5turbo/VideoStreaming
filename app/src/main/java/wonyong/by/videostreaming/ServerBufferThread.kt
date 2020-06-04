package wonyong.by.videostreaming

import android.util.Log
import java.io.DataInputStream

class ServerBufferThread(var mode : String, var playerActivity: ServerPlayerActivity) : Thread() {
    val CONST = Consts()
    override fun run() {
        if(mode.equals(CONST.L_PLAYER_SERVER_WAITING_RECEIVE)){
            var socket = playerActivity?.bufferSocketList[0]
            var dis = DataInputStream(socket.getInputStream())
            var receiveMessage = dis.readUTF()
            Log.d("###", receiveMessage)
        }else if(mode.equals(CONST.L_PLAYER_SERVER_WAITING_RECEIVE_2)){
            var socket = playerActivity?.bufferSocketList[1]
            var dis = DataInputStream(socket.getInputStream())
            var receiveMessage = dis.readUTF()
            Log.d("###", receiveMessage)
        }
    }
}