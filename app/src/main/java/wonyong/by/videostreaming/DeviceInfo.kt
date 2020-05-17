package wonyong.by.videostreaming

import java.io.Serializable

data class DeviceInfo(var deviceName : String, var heightPixel : Int, var widthPixel : Int, var widthMM : Float, var heightMM : Float, var deviceOrder : Int) :Serializable{
    fun getVideoSize(serverDevice : DeviceInfo, clientDevice1 : DeviceInfo, clientDevice2 : DeviceInfo){

    }
}