package wonyong.by.videostreaming

import android.os.AsyncTask
import android.util.Log
import android.widget.TextView
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.*

class ServerNetworkTask(var mode : String, var textView: TextView, var taskListener: ServerTaskListener, var clientList : ArrayList<DeviceInfo>) : AsyncTask<Void, Void, Void>() {
    var CONST = Consts()
    override fun doInBackground(vararg p0: Void?): Void? {

        when(mode){
            CONST.N_ON_CONNECT->{
                Log.d("###", "inner")
                var serverSocket = ServerSocket(CONST.NETWORK_MESSAGE_PORT)
                var socket = serverSocket.accept()

                var dis = DataInputStream(socket.getInputStream())
                var receiveMessage = dis.readUTF()
                var st : StringTokenizer
                st = StringTokenizer(receiveMessage, CONST.DELIMETER)
                var heightPixel = st.nextToken()
                Log.d("hp", heightPixel)
                var widthPixel = st.nextToken()
                Log.d("hp", widthPixel)
                var widthMM = st.nextToken()
                Log.d("hp", widthMM)
                var heightMM = st.nextToken()
                var deviceOrder = st.nextToken()
                var inetAddress = st.nextToken()
                Log.d("###", heightMM+CONST.DELIMETER+heightPixel+CONST.DELIMETER+widthMM+CONST.DELIMETER+widthPixel+CONST.DELIMETER+deviceOrder+CONST.DELIMETER+inetAddress)

                ui(receiveMessage)
                taskListener.addClientDeviceInfo(heightPixel.toInt(), widthPixel.toInt(), widthMM.toFloat(), heightMM.toFloat(), deviceOrder.toInt(), inetAddress)
                taskListener.playVideo()
                socket.close()
                serverSocket.close()


            }


            CONST.N_SEND_MESSAGE->{

            }

            CONST.N_PLAY_VIDEO->{
                Log.d("##N_PLAY_VIDEO", "out")
                for(deviceInfo:DeviceInfo in clientList){
                    Log.d("##N_PLAY_VIDEO", deviceInfo.inetAddress)
                    var socket = Socket(deviceInfo.inetAddress, CONST.NETWORK_MESSAGE_PORT)
                    while(!socket.isConnected){
                        socket.close()
                        socket = Socket(deviceInfo.inetAddress, CONST.NETWORK_MESSAGE_PORT)
                        Log.d("##N_PLAY_VIDEO", "loop")
                    }
                    var dos = DataOutputStream(socket.getOutputStream())
                    Log.d("##N_PLAY_VIDEO", "socket")
                    dos.writeUTF(CONST.N_PLAY_VIDEO)
                    Log.d("##N_PLAY_VIDEO", "send")
                    socket.close()
                }
            }
        }
        return null
    }

    fun ui(addr: String){
        textView.setText(addr)
    }


}
//socket.inetAddress->접속된 상대의 ip주소




//fun runon(addr:InetAddress){
//    var th = Thread(object :Runnable{
//        override fun run() {
//            Log.d("###addr", addr.toString())
//            textView.setText(addr.toString())
//        }
//    }).start()
//}