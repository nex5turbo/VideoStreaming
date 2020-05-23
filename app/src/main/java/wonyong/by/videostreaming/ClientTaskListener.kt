package wonyong.by.videostreaming

import java.net.InetAddress

interface ClientTaskListener {
    fun onWait()
    fun showAddr(addr:InetAddress)
    fun playVideo()
    fun filetransferOver()
}