package utdi.anisadwinugraheni.accelerometer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.vectordrawable.graphics.drawable.PathInterpolatorCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {

    lateinit var sensorManager: SensorManager
    // Create a constant to convert nanoseconds to seconds.
    private val NS2S = 1.0f / 1000000000.0f
    private val deltaRotationVector = FloatArray(4) { 0f }
    private var timestamp: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        accelerometer_data.text = "Sensor changed"
    }

    override fun onSensorChanged(event: SensorEvent?) {
        accelerometer_data.text = "x= ${event!!.values[0]}\n\n" +
                "Y=${event!!.values[1]}\n\n" +
                "Z=${event!!.values[2]}"
        // This timestep's delta rotation to be multiplied by the current rotation
        // after computing it from the gyro sample data.
        if (timestamp != 0f && event != null) {
            val dT = (event.timestamp - timestamp) * NS2S
            // Axis of the rotation sample, not normalized yet.
            var axisX: Float = event.values[0]
            var axisY: Float = event.values[1]
            var axisZ: Float = event.values[2]

            // Calculate the angular speed of the sample
            val omegaMagnitude: Float = sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ)

            // Normalize the rotation vector if it's big enough to get the axis
            // (that is, EPSILON should represent your maximum allowable margin of error)
            if (omegaMagnitude > PathInterpolatorCompat.EPSILON) {
                axisX /= omegaMagnitude
                axisY /= omegaMagnitude
                axisZ /= omegaMagnitude
            }
            // Integrate around this axis with the angular speed by the timestep
            // in order to get a delta rotation from this sample over the timestep
            // We will convert this axis-angle representation of the delta rotation
            // into a quaternion before turning it into the rotation matrix.
            val thetaOverTwo: Float = omegaMagnitude * dT / 2.0f
            val sinThetaOverTwo: Float = sin(thetaOverTwo)
            val cosThetaOverTwo: Float = cos(thetaOverTwo)
            deltaRotationVector[0] = sinThetaOverTwo * axisX
            deltaRotationVector[1] = sinThetaOverTwo * axisY
            deltaRotationVector[2] = sinThetaOverTwo * axisZ
            deltaRotationVector[3] = cosThetaOverTwo
        }
        timestamp = event?.timestamp?.toFloat() ?: 0f
        val deltaRotationMatrix = FloatArray(9) { 0f }
        SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
        // User code should concatenate the delta rotation we computed with the current rotation
        // in order to get the updated rotation.
        // rotationCurrent = rotationCurrent * deltaRotationMatrix;

        gyroscope_data.text="x= ${deltaRotationVector[0]}\n\n"+
                "Y=${deltaRotationVector[1]}\n\n"+
                "Z=${deltaRotationVector[2]}"

    }
}
