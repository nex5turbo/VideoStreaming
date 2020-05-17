package wonyong.by.videostreaming

import android.os.AsyncTask
import android.util.Log
import android.widget.TextView
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

class ServerNetworkTask(var mode : String, var textView: TextView) : AsyncTask<Void, Void, Void>() {
    var CONST = Consts()
    override fun doInBackground(vararg p0: Void?): Void? {

        var ss = ServerSocket(8888)
        var socket : Socket = ss.accept()
        var addr = socket.inetAddress
        Log.d("###loc",socket.localAddress.toString())


        var inputStream = socket.getInputStream()
        var dis = DataInputStream(inputStream)
        var msg = dis.readUTF()
        Log.d("###msg", msg)
        return null

//        when(mode){
//            CONST.N_SEND_MESSAGE->{
//                var serverSocket = ServerSocket(CONST.NETWORK_MESSAGE_PORT+2)
//                var socket = serverSocket.accept()
//                var outputStream = socket.getOutputStream()
//                var dos = DataOutputStream(outputStream)
//
//                dos.writeUTF("test message"+CONST.DELIMETER+socket.localAddress)
//                dos.close()
//                socket.close()
//                serverSocket.close()
//            }
//        }
//        return null
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