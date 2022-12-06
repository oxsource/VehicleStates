package vendor.car.server.props

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log

class CarPropsClient {
    private val observers: MutableMap<String, ContentObserver> = mutableMapOf()
    private val handler = Handler(Looper.getMainLooper())

    fun set(context: Context, path: String, values: ContentValues): Result<Int> =
        kotlin.runCatching {
            val uri = compose(path)
            return@runCatching context.contentResolver.update(uri, values, null, null)
        }.onFailure {
            Log.e(TAG, "set exception!! ${it.message}")
        }

    fun get(context: Context, path: String, block: (Cursor?) -> Unit): Result<Unit> =
        kotlin.runCatching {
            val uri = compose(path)
            context.contentResolver.query(uri, null, null, null, null).use(block)
            return@runCatching
        }.onFailure {
            Log.e(TAG, "get exception!! ${it.message}")
        }

    fun observe(context: Context, path: String, block: (selfChange: Boolean, uri: Uri?) -> Unit) {
        kotlin.runCatching {
            if (observers[path] != null) {
                Log.w(TAG, "observer for $path existed.")
                return@runCatching
            }
            val uri = compose(path)
            val observer = object : ContentObserver(handler) {
                override fun onChange(selfChange: Boolean, uri: Uri?) {
                    block(selfChange, uri)
                }
            }
            context.contentResolver.registerContentObserver(uri, true, observer)
            observers[path] = observer
            return@runCatching
        }
    }

    fun removeObserver(context: Context, path: String) {
        kotlin.runCatching {
            val observer = observers[path] ?: return@runCatching
            context.contentResolver.unregisterContentObserver(observer)
            observers.remove(path)
        }
    }

    fun removeAllObserver(context: Context) {
        kotlin.runCatching {
            observers.values.forEach(context.contentResolver::unregisterContentObserver)
            observers.clear()
        }
    }

    companion object {
        private const val TAG = "CarProps.Client"
        fun compose(path: String): Uri {
            return Uri.parse("${ContentResolver.SCHEME_CONTENT}://${CarPropsKeys.AUTHORITIES}/$path")
        }
    }
}