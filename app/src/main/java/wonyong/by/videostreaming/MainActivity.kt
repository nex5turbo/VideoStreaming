package wonyong.by.videostreaming

import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pDevice
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*

//TODO https://github.com/nex5turbo/graduate.git

class MainActivity : AppCompatActivity() {
    lateinit var deviceInfo : DeviceInfo
    var toastAct : ToastAct = ToastAct()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getDeviceInfo()
        requestLocationPermission()
        requestPhoneStatePermission()
        buttonListener()
    }

    private fun requestPhoneStatePermission() {
        if(ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_PHONE_STATE)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.READ_PHONE_STATE),
                90)
        }

        if(ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                110)
        }
        if(ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                120)
        }
    }

    private fun buttonListener() {

        enterClientStreamingButton.setOnClickListener {
            val i : Intent = Intent(this, ClientActivity::class.java)
            i.putExtra("deviceInfo", deviceInfo)
            i.putExtra("DS", "streaming")
            startActivity(i)
        }

        enterServerStreamingButton.setOnClickListener {
            val i : Intent = Intent(this, ServerActivity::class.java)
            i.putExtra("deviceInfo", deviceInfo)
            i.putExtra("DS", "streaming")
            startActivity(i)
        }
    }

    private fun getDeviceInfo() {
        var thisDevice = WifiP2pDevice()
        var displayInfo = DisplayMetrics()


        windowManager.defaultDisplay.getMetrics(displayInfo)
        var widthPixel = displayInfo.widthPixels
        var heightPixel = displayInfo.heightPixels

        Log.d("###xdpi", displayInfo.xdpi.toString())
        Log.d("###xdpiint", displayInfo.xdpi.toInt().toString())
        Log.d("###ydpi", displayInfo.ydpi.toString())
        Log.d("###ydpiint", displayInfo.ydpi.toInt().toString())

        Log.d("###wpx", widthPixel.toString())
        Log.d("###hpx", heightPixel.toString())
        var widthMM = widthPixel / TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 1f, displayInfo)
        Log.d("###wMM", widthMM.toString())
        var heightMM = heightPixel / TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 1f, displayInfo)
        Log.d("###hMM", heightMM.toString())

        deviceInfo = DeviceInfo(widthPixel, heightPixel, widthMM, heightMM, 0, null, null)

        toastAct.toast(Build.ID, this)


    }

    fun requestLocationPermission(){
        if(ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),
                100)
        }
    }
}