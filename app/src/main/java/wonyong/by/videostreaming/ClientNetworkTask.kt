package wonyong.by.videostreaming

import android.os.AsyncTask
import android.util.Log
import java.io.*
import java.lang.Thread.sleep
import java.lang.ref.WeakReference
import java.net.Socket
import java.util.*

class ClientNetworkTask(var mode:String, val activity : ClientActivity?, val playerActivity: ClientPlayerActivity?) : AsyncTask<Void, Void, Void>() {

    var CONST = Consts()
    var dataRef = WeakReference(activity)
    var clientActivityData = dataRef.get()
    var socket = clientActivityData?.socket
    var dataSocket = clientActivityData?.dataSocket

    var playerDataRef = WeakReference(playerActivity)
    var playerActivityData = playerDataRef.get()
    var playerSocket = playerActivityData?.socket

    override fun doInBackground(vararg p0: Void?): Void? {


        when(mode){
            CONST.L_WAITING_RECEIVE->{
                var inputStream = socket?.getInputStream()
                var dis = DataInputStream(inputStream)
                var receiveMessage = dis.readUTF()
                Log.d("###", receiveMessage)
                if (receiveMessage.equals(CONST.N_PLAY_VIDEO)) {
                    clientActivityData?.playVideo()
                    return null
                }else if(receiveMessage.equals(CONST.N_FILE_STREAMING_START)){
                    receiveMessage = dis.readUTF()
                    clientActivityData?.fileName = receiveMessage
                    receiveMessage = dis.readUTF()
                    var st = StringTokenizer(receiveMessage, CONST.DELIMETER)
                    clientActivityData?.fileSize = st.nextToken().toLong()
                    clientActivityData?.moovSize = st.nextToken().toLong()
                    clientActivityData?.onWait()
                    Log.d("###fileName", clientActivityData?.fileName)
                    var fileDis = DataInputStream(dataSocket?.getInputStream())
                    var file = File(clientActivityData?.storage + "/" + clientActivityData?.fileName)
                    var FILE_OUTPUT_STREAM = FileOutputStream(file)
                    var BUFFERED_OUTPUT_STREAM = BufferedOutputStream(FILE_OUTPUT_STREAM)
                    var len : Int = 0
                    var lenSum = 0
                    var size = 4096
                    var data = ByteArray(size)
                    while(len > -1){
                        len = fileDis.read(data)
                        if(len == -1){
                            break
                        }
                        lenSum = lenSum + len
                        BUFFERED_OUTPUT_STREAM.write(data, 0, len)
                        BUFFERED_OUTPUT_STREAM.flush()
                    }
                    BUFFERED_OUTPUT_STREAM.close()
                    FILE_OUTPUT_STREAM.close()
                    Log.d("###", "File transfer over")

                    return null
                }
                Log.d("###", "before on wait")
                clientActivityData?.onWait()
            }
            CONST.L_ON_CONNECT->{

                socket = Socket(clientActivityData?.hostAddress, CONST.NETWORK_MESSAGE_PORT)
                dataSocket = Socket(clientActivityData?.hostAddress, 8585)
                clientActivityData?.dataSocket = dataSocket
                Log.d("###", "dataSocket Connect")
                clientActivityData?.socket = socket
                Log.d("###", "Socket Connect")
                clientActivityData?.localAddress ="for test"
                clientActivityData?.deviceInfo?.socket = socket

                var dos = DataOutputStream(socket?.getOutputStream())
                dos.writeUTF(clientActivityData?.deviceInfo?.widthPixel.toString()+CONST.DELIMETER+
                                    clientActivityData?.deviceInfo?.heightPixel.toString()+CONST.DELIMETER+
                                    clientActivityData?.deviceInfo?.widthMM.toString()+CONST.DELIMETER+
                                    clientActivityData?.deviceInfo?.heightMM.toString()+CONST.DELIMETER+
                                    "0"+CONST.DELIMETER+
                                    socket?.localAddress)
                var dis = DataInputStream(socket?.getInputStream())
                var receiveMessage = dis.readUTF()
                Log.d("###", receiveMessage)
                var st = StringTokenizer(receiveMessage, CONST.DELIMETER)
                clientActivityData?.totalWidthMM = st.nextToken().toFloat()
                clientActivityData?.deviceInfo?.deviceOrder = st.nextToken().toInt()
                clientActivityData?.aX = st.nextToken().toFloat()
                Log.d("###onconnectax", clientActivityData?.aX.toString())
                dos.flush()
                clientActivityData?.onWait()
                return null

            }
            CONST.L_PLAYER_ON_CONNECT->{
                Log.d("###", "Before Socket Connect")
                playerActivityData?.socket = Socket(playerActivityData?.hostAddress, CONST.NETWORK_PLAYER_PORT)
                playerActivityData?.bufferSocket = Socket(playerActivityData?.hostAddress, 9090)
                var dis = DataInputStream(playerActivityData?.socket?.getInputStream())
                var receiveMessage = dis.readUTF()

                var dos = DataOutputStream(playerActivityData?.socket?.getOutputStream())
                dos.writeUTF("PLAYER_ON_CONNECT")

                receiveMessage = dis.readUTF()
                playerActivityData?.timeRate = receiveMessage.toLong()

                playerActivityData?.onWait()
                Log.d("###", "Socket Connect")
                return null

            }
            CONST.N_PLAYER_BUFFER->{
                var dos = DataOutputStream(playerActivityData?.bufferSocket?.getOutputStream())
                var dis = DataInputStream(playerActivityData?.bufferSocket?.getInputStream())
                dos.writeUTF(CONST.N_PLAYER_BUFFER)
                var receiveMessage = dis.readUTF()
                while(receiveMessage.equals("FORWARDING")){
                    Log.d("###while", receiveMessage)
                    sleep(500)
                    dos.writeUTF(CONST.N_PLAYER_BUFFER)
                    receiveMessage = dis.readUTF()
                }
                return null
            }
            CONST.N_READY_FORWARD->{
                var dos = DataOutputStream(playerActivityData?.socket?.getOutputStream())
                dos.writeUTF(CONST.N_READY_FORWARD)
                dos.flush()
                return null
            }
            CONST.L_PLAYER_CLIENT_WAITING_RECEIVE->{
                Log.d("###", "onWait")
                var dis = DataInputStream(playerActivityData?.socket?.getInputStream())
                Log.d("###", "ready2Receive")
                var receiveMessage = dis.readUTF()
                Log.d("###", receiveMessage)
                when(receiveMessage){
                    CONST.N_PLAYER_BUFFER->{
                        playerActivityData?.pauseVideo()
                    }
                    CONST.N_BUFFER_OVER->{
                        receiveMessage = dis.readUTF()
                        var position = receiveMessage.toInt()
                        playerActivityData?.bufferOver(position)
                    }
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
                    CONST.N_PLAYER_EXIT->{
                        playerActivityData?.exitPlayer()
                        return null
                    }
                }
                playerActivityData?.onWait()
            }

        }
        return null
    }
}