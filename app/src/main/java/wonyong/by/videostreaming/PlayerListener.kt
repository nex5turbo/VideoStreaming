package wonyong.by.videostreaming

interface PlayerListener {
    fun playVideo()
    fun pauseVideo()
    fun onWait()
    fun forward(position : Int)
    fun backward(position : Int)
    fun serverOnWait1()
    fun serverOnWait2()
    fun setAfterBuffered(position : Int)
    fun bufferOver(position:Int)
}