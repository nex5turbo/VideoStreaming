package wonyong.by.videostreaming

import android.content.Context
import android.widget.Toast

class ToastAct {
    fun toast(msg:String, context : Context){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
}