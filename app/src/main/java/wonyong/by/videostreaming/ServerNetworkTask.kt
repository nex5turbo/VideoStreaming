package wonyong.by.videostreaming

import android.os.AsyncTask
import android.util.Log
import java.io.*
import java.lang.ref.WeakReference
import java.net.ServerSocket
import java.net.Socket
import java.util.*

class ServerNetworkTask(var mode : String, var activity: ServerActivity?, var playerActivity: ServerPlayerActivity?) : AsyncTask<Void, Void, Void>() {
    var CONST = Consts()
    override fun doInBackground(vararg p0: Void?): Void? {

        var dataRef= WeakReference(activity)
        var serverActivityData = dataRef.get()
        var socket = serverActivityData?.socket

        var playerDataRef = WeakReference(playerActivity)
        var playerActivityData = playerDataRef.get()
        var playerSocketList = playerActivityData?.socketList

        when(mode){
            CONST.L_ON_CONNECT->{
                Log.d("###", "inner")
                serverActivityData?.serverSocket = ServerSocket(CONST.NETWORK_MESSAGE_PORT)
                serverActivityData?.serverSocket?.reuseAddress = true
                for(i in 0..1) {
                    socket = serverActivityData?.serverSocket?.accept()
                    serverActivityData?.socket = socket
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

                    serverActivityData?.addClientDeviceInfo(
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
                for(di:DeviceInfo in serverActivityData!!.clientDeviceInfoList) {
                    var diSocket = di.socket
                    var dos = DataOutputStream(diSocket?.getOutputStream())
                    dos.writeUTF(CONST.N_REQUEST_READY_FILE_TRANSFER)

                    var dis = DataInputStream(diSocket?.getInputStream())
                    var receiveMessage = dis.readUTF()
                    if (receiveMessage.equals(CONST.N_READY_FILE_TRANSFER)) {
                        //파일전송 시퀀스
                        dos.writeUTF(serverActivityData?.fileName)


                        var file = File(serverActivityData?.resultPath + "/" + serverActivityData?.fileName)
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
                        serverActivityData?.filetransferOver()
                        di.socket = serverActivityData?.serverSocket?.accept()
                    } else {
                        return null
                    }
                }
            }



            CONST.N_PLAY_VIDEO->{
                for(di : DeviceInfo in serverActivityData!!.clientDeviceInfoList) {
                    var diSocket = di.socket
                    var dos = DataOutputStream(diSocket?.getOutputStream())
                    dos.writeUTF(CONST.N_PLAY_VIDEO)
                }
                serverActivityData?.playVideo()
                serverActivityData?.serverSocket?.close()

            }
            CONST.L_PLAYER_ON_CONNECT->{
                playerActivityData?.playerServerSocket = ServerSocket(CONST.NETWORK_PLAYER_PORT)
                playerActivityData?.playerServerSocket?.reuseAddress = true
                for(i in 0..1){
                    Log.d("###", "Before Socket Connect")
                    var tempSocket : Socket? = null
                    Log.d("###", "Before Socket Connect2")
                    tempSocket = playerActivityData?.playerServerSocket?.accept()
                    Log.d("###", "Before Socket Connect3")
                    playerActivityData?.socketList?.add(tempSocket!!)
                    Log.d("###", "Before Socket Connect4")
                }
                playerActivityData?.playVideo()

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