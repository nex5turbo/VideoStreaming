package wonyong.by.videostreaming

import android.os.AsyncTask
import android.util.Log
import android.widget.TextView
import java.io.*
import java.lang.Exception
import java.lang.ref.WeakReference
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.*

class ServerNetworkTask(var mode : String, var activity: ServerActivity) : AsyncTask<Void, Void, Void>() {
    var CONST = Consts()
    override fun doInBackground(vararg p0: Void?): Void? {

        var dataRef= WeakReference(activity)
        var serverActivity = dataRef.get()
        var socket = serverActivity?.socket

        when(mode){
            CONST.N_ON_CONNECT->{
                Log.d("###", "inner")
                serverActivity?.serverSocket = ServerSocket(CONST.NETWORK_MESSAGE_PORT)
                serverActivity?.serverSocket?.reuseAddress = true
                for(i in 0..1) {
                    socket = serverActivity?.serverSocket?.accept()
                    serverActivity?.socket = socket
                    var dis = DataInputStream(socket?.getInputStream())
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

                    serverActivity?.addClientDeviceInfo(
                        heightPixel.toInt(),
                        widthPixel.toInt(),
                        widthMM.toFloat(),
                        heightMM.toFloat(),
                        deviceOrder.toInt(),
                        socket!!
                    )
                }

            }

            CONST.N_REQUEST_READY_FILE_TRANSFER->{
                for(di:DeviceInfo in serverActivity!!.clientDeviceInfoList) {
                    var diSocket = di.socket
                    var dos = DataOutputStream(diSocket?.getOutputStream())
                    dos.writeUTF(CONST.N_REQUEST_READY_FILE_TRANSFER)

                    var dis = DataInputStream(diSocket?.getInputStream())
                    var receiveMessage = dis.readUTF()
                    if (receiveMessage.equals(CONST.N_READY_FILE_TRANSFER)) {
                        //파일전송 시퀀스
                        dos.writeUTF(serverActivity?.fileName)


                        var file = File(serverActivity?.resultPath + "/" + serverActivity?.fileName)
                        Log.d("FileSize", file.length().toString())
                        var fis = FileInputStream(file)
                        var bis = BufferedInputStream(fis)

                        var len: Int
                        var lenSum = 0
                        var size = 1024
                        var data = ByteArray(size)
                        while (true) {
                            len = bis.read(data)
                            lenSum = lenSum + len
                            Log.d("###", len.toString())
                            if (len == -1) {
                                break
                            }
                            dos.write(data, 0, len)

                        }
                        dos.flush()
                        dos.close()
                        diSocket?.close()
                        Log.d("###", "파일전송 완료")
                        serverActivity?.filetransferOver()
                        di.socket = serverActivity?.serverSocket?.accept()
                    } else {
                        return null
                    }
                }



            }



            CONST.N_PLAY_VIDEO->{
                for(di : DeviceInfo in serverActivity!!.clientDeviceInfoList) {
                    var diSocket = di.socket
                    var dos = DataOutputStream(diSocket?.getOutputStream())
                    dos.writeUTF(CONST.N_PLAY_VIDEO)
                    serverActivity?.playVideo()
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