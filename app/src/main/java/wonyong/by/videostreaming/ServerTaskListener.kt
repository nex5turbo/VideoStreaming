package wonyong.by.videostreaming

interface ServerTaskListener {

    fun addClientDeviceInfo(heightPixel:Int, widthPixel:Int, widthMM:Float,  heightMM:Float, deviceOrder:Int, inetAddress:String)
}