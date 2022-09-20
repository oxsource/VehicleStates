package vendor.car.server.props.impl

import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import vendor.car.server.props.arch.CarPropsModule
import vendor.car.server.props.CarPropsKeys

class HAVCModule : CarPropsModule {
    private val values: MutableMap<String, Any?> = mutableMapOf()
    private val matcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(CarPropsKeys.AUTHORITIES, CarPropsKeys.HAVC_P1, 1)
        addURI(CarPropsKeys.AUTHORITIES, CarPropsKeys.HAVC_P2, 2)
    }

    override fun name(): String = "HAVC"

    override fun match(uri: Uri?): Boolean = matcher.match(uri) > 0

    override fun get(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val path = uri.path ?: return null
        val value = values[path] ?: return null
        val cursor = MatrixCursor(arrayOf(CarPropsKeys.KEY_VALUE))
        cursor.newRow().add(CarPropsKeys.KEY_VALUE, value)
        return cursor
    }

    override fun set(
        uri: Uri,
        value: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        val path = uri.path ?: return 0
        values[path] = value?.get(CarPropsKeys.KEY_VALUE)
        return 1
    }

    override fun dump(): String {
        val content = values.map { "${it.key}: ${it.value}" }.joinToString(separator = "\n")
        return "**${name()}:\n$content"
    }
}