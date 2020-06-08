package wonyong.by.videostreaming

import android.util.Log
import kotlinx.android.synthetic.main.activity_server_player.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket

class ServerBufferThread(var mode : String, var playerActivity: ServerPlayerActivity) : Thread() {
    val CONST = Consts()
    override fun run() {
        if(mode.equals(CONST.L_PLAYER_SERVER_WAITING_RECEIVE)){
            Log.d("###", "waiting1")
            var socket = playerActivity?.bufferSocketList[0]
            var dis = DataInputStream(socket.getInputStream())
            var receiveMessage = dis.readUTF()
            when(receiveMessage){
                CONST.N_PLAYER_READY_BUFFER->{
                    if(playerActivity?.bufferReady == false)
                        playerActivity?.bufferReady = true
                    else{
                        playerActivity?.bufferReady = false
                        playerActivity?.callAsyncTask(CONST.N_BUFFER_OVER)
                    }
                    playerActivity?.serverVideoView.seekTo(playerActivity?.bufferPosition)
                    playerActivity?.serverOnWait1()
                    return
                }

                CONST.N_PLAYER_BUFFER->{
                    if(playerActivity?.isForwarding){
                        var dos = DataOutputStream(socket.getOutputStream())
                        dos.writeUTF("FORWARDING")
                        playerActivity?.serverOnWait1()
                        return
                    }
                    var dos = DataOutputStream(socket.getOutputStream())
                    dos.writeUTF("OK")
                    playerActivity?.pauseVideo()
                    playerActivity?.bufferPosition = playerActivity?.serverVideoView.currentPosition
                    var i = 0
                    for(sock: Socket in playerActivity!!.socketList){
                        Log.d("###", "iteration"+i.toString())
                        var dos = DataOutputStream(sock.getOutputStream())
                        Log.d("###", "iteration"+i.toString())
                        dos.writeUTF(CONST.N_PLAYER_BUFFER)
                        Log.d("###", "iteration"+i.toString())
                        dos.writeUTF(playerActivity?.bufferPosition.toString())
                        Log.d("###", playerActivity?.bufferPosition.toString())
                        i++
                    }
                    playerActivity?.bufferReady = false
                    playerActivity?.serverOnWait1()
                    return
                }
                CONST.N_PLAYER_EXIT->{
                    for(sock : Socket in playerActivity!!.socketList){
                        var dos = DataOutputStream(sock.getOutputStream())
                        dos.writeUTF(CONST.N_PLAYER_EXIT)
                        sock.close()
                    }
                    playerActivity?.playerServerSocket?.close()
                    playerActivity?.finish()

                }
            }

        }else if(mode.equals(CONST.L_PLAYER_SERVER_WAITING_RECEIVE_2)){
            Log.d("###", "waiting2")
            var socket = playerActivity?.bufferSocketList[1]
            var dis = DataInputStream(socket.getInputStream())
            var receiveMessage = dis.readUTF()
            when(receiveMessage){
                CONST.N_PLAYER_READY_BUFFER->{
                    if(playerActivity?.bufferReady == false)
                        playerActivity?.bufferReady = true
                    else{
                        playerActivity?.bufferReady = false
                        playerActivity?.callAsyncTask(CONST.N_PLAYER_PLAY)
                    }
                    playerActivity?.serverVideoView.seekTo(playerActivity?.bufferPosition)
                    playerActivity?.serverOnWait2()
                    return
                }
                CONST.N_PLAYER_BUFFER->{
                    if(playerActivity?.isForwarding){
                        var dos = DataOutputStream(socket.getOutputStream())
                        dos.writeUTF("FORWARDING")
                        playerActivity?.serverOnWait2()
                        return
                    }
                    var dos = DataOutputStream(socket.getOutputStream())
                    dos.writeUTF("OK")
                    playerActivity?.pauseVideo()
                    playerActivity?.bufferPosition = playerActivity?.serverVideoView.currentPosition
                    var i = 0
                    for(sock: Socket in playerActivity!!.socketList){
                        Log.d("###", "iteration"+i.toString())
                        var dos = DataOutputStream(sock.getOutputStream())
                        Log.d("###", "iteration"+i.toString())
                        dos.writeUTF(CONST.N_PLAYER_BUFFER)
                        Log.d("###", "iteration"+i.toString())
                        dos.writeUTF(playerActivity?.bufferPosition.toString())
                        Log.d("###", playerActivity?.bufferPosition.toString())
                        i++
                    }
                    playerActivity?.serverOnWait2()
                    return
                }
                CONST.N_PLAYER_EXIT->{
                    for(sock : Socket in playerActivity!!.socketList){
                        var dos = DataOutputStream(sock.getOutputStream())
                        dos.writeUTF(CONST.N_PLAYER_EXIT)
                        sock.close()
                    }
                    playerActivity?.playerServerSocket?.close()
                    playerActivity?.finish()
                }
            }

        }
    }
}