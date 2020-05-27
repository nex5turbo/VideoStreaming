package wonyong.by.videostreaming

interface PlayerListener {
    fun playVideo()
    fun pauseVideo()
    fun onWait()
    fun forward(position : Int)
    fun backward(position : Int)
}