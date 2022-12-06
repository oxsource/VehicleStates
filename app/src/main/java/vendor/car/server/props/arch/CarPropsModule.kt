package vendor.car.server.props.arch

import android.car.Car
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

interface CarPropsModule {
    companion object {
        const val PATH_SPLIT = "/"
    }

    fun name(): String

    fun onConnected(car: Car)

    fun onDisconnected()

    fun match(uri: Uri?): Boolean

    fun match(uri: Uri?, key: String): Boolean {
        val value = uri?.path?.split(PATH_SPLIT)?.lastOrNull()
        return key.isNotEmpty() && value == key
    }

    fun get(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor?

    fun set(
        uri: Uri,
        value: ContentValues?, selection: String?,
        selectionArgs: Array<out String>?
    ): Int

    fun dump(): String

    interface Callback {
        fun notifyChange(path: String)
    }
}