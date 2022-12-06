package car.server.props.impl.arch

import android.car.Car
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.util.Log
import car.server.props.impl.module.EchoModule
import car.server.props.impl.module.HAVCModule
import vendor.car.server.props.CarPropsClient
import java.io.FileDescriptor
import java.io.PrintWriter

class CarPropsProvider : ContentProvider(), CarPropsModule.Callback {
    companion object {
        private const val TAG = "CarProps.Provider"
        private val DEBUG = Log.isLoggable(TAG, Log.DEBUG)
    }

    private val modules: MutableList<CarPropsModule> = mutableListOf()

    override fun onCreate(): Boolean {
        modules.add(EchoModule())
        modules.add(HAVCModule(callback = this))
        Car.createCar(context, null, Car.CAR_WAIT_TIMEOUT_WAIT_FOREVER) { car, success ->
            Log.d(TAG, "onLifecycleChanged: $car, $success")
            if (null != car) {
                modules.forEach { it.onConnected(car) }
            } else {
                modules.forEach { it.onDisconnected() }
            }
        }
        return true
    }

    @Throws(Exception::class)
    private fun module(uri: Uri, readonly: Boolean): CarPropsModule {
        CarPropsSecurity.check(provider = this, uri, readonly)
        val value = modules.find { it.match(uri = uri) }
        return value ?: throw Exception("no matching module for uri `$uri`")
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return kotlin.runCatching {
            val who = module(uri = uri, readonly = true)
            if (DEBUG) Log.d(TAG, "query uri `$uri`")
            return@runCatching who.get(uri, projection, selection, selectionArgs, sortOrder)
        }.onFailure {
            Log.e(TAG, "query exception!! ${it.message}")
        }.getOrNull()
    }

    override fun getType(uri: Uri): String = ""

    override fun insert(uri: Uri, value: ContentValues?): Uri? {
        Log.w(TAG, "not support insert for `$uri`")
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        Log.w(TAG, "not support delete for `$uri`")
        return 0
    }

    override fun update(
        uri: Uri,
        value: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = kotlin.runCatching {
        val who = module(uri = uri, readonly = false)
        if (DEBUG) Log.d(TAG, "update uri `$uri`")
        return@runCatching who.set(uri, value, selection, selectionArgs)
    }.onFailure {
        Log.e(TAG, "update exception!! ${it.message}")
        it.printStackTrace()
    }.getOrNull() ?: 0

    override fun dump(fd: FileDescriptor?, writer: PrintWriter?, args: Array<out String>?) {
        val s = args?.firstOrNull()?.toString().orEmpty()
        val modules: List<CarPropsModule> = if (s.isEmpty()) modules else modules.filter { e ->
            e.name().contentEquals(s, ignoreCase = true)
        }
        writer?.println("---$TAG---")
        modules.forEach { writer?.println(it.dump()) }
    }

    override fun notifyChange(path: String) {
        val uri = CarPropsClient.compose(path)
        context?.contentResolver?.notifyChange(uri, null, 0)
    }
}