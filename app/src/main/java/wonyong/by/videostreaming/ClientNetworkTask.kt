package wonyong.by.videostreaming

import android.net.wifi.p2p.WifiP2pDevice
import android.os.AsyncTask
import android.util.Log
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

class ClientNetworkTask(var mode:String, var localAddress : String, var hostAddress: InetAddress, var taskListener: ClientTaskListener, var di:DeviceInfo) : AsyncTask<Void, Void, Void>() {

    var CONST = Consts()

    override fun doInBackground(vararg p0: Void?): Void? {


        when(mode){
            CONST.L_WAITING_RECEIVE->{
                Log.d("##onwait", "onwait")

                    var serverSocket = ServerSocket(CONST.NETWORK_MESSAGE_PORT)
                    serverSocket.setReuseAddress(true)
                    Log.d("##serversocket", "onwait")
                    var socket = serverSocket.accept()
                    Log.d("##accept", "dd")

                    var inputStream = socket.getInputStream()
                    var dis = DataInputStream(inputStream)

                    var receiveMessage = dis.readUTF()
                    Log.d("##receive", receiveMessage)
                    if (receiveMessage.equals(CONST.N_PLAY_VIDEO)) {
                        taskListener.playVideo()
                    }


            }
            CONST.N_ON_CONNECT->{
                var socket = Socket(hostAddress, CONST.NETWORK_MESSAGE_PORT)
                Log.d("###", hostAddress.toString())
                Log.d("###", socket.inetAddress.toString())
                Log.d("###", socket.localAddress.toString())

                var dos = DataOutputStream(socket.getOutputStream())
                dos.writeUTF("0"+CONST.DELIMETER+"0"+CONST.DELIMETER+di.widthMM+CONST.DELIMETER+di.heightMM+CONST.DELIMETER+"0"+CONST.DELIMETER+socket.localAddress)

//                var outputStream = socket.getOutputStream()
//                var dos = DataOutputStream(outputStream)
//
//                dos.writeUTF(CONST.N_ON_CONNECT+CONST.DELIMETER+socket.localAddress)
//                var inputStream = socket.getInputStream()
//                var dis = DataInputStream(inputStream)
//
//                var receiveMessage = dis.readUTF()
                dos.close()
                socket.close()
                taskListener.onWait()


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

