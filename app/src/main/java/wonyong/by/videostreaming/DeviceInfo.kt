package wonyong.by.videostreaming

import java.io.Serializable
import java.net.InetAddress

data class DeviceInfo(var heightPixel : Int,
                      var widthPixel : Int,
                      var widthMM : Float,
                      var heightMM : Float,
                      var deviceOrder : Int,
                      var inetAddress: String?) :Serializable{
    fun getVideoSize(serverDevice : DeviceInfo, clientDevice1 : DeviceInfo, clientDevice2 : DeviceInfo){

    }
}