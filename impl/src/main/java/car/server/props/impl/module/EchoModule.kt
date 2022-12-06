package car.server.props.impl.module

import android.car.Car
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.util.Log
import vendor.car.server.props.CarPropsKeys

class EchoModule : car.server.props.impl.arch.CarPropsModule {
    private val matcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(CarPropsKeys.AUTHORITIES, CarPropsKeys.ECHO, 1)
    }

    override fun name(): String = "CarProp.Echo"

    override fun onConnected(car: Car) = Unit

    override fun onDisconnected() = Unit

    override fun match(uri: Uri?): Boolean = matcher.match(uri) > 0

    override fun get(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        Log.d(name(), "echo called.")
        return null
    }

    override fun set(
        uri: Uri,
        value: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0

    override fun dump(): String = ""
}