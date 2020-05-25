package wonyong.by.videostreaming

import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import java.io.*
import java.lang.Thread.sleep
import java.lang.ref.WeakReference
import java.net.InetAddress
import java.net.Socket

class ClientNetworkTask(var mode:String, val activity : ClientActivity?, val playerActivity: ClientPlayerActivity?) : AsyncTask<Void, Void, Void>() {

    var CONST = Consts()
    var dataRef = WeakReference(activity)
    var clientActivityData = dataRef.get()
    var socket = clientActivityData?.socket

    var playerDataRef = WeakReference(playerActivity)
    var playerActivityData = playerDataRef.get()
    var playerSocket = playerActivityData?.socket

    override fun doInBackground(vararg p0: Void?): Void? {


        when(mode){
            CONST.L_WAITING_RECEIVE->{

                var inputStream = socket?.getInputStream()

                var dis = DataInputStream(inputStream)
                var receiveMessage = dis.readUTF()

                Log.d("##receive", receiveMessage)
                if (receiveMessage.equals(CONST.N_PLAY_VIDEO)) {
                    clientActivityData?.socket?.close()
                    clientActivityData?.playVideo()
                    return null
                }else if(receiveMessage.equals(CONST.N_REQUEST_READY_FILE_TRANSFER)){

                    receiveMessage = dis.readUTF()
                    clientActivityData?.fileName = receiveMessage
                    var dos = DataOutputStream(socket?.getOutputStream())
                    var file = File(clientActivityData?.storage + "/" + receiveMessage)
                    if(file.exists()){
                        Log.d("###", file.exists().toString())
                        dos.writeUTF(CONST.N_FILE_EXIST)
                        clientActivityData?.filetransferOver()
                        clientActivityData?.onWait()
                        return null
                    }
                    dos.writeUTF(CONST.N_READY_FILE_TRANSFER)


                    var fos = FileOutputStream(file)
                    var bos = BufferedOutputStream(fos)

                    var len : Int = 0
                    var lenSum = 0
                    var size = 1024
                    var data = ByteArray(size)
                    while(len > -1){
                        if(len == -1){
                            break
                        }
                        len = dis.read(data)
                        if(len == -1){
                            break
                        }

                        lenSum = lenSum + len
                        Log.d("###", "len#" + len.toString())
                        Log.d("###", "lenSum" + lenSum.toString())

                        bos.write(data, 0, len)
                    }

                    Log.d("###", "File transfer over")

                    clientActivityData?.filetransferOver()
                    clientActivityData?.socket?.close()
                    clientActivityData?.socket = Socket(clientActivityData?.hostAddress, CONST.NETWORK_MESSAGE_PORT)
                }
                Log.d("###", "before on wait")
                clientActivityData?.onWait()
            }
            CONST.L_ON_CONNECT->{

                socket = Socket(clientActivityData?.hostAddress, CONST.NETWORK_MESSAGE_PORT)
                clientActivityData?.socket = socket
                clientActivityData?.localAddress ="for test"

                Log.d("###", clientActivityData?.localAddress)
                Log.d("###", socket?.inetAddress.toString())
                Log.d("###", socket?.localAddress.toString())
                clientActivityData?.deviceInfo?.socket = socket

                var dos = DataOutputStream(socket?.getOutputStream())
                dos.writeUTF("0"+CONST.DELIMETER+"0"+CONST.DELIMETER+clientActivityData?.deviceInfo?.widthMM+CONST.DELIMETER+clientActivityData?.deviceInfo?.heightMM+CONST.DELIMETER+"0"+CONST.DELIMETER+socket?.localAddress)
                dos.flush()
                clientActivityData?.onWait()

            }
            CONST.L_PLAYER_ON_CONNECT->{
                Log.d("###", "Before Socket Connect")
                playerActivityData?.socket = Socket(playerActivityData?.hostAddress, CONST.NETWORK_PLAYER_PORT)
                var dis = DataInputStream(playerActivityData?.socket?.getInputStream())
                var receiveMessage = dis.readUTF()

                var dos = DataOutputStream(playerActivityData?.socket?.getOutputStream())
                dos.writeUTF("PLAYER_ON_CONNECT")

                receiveMessage = dis.readUTF()
                playerActivityData?.timeRate = receiveMessage.toLong()

                playerActivityData?.onWait()
                Log.d("###", "Socket Connect")
            }
            CONST.L_PLAYER_WAITING_RECEIVE->{
                var dis = DataInputStream(playerActivityData?.socket?.getInputStream())
                var receiveMessage = dis.readUTF()
                when(receiveMessage){
                    CONST.N_PLAYER_PLAY->{
                        playerActivityData?.playVideo()
                    }
                    CONST.N_PLAYER_PAUSE->{
                        playerActivityData?.pauseVideo()
                    }
                    CONST.N_PLAYER_FORWARD->{
                        receiveMessage = dis.readUTF()
                        var position = receiveMessage.toInt()
                        playerActivityData?.forward(position)
                    }
                    CONST.N_PLAYER_BACKWARD->{
                        receiveMessage = dis.readUTF()
                        var position = receiveMessage.toInt()
                        playerActivityData?.backward(position)
                    }
                }
                playerActivityData?.onWait()
            }

        }
        return null
    }

}