package wonyong.by.videostreaming

import android.os.AsyncTask
import android.util.Log
import android.widget.TextView
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

class ServerNetworkTask(var mode : String, var textView: TextView, var addr:String) : AsyncTask<Void, Void, Void>() {
    var CONST = Consts()
    override fun doInBackground(vararg p0: Void?): Void? {

//        var ss = ServerSocket(8888)
//        var socket : Socket = ss.accept()
//        var addr = socket.inetAddress
//        Log.d("###loc",socket.localAddress.toString())
//
//
//        var inputStream = socket.getInputStream()
//        var dis = DataInputStream(inputStream)
//        var msg = dis.readUTF()
//        Log.d("###msg", msg)
//        return null

        when(mode){
            CONST.N_ON_CONNECT->{
                var serverSocket = ServerSocket(CONST.NETWORK_MESSAGE_PORT)
                var socket = serverSocket.accept()

                ui(socket.inetAddress.toString())
                socket.close()
                serverSocket.close()
            }


            CONST.N_SEND_MESSAGE->{
                var socket = Socket(addr, CONST.NETWORK_MESSAGE_PORT)//아이피주소 받아와서 여기두개 넣어줘야함
                var outputStream = socket.getOutputStream()
                var dos = DataOutputStream(outputStream)

                dos.writeUTF("test message"+CONST.DELIMETER+socket.localAddress)
                dos.close()
                socket.close()
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