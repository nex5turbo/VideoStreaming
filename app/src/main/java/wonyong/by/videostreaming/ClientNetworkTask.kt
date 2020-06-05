package wonyong.by.videostreaming

import android.os.AsyncTask
import android.util.Log
import java.io.*
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
                }else if(receiveMessage.equals(CONST.N_REQUEST_READY_FILE_TRANSFER)){

                    receiveMessage = dis.readUTF()
                    clientActivityData?.fileName = receiveMessage
                    var dos = DataOutputStream(socket?.getOutputStream())
                    var file = File(clientActivityData?.storage + "/" + receiveMessage)
                    if(file.exists()){
                        dos.writeUTF(CONST.N_FILE_EXIST)
                        clientActivityData?.filetransferOver()
                        clientActivityData?.onWait()
                        return null
                    }
                    dos.writeUTF(CONST.N_READY_FILE_TRANSFER)

                    var FILE_OUTPUT_STREAM = FileOutputStream(file)
                    var BUFFERED_OUTPUT_STREAM = BufferedOutputStream(FILE_OUTPUT_STREAM)
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
                        BUFFERED_OUTPUT_STREAM.write(data, 0, len)
                    }

                    Log.d("###", "File transfer over")

                    clientActivityData?.filetransferOver()
                    clientActivityData?.socket?.close()
                    clientActivityData?.socket = Socket(clientActivityData?.hostAddress, CONST.NETWORK_MESSAGE_PORT)
                }else if(receiveMessage.equals(CONST.N_FILE_STREAMING_START)){
                    receiveMessage = dis.readUTF()
                    clientActivityData?.fileName = receiveMessage
                    clientActivityData?.onWait()
                    Log.d("###fileName", clientActivityData?.fileName)
                    var fileDis = DataInputStream(dataSocket?.getInputStream())
                    var file = File(clientActivityData?.storage + "/" + receiveMessage)
                    var FILE_OUTPUT_STREAM = FileOutputStream(file)
                    var BUFFERED_OUTPUT_STREAM = BufferedOutputStream(FILE_OUTPUT_STREAM)
                    var len : Int = 0
                    var lenSum = 0
                    var size = 4096
                    var data = ByteArray(size)
                    while(len > -1){
                        if(len == -1){
                            break
                        }
                        len = fileDis.read(data)
                        if(len == -1){
                            break
                        }
                        lenSum = lenSum + len
                        BUFFERED_OUTPUT_STREAM.write(data, 0, len)
                    }
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
                clientActivityData?.totalWidthPixel = st.nextToken().toInt()
                clientActivityData?.deviceInfo?.deviceOrder = st.nextToken().toInt()
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
                dos.writeUTF(CONST.N_PLAYER_BUFFER)
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
                        receiveMessage = dis.readUTF()
                        Log.d("###", receiveMessage)
                        playerActivityData?.setAfterBuffered(receiveMessage.toInt())

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