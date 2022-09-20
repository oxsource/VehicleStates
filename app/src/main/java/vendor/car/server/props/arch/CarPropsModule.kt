package vendor.car.server.props.arch

import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

interface CarPropsModule{
    fun name(): String

    fun match(uri: Uri?): Boolean

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
}