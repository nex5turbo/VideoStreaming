package wonyong.by.videostreaming

import android.os.AsyncTask
import android.util.Log
import java.io.*
import java.lang.Thread.sleep
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
                    var widthPixel = st.nextToken()
                    Log.d("hp", widthPixel)
                    var heightPixel = st.nextToken()
                    Log.d("hp", heightPixel)
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
                serverActivityData?.calcPixel()
                var i = 1
                for(di:DeviceInfo in serverActivityData!!.clientDeviceInfoList){
                    var infoSocket = di.socket
                    var dos = DataOutputStream(infoSocket?.getOutputStream())
                    dos.writeUTF(serverActivityData?.totalWidthPixel.toString()+CONST.DELIMETER+i.toString())
                    i++
                }
            }

            CONST.N_REQUEST_READY_FILE_TRANSFER->{
                for(di:DeviceInfo in serverActivityData!!.clientDeviceInfoList) {
                    var diSocket = di.socket
                    var dos = DataOutputStream(diSocket?.getOutputStream())
                    dos.writeUTF(CONST.N_REQUEST_READY_FILE_TRANSFER)
                    dos.writeUTF(serverActivityData?.fileName)

                    var dis = DataInputStream(diSocket?.getInputStream())
                    var receiveMessage = dis.readUTF()

                    if(receiveMessage.equals(CONST.N_FILE_EXIST)){
                     continue
                    }else if (receiveMessage.equals(CONST.N_READY_FILE_TRANSFER)) {
                        //파일전송 시퀀스

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

                    } else {
                        return null
                    }
                    dos.flush()
                    dos.close()
                    diSocket?.close()
                    Log.d("###", "파일전송 완료")
                    serverActivityData?.filetransferOver()
                    di.socket = serverActivityData?.serverSocket?.accept()
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
                serverActivityData?.serverSocket = null
                serverActivityData?.socket = null
                serverActivityData?.clientDeviceInfoList?.clear()
            }
            CONST.L_PLAYER_ON_CONNECT->{
                playerActivityData?.playerServerSocket = ServerSocket(CONST.NETWORK_PLAYER_PORT)
                playerActivityData?.playerServerSocket?.reuseAddress = true
                //여기서 전송시간 계산해서 받아주기
                for(i in 0..1){
                    var tempSocket : Socket? = null
                    tempSocket = playerActivityData?.playerServerSocket?.accept()
                    playerActivityData?.socketList?.add(tempSocket!!)
                    var beforeTime = System.currentTimeMillis()
                    var dos = DataOutputStream(tempSocket?.getOutputStream())
                    dos.writeUTF("PLAYER_ON_CONNECT")

                    var dis = DataInputStream(tempSocket?.getInputStream())
                    var receiveMessage = dis.readUTF()
                    var afterTime = System.currentTimeMillis()
                    var timeRate = afterTime - beforeTime
                    dos.writeUTF(timeRate.toString())
                    Log.d("###", timeRate.toString())
                    playerActivityData?.timeRateArray?.add(timeRate)
                }
            }
            CONST.N_PLAYER_PLAY->{
                for(sock : Socket in playerSocketList!!){
                    var dos = DataOutputStream(sock.getOutputStream())
                    dos.writeUTF(CONST.N_PLAYER_PLAY)
                }
                playerActivityData?.playVideo()
            }
            CONST.N_PLAYER_PAUSE->{
                for(sock : Socket in playerSocketList!!){
                    var dos = DataOutputStream(sock.getOutputStream())
                    dos.writeUTF(CONST.N_PLAYER_PAUSE)
                }
                playerActivityData?.pauseVideo()
            }
            CONST.N_PLAYER_BACKWARD->{
                for(sock : Socket in playerSocketList!!){
                    var dos = DataOutputStream(sock.getOutputStream())
                    dos.writeUTF(CONST.N_PLAYER_BACKWARD)
                    dos.writeUTF(playerActivityData?.nowPosition.toString())
                }
                playerActivityData?.backward(playerActivityData?.nowPosition)
            }
            CONST.N_PLAYER_FORWARD->{
                for(sock : Socket in playerSocketList!!){
                    var dos = DataOutputStream(sock.getOutputStream())
                    dos.writeUTF(CONST.N_PLAYER_FORWARD)
                    dos.writeUTF(playerActivityData?.nowPosition.toString())
                }
                playerActivityData?.forward(playerActivityData?.nowPosition)
            }
            CONST.N_PLAYER_EXIT->{
                for(sock : Socket in playerSocketList!!){
                    var dos = DataOutputStream(sock.getOutputStream())
                    dos.writeUTF(CONST.N_PLAYER_EXIT)
                    sock.close()
                }
                playerActivityData?.playerServerSocket?.close()
            }
        }
        return null
    }
}