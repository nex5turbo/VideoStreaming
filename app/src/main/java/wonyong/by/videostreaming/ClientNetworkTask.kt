package wonyong.by.videostreaming

import android.os.AsyncTask
import android.util.Log
import java.io.*
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
                    var dos = DataOutputStream(socket?.getOutputStream())
                    dos.writeUTF(CONST.N_READY_FILE_TRANSFER)
                    receiveMessage = dis.readUTF()
                    clientActivityData?.fileName = receiveMessage
                    var file = File(clientActivityData?.storage + "/" + receiveMessage)
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
                playerActivity?.socket = Socket(playerActivityData?.hostAddress, CONST.NETWORK_PLAYER_PORT)
                Log.d("###", "Socket Connect")
            }
        }
        return null
    }

}