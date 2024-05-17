package com.example.buddybeat

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.LinkedList
import kotlin.math.ceil
import kotlin.math.sqrt


class SensorService : Service(), SensorEventListener {
    // Override callback methods here

    private val scope = CoroutineScope(Dispatchers.Default)

    private lateinit var sensorManager: SensorManager
    private var gyroSensor: Sensor? = null
    private var accelSensor: Sensor? = null


    private var lastUpdate: Long = 0
    private var lastAcceleration: Float = 0f
    private var acceleration: Float = 0f
    private var currentAcceleration: Float = SensorManager.GRAVITY_EARTH
    private var steps: Int = 0
    private var lastStepTime: Long = 0
    private var stepFrequency: Float = 0f

    private val stepTimes = LinkedList<Long>()
    private var startTime: Long = 0

    private var gyroData: String = ""
    private var accelData: String = ""
    private var stepData: String = ""
    private var stepFreq: String = ""

    /* variabili da regolare */
    private var unitTime = 30000  //60000 millisecondi = 60 secondi
    private var threshold = 3  //per calcolo steps
    private var deltaTime = 300// 0.5s intervallo di aggiornamento dati

    companion object {
        const val CHANNEL_ID = "SensorsChannel"
    }



    // Create the NotificationChannel.
//    val name = "sensor_channel"
//    val descriptionText = "Notification channel used for sensors"
//    val importance = NotificationManager.IMPORTANCE_DEFAULT
//    val mChannel = NotificationChannel("my_channel_id", name, importance)
//    mChannel.description = descriptionText
//    // Register the channel with the system. You can't change the importance
//    // or other notification behaviors after this.
//    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//    notificationManager.createNotificationChannel(mChannel)


    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(UnstableApi::class) override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        startForegroundService()
        scope.launch {
            while (true) {
                Log.d("SensorService", "Step Count: $steps")
                Log.d("SensorService", "Step Cadence: $stepFrequency")
                updateNotification()
                delay(1000)
            }
        }
        return START_STICKY
    }
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL)

        val intent = Intent(this, SensorService::class.java)
        startService(intent)
        startTime = System.currentTimeMillis()
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @OptIn(UnstableApi::class) override fun onDestroy() {

        Log.d("SensorService", "Distruggo il service")
        scope.cancel()
        sensorManager.unregisterListener(this, gyroSensor)
        sensorManager.unregisterListener(this, accelSensor)
        super.onDestroy()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor == gyroSensor) {
                gyroData = "Gyroscope data: ${it.values[0]}, ${it.values[1]}, ${it.values[2]}"
            } else if (it.sensor == accelSensor) {
                val x = it.values[0]
                val y = it.values[1]
                val z = it.values[2]
                accelData = "Accelerometer data: ${it.values[0]}, ${it.values[1]}, ${it.values[2]}"

                lastAcceleration = currentAcceleration
                currentAcceleration = sqrt((x * x + y * y + z * z))
                val delta = currentAcceleration - lastAcceleration

                /* filtro passa basso rispetto a delta */
                acceleration = acceleration * 0.9f + delta
                val now = System.currentTimeMillis()

                if (acceleration > threshold) {
                    if ((now - lastUpdate) > deltaTime) {
                        lastUpdate = now
                        steps++
                        stepTimes.add(now)
                    }
                }

                stepData ="Steps: $steps"

                // Remove times older than one minute
                while ((stepTimes.firstOrNull() ?: Long.MAX_VALUE) < now - unitTime) {
                    stepTimes.removeFirstOrNull()
                }

                // Calculate step frequency
                if (now - startTime < unitTime) {  //60000 millesimi di secondo=1 min
                    stepFrequency = ceil((stepTimes.size.toFloat() / (now - startTime))*60000F)
                } else {
                    stepFrequency = ceil((stepTimes.size.toFloat()/(unitTime))*60000F)
                }

                lastStepTime = now
                stepFreq = "Frequency: $stepFrequency step/min"
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Sesnors Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startForegroundService() {

        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Sport Activity")
            .setContentText("Steps: $steps\nStep Frequency: $stepFrequency")
            .setSmallIcon(R.drawable.ic_play)
            .build()

        startForeground(1, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateNotification() {
        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Sport Activity")
            .setContentText("Steps: $steps\nStep Frequency: $stepFrequency")
            .setSmallIcon(R.drawable.ic_play)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(1, notification)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
    }

}