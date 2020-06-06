package wonyong.by.videostreaming

import android.util.Log
import java.io.DataOutputStream

class ClientBufferThread(var mode : String, var playerActivity: ClientPlayerActivity) : Thread(){
    val CONST = Consts()
    override fun run() {
        when(mode){
            CONST.N_PLAYER_READY_BUFFER->{
                var socket = playerActivity?.bufferSocket
                var dos = DataOutputStream(socket?.getOutputStream())
                Log.d("###", "dos")
                dos.writeUTF(CONST.N_PLAYER_READY_BUFFER)
            }
        }
    }
}