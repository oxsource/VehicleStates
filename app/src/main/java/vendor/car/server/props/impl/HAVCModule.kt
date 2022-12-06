package vendor.car.server.props.impl

import android.car.Car
import android.car.hardware.CarPropertyValue
import android.car.hardware.hvac.CarHvacManager
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.util.Log
import vendor.car.server.props.CarPropsKeys
import vendor.car.server.props.arch.CarPropsModule

class HAVCModule(private val callback: CarPropsModule.Callback) : CarPropsModule {
    private val caches: MutableMap<String, Any?> = mutableMapOf()
    private val matcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(CarPropsKeys.AUTHORITIES, CarPropsKeys.HAVC_SPEED, 1)
        addURI(CarPropsKeys.AUTHORITIES, CarPropsKeys.HAVC_TEMP, 2)
    }
    private val props: List<Int> = listOf(
        CarHvacManager.ID_ZONED_FAN_SPEED_SETPOINT,
        CarHvacManager.ID_ZONED_TEMP_SETPOINT
    )
    private var hvac: CarHvacManager? = null

    override fun name(): String = "CarProp.HAVC"

    override fun onConnected(car: Car) {
        if (null != hvac) return
        val value = car.getCarManager(Car.HVAC_SERVICE) as? CarHvacManager ?: return
        value.registerHvacListener(carCallback, props, 0)
        hvac = value
    }

    override fun onDisconnected() {
        val value = hvac ?: return
        hvac = null
        value.unregisterHvacListener(carCallback, props)
    }

    override fun match(uri: Uri?): Boolean = matcher.match(uri) > 0

    override fun get(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val manager = hvac ?: return null
        val value = when {
            match(uri, CarPropsKeys.HAVC_SPEED) -> {
                getValue(CarPropsKeys.HAVC_SPEED) { manager.getHvacFanSpeed(0) }
            }
            match(uri, CarPropsKeys.HAVC_TEMP) -> {
                getValue(CarPropsKeys.HAVC_TEMP) { manager.getHvacTemperTure(0) }
            }
            else -> null
        }
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
        val manager = hvac ?: return 0
        val e = value?.get(CarPropsKeys.KEY_VALUE) ?: return 0
        when {
            match(uri, CarPropsKeys.HAVC_SPEED) -> {
                val speed = e as? Int ?: return 0
                manager.setHvacFanSpeed(speed, 0)
            }
            match(uri, CarPropsKeys.HAVC_TEMP) -> {
                val temp = e as? Float ?: return 0
                manager.setHvacTemperTure(temp, 0)
            }
            else -> return 0
        }
        return 1
    }

    override fun dump(): String {
        val content = caches.map { "${it.key}: ${it.value}" }.joinToString(separator = "\n")
        return "**${name()}:\n$content"
    }

    private fun getValue(key: String, block: () -> Any?): Any? {
        val e = block() ?: return caches[key]
        caches[key] = e
        return e
    }

    private val carCallback: CarHvacManager.CarHvacEventCallback =
        object : CarHvacManager.CarHvacEventCallback {
            override fun onChangeEvent(value: CarPropertyValue<*>?) {
                val propId = value?.propertyId ?: return
                when (propId) {
                    CarHvacManager.ID_ZONED_FAN_SPEED_SETPOINT -> {
                        val speed = (value.value as? Int) ?: return
                        caches[CarPropsKeys.HAVC_SPEED] = speed
                        callback.notifyChange(CarPropsKeys.HAVC_SPEED)
                    }
                    CarHvacManager.ID_ZONED_TEMP_SETPOINT -> {
                        val speed = (value.value as? Int) ?: return
                        caches[CarPropsKeys.HAVC_SPEED] = speed
                        callback.notifyChange(CarPropsKeys.HAVC_SPEED)
                    }
                }
            }

            override fun onErrorEvent(propId: Int, zone: Int) {
                Log.e(name(), "onErrorEvent: $propId, $zone")
            }
        }
}