package wonyong.by.videostreaming

import java.net.Socket

interface ServerTaskListener {

    fun addClientDeviceInfo(heightPixel:Int, widthPixel:Int, widthMM:Float,  heightMM:Float, deviceOrder:Int, sock : Socket)
    fun playVideo()
    fun filetransferOver()
    fun playEnable()
    fun sendEnable()
    fun calcPixel()
}