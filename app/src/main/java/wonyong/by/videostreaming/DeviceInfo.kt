package wonyong.by.videostreaming

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable
import java.net.InetAddress
import java.net.Socket

data class DeviceInfo(var heightPixel : Int,
                      var widthPixel : Int,
                      var widthMM : Float,
                      var heightMM : Float,
                      var deviceOrder : Int,
                      var socket : Socket?) : Serializable {

    fun getVideoSize(serverDevice : DeviceInfo, clientDevice1 : DeviceInfo, clientDevice2 : DeviceInfo){

    }
}