package marcinlowercase.a.core.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class ShakeDetector(context: Context, private val onShake: () -> Unit) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // Threshold: acceleration > 2.7g (Earth gravity is 1.0g)
    // Adjust this: Lower = more sensitive, Higher = harder shake required
    private val shakeThresholdGravity = 2.7F
    private val shakeSlopTimeMs = 500 // Minimum time between shakes
    private var shakeTimestamp: Long = 0

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val gX = x / SensorManager.GRAVITY_EARTH
            val gY = y / SensorManager.GRAVITY_EARTH
            val gZ = z / SensorManager.GRAVITY_EARTH

            // Calculate gForce (vector magnitude)
            val gForce = sqrt(gX * gX + gY * gY + gZ * gZ)

            if (gForce > shakeThresholdGravity) {
                val now = System.currentTimeMillis()
                // Ignore shakes that happen too close together
                if (shakeTimestamp + shakeSlopTimeMs > now) {
                    return
                }

                shakeTimestamp = now
                onShake()
            }
        }
    }
}
