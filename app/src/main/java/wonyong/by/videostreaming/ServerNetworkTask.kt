package wonyong.by.videostreaming

import android.os.AsyncTask
import android.util.Log
import android.widget.TextView
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.lang.Exception
import java.net.InetAddress
import java.net.InetSocketAddress
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
                serverSocket.setReuseAddress(true)
                var socket = serverSocket.accept()

                    var dis = DataInputStream(socket.getInputStream())
                    var receiveMessage = dis.readUTF()
                    var st: StringTokenizer
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
                    Log.d(
                        "###",
                        heightMM + CONST.DELIMETER + heightPixel + CONST.DELIMETER + widthMM + CONST.DELIMETER + widthPixel + CONST.DELIMETER + deviceOrder + CONST.DELIMETER + inetAddress
                    )

                    taskListener.addClientDeviceInfo(
                        heightPixel.toInt(),
                        widthPixel.toInt(),
                        widthMM.toFloat(),
                        heightMM.toFloat(),
                        deviceOrder.toInt(),
                        inetAddress
                    )
                    dis.close()
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

                    var dos = DataOutputStream(socket.getOutputStream())
                    Log.d("##N_PLAY_VIDEO", "socket")
                    dos.writeUTF(CONST.N_PLAY_VIDEO)
                    Log.d("##N_PLAY_VIDEO", "send")
                    dos.close()
                    socket.close()
                }
            }
        }
        return null
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