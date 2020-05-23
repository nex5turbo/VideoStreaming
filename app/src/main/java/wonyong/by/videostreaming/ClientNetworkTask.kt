package wonyong.by.videostreaming

import android.content.Context
import android.net.wifi.p2p.WifiP2pDevice
import android.os.AsyncTask
import android.util.Log
import java.io.*
import java.lang.ref.WeakReference
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

class ClientNetworkTask(var mode:String, var activity : ClientActivity) : AsyncTask<Void, Void, Void>() {

    var CONST = Consts()

    override fun doInBackground(vararg p0: Void?): Void? {
        var dataRef = WeakReference(activity)
        var clientActivity = dataRef.get()
        var socket = clientActivity?.socket

        when(mode){
            CONST.L_WAITING_RECEIVE->{

                var inputStream = socket?.getInputStream()
                while(inputStream == null){
                    inputStream = socket?.getInputStream()
                    Log.d("##accept", "dd")
                }
                var dis = DataInputStream(inputStream)
                var receiveMessage = dis.readUTF()

                Log.d("##receive", receiveMessage)
                if (receiveMessage.equals(CONST.N_PLAY_VIDEO)) {
                    clientActivity?.playVideo()
                    clientActivity?.onWait()
                }else if(receiveMessage.equals(CONST.N_REQUEST_READY_FILE_TRANSFER)){
                    var dos = DataOutputStream(socket?.getOutputStream())
                    dos.writeUTF(CONST.N_READY_FILE_TRANSFER)
                    receiveMessage = dis.readUTF()
                    var file = File(clientActivity?.storage + "/" + receiveMessage)
                    var fos = FileOutputStream(file)
                    var bos = BufferedOutputStream(fos)

                    var len : Int
                    var size = 1024
                    var data = ByteArray(size)
                    data@while(true){
                        len = dis.read(data)
                        if(len ==-1){
                            break@data
                        }
                        Log.d("###", "inner")
                        bos.write(data, 0, len)
                    }
                    Log.d("###", "File transfer over")
                    clientActivity?.onWait()

                }

            }
            CONST.N_ON_CONNECT->{

                socket = Socket(clientActivity?.hostAddress, CONST.NETWORK_MESSAGE_PORT)
                clientActivity?.socket = socket
                clientActivity?.localAddress ="for test"

                Log.d("###", clientActivity?.localAddress)
                Log.d("###", socket?.inetAddress.toString())
                Log.d("###", socket?.localAddress.toString())
                clientActivity?.deviceInfo?.socket = socket

                var dos = DataOutputStream(socket?.getOutputStream())
                dos.writeUTF("0"+CONST.DELIMETER+"0"+CONST.DELIMETER+clientActivity?.deviceInfo?.widthMM+CONST.DELIMETER+clientActivity?.deviceInfo?.heightMM+CONST.DELIMETER+"0"+CONST.DELIMETER+socket?.localAddress)

//                var outputStream = socket.getOutputStream()
//                var dos = DataOutputStream(outputStream)
//
//                dos.writeUTF(CONST.N_ON_CONNECT+CONST.DELIMETER+socket.localAddress)
//                var inputStream = socket.getInputStream()
//                var dis = DataInputStream(inputStream)
//
//                var receiveMessage = dis.readUTF()f
                clientActivity?.onWait()


            }
        }
        return null
    }

}
/*
 네트워크 시퀀스 -> 소켓이 열리면 클라에서 먼저 기기정보 전송을함
 그리고 readinput 대기.
 소켓이 열리면 서버는 readinput대기하다가 기기정보가 전송되면
 기기정보 받아두고, 비디오를 선택함.(이 때 asynctask는 끝난 상태)
 비디오 파일 전송 시작메세지를 보냄.(asynctask 다시 execute해서 보냄)(더불어 소켓 accept 대기 근데 datagram도 소켓이 필요하던가?)(필요없다면 보류)
 그리고 readinput대기.(receive waiting 신호로 asyncTask execute해서 대기)
 클라에서는 전송 시작메세지 받고 asynctask가 종료 야호~ 일단 한가닥 돌렸다


 1.기기정보 전송

 */
//        var socket = Socket(hostAddress, 8888)
//        var outputStream = socket.getOutputStream()
//        var dos = DataOutputStream(outputStream)
//        dos.writeUTF("MESSAGE_RECEIVED")
//        return null

