package vendor.car.tclient

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.database.getLongOrNull
import vendor.car.server.props.CarPropsClient
import vendor.car.server.props.CarPropsKeys

class MainActivity : AppCompatActivity() {
    private val client by lazy { CarPropsClient() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val vText: TextView = findViewById(R.id.vText)
        client.get(applicationContext, CarPropsKeys.ECHO)
        //
        vText.setOnClickListener {
            val values = ContentValues()
            values.put(CarPropsKeys.KEY_VALUE, 1)
            client.set(context = applicationContext, CarPropsKeys.HAVC_SPEED, values)
        }
        client.observe(applicationContext, CarPropsKeys.HAVC_SPEED) { selfChange, uri ->
            Log.d(TAG, "selfChang=$selfChange, uri=${uri?.path}")
            client.get(context = applicationContext, CarPropsKeys.HAVC_SPEED) {
                if (it?.moveToFirst() != true) return@get
                val index = it.getColumnIndexOrThrow(CarPropsKeys.KEY_VALUE)
                val value = it.getLongOrNull(index)
                Log.d(TAG, "get value of `${CarPropsKeys.HAVC_SPEED}` is $value")
            }
        }
    }

    override fun onDestroy() {
        client.removeAllObserver(applicationContext)
        super.onDestroy()
    }

    companion object {
        private const val TAG = "CarProps.Mock"
    }
}