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
        var dataSocket = serverActivityData?.dataSocket

        var playerDataRef = WeakReference(playerActivity)
        var playerActivityData = playerDataRef.get()
        var playerSocketList = playerActivityData?.socketList
        Log.d("###", "syncOn"+mode)
        when(mode){
            CONST.L_ON_CONNECT->{
                Log.d("###", "inner")
                serverActivityData?.serverSocket = ServerSocket(CONST.NETWORK_MESSAGE_PORT)
                Log.d("###", "Socket Connect")
                serverActivityData?.dataServerSocket = ServerSocket(8585)
                Log.d("###", "dataSocket Connect")
                serverActivityData?.serverSocket?.reuseAddress = true
                for(i in 0..1) {
                    dataSocket = serverActivityData?.dataServerSocket?.accept()
                    socket = serverActivityData?.serverSocket?.accept()
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
                        socket!!,
                        dataSocket!!
                    )
                }

                Log.d("###size", serverActivityData?.clientDeviceInfoList?.size.toString())

                if(serverActivityData?.clientDeviceInfoList?.size == 1)
                    return null
                else {
                    serverActivityData?.calcPixel()
                    var i = 1
                    for (di: DeviceInfo in serverActivityData!!.clientDeviceInfoList) {
                        var infoSocket = di.socket
                        var dos = DataOutputStream(infoSocket?.getOutputStream())
                        if (i == 1) {
                            dos.writeUTF(serverActivityData?.totalWidthMM.toString() + CONST.DELIMETER + i.toString() + CONST.DELIMETER + 0.toString())
                            i++
                        } else {
                            var aX = serverActivityData?.clientDeviceInfoList[0].widthMM
                            dos.writeUTF(serverActivityData?.totalWidthMM.toString() + CONST.DELIMETER + i.toString() + CONST.DELIMETER + aX.toString())
                        }
                    }
                }
            }
            CONST.N_FILE_STREAMING_START->{
                Log.d("###order", serverActivityData?.streamingOrder.toString())
                var di = serverActivityData!!.clientDeviceInfoList[serverActivityData?.streamingOrder]
                var diSocket = di.socket
                var dos = DataOutputStream(diSocket?.getOutputStream())
                var diFileSocket = di.dataSocket
                var fileDos = DataOutputStream(diFileSocket?.getOutputStream())
                dos.writeUTF(CONST.N_FILE_STREAMING_START)
                dos.writeUTF(serverActivityData?.fileName)
                var file = File(serverActivityData?.resultPath + "/" + serverActivityData?.fileName)
                Log.d("FileSize", file.length().toString())
                dos.writeUTF(file.length().toString()+CONST.DELIMETER+serverActivityData?.moovSize.toString())
                var fis = FileInputStream(file)
                var bis = BufferedInputStream(fis)

                var len: Int
                var lenSum = 0
                var size = 4096
                var data = ByteArray(size)
                while (true) {
                    len = bis.read(data)
                    lenSum = lenSum + len
                    if (len == -1) {
                        break
                    }
                    fileDos.write(data, 0, len)

                }
                fileDos.flush()
                fileDos.close()
                Log.d("###", "파일전송 완료")
                //serverActivityData?.filetransferOver()
            }


            CONST.N_PLAY_VIDEO->{
                for(di : DeviceInfo in serverActivityData!!.clientDeviceInfoList) {
                    var diSocket = di.socket
                    var dos = DataOutputStream(diSocket?.getOutputStream())
                    dos.writeUTF(CONST.N_PLAY_VIDEO)
                }
                serverActivityData?.playVideo()
            }
            CONST.L_PLAYER_ON_CONNECT->{
                playerActivityData?.playerServerSocket = ServerSocket(CONST.NETWORK_PLAYER_PORT)
                playerActivityData?.playerBufferSocket = ServerSocket(9090)
                playerActivityData?.playerServerSocket?.reuseAddress = true
                //여기서 전송시간 계산해서 받아주기
                for(i in 0..1){
                    var tempSocket : Socket? = null
                    var tempBufferSocket : Socket? = null
                    tempSocket = playerActivityData?.playerServerSocket?.accept()
                    tempBufferSocket = playerActivityData?.playerBufferSocket?.accept()
                    playerActivityData?.socketList?.add(tempSocket!!)
                    playerActivityData?.bufferSocketList?.add(tempBufferSocket!!)
                    var beforeTime = System.currentTimeMillis()
                    var dos = DataOutputStream(tempSocket?.getOutputStream())
                    dos.writeUTF("PLAYER_ON_CONNECT")

                    var dis = DataInputStream(tempSocket?.getInputStream())
                    var receiveMessage = dis.readUTF()
                    var afterTime = System.currentTimeMillis()
                    var timeRate = afterTime - beforeTime
                    dos.writeUTF(timeRate.toString())
                    Log.d("###", timeRate.toString())
                }
                playerActivityData?.serverOnWait1()
                playerActivityData?.serverOnWait2()
            }
            CONST.N_PLAYER_PLAY->{
                Log.d("###", "playIn")
                for(sock : Socket in playerSocketList!!){
                    Log.d("###", "before")
                    var dos = DataOutputStream(sock.getOutputStream())
                    dos.writeUTF(CONST.N_PLAYER_PLAY)
                    Log.d("###", "after")
                }
                playerActivityData?.isForwarding = false
                playerActivityData?.playVideo()
            }
            CONST.N_PLAYER_PAUSE->{
                for(sock : Socket in playerSocketList!!){
                    var dos = DataOutputStream(sock.getOutputStream())
                    dos.writeUTF(CONST.N_PLAYER_PAUSE)
                }
                playerActivityData?.pauseVideo()
            }
            CONST.N_BUFFER_OVER->{
                for(sock : Socket in playerSocketList!!){
                    var dos = DataOutputStream(sock.getOutputStream())
                    dos.writeUTF(CONST.N_BUFFER_OVER)
                    dos.writeUTF(playerActivityData?.bufferPosition.toString())
                }
                playerActivityData?.callAsyncTask(CONST.N_PLAYER_PLAY)
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
                    var dis = DataInputStream(sock.getInputStream())
                    var receiveMessage = dis.readUTF()
                }
                playerActivityData?.forward(playerActivityData?.nowPosition)
                if(playerActivityData!!.isPlaying){
                    playerActivityData?.callAsyncTask(CONST.N_PLAYER_PLAY)
                }
                playerActivityData?.isForwarding = false
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