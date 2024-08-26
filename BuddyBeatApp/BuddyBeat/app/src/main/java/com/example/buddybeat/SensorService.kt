package com.example.buddybeat

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.icu.text.SimpleDateFormat
import android.os.Binder
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

import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Date
import java.util.Locale


class SensorService : Service(), SensorEventListener {
    // Override callback methods here

    //private val scope = CoroutineScope(Dispatchers.Default)

    private lateinit var sensorManager: SensorManager
    private var gyroSensor: Sensor? = null
    private var accelSensor: Sensor? = null


    private var lastUpdate: Long = 0
    private var lastAcceleration: Float = 0f
    private var acceleration: Float = 0f
    private var currentAcceleration: Float = SensorManager.GRAVITY_EARTH
    private var steps: Int = 0
    private var lastStepTime: Long = 0
    private var alpha: Float = 0.5f
    private var inputFreq: Float = 0f
    private var outputFreq: Float = 0f
    private var oldStepFrequency: Int = 0

    private val stepTimes = LinkedList<Long>()
    private var startTime: Long = 0

    private var bpm_song = 0

    // Step frequency variables
    private var lastUpdateTime : Int = 0 //new
    private var penultimateUpdateTime: Int = 0 //new
    private var deltaBetweenTwoSteps: Int = 0 //new
    private var stepFreqNow : Int = 0 //new: starting value di frequenza passi
    private val previousStepFrequency = mutableListOf<Int>()
    private val currentFrequency = mutableListOf<Int>()
    private val frequency = mutableListOf<Int>()
    private var newStepFrequency: Int = 0


    /* variabili da regolare */
    private var unitTime = 60000  //60000 millisecondi = 60 secondi
    private var threshold = 2  //per calcolo steps
    private var deltaTime = 300// 0.5s intervallo di aggiornamento dati
    private val n = 15 //stabilizzazione valori frequenza

    companion object {
        const val CHANNEL_ID = "SensorsChannel"
    }


    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods.
        fun getService(): SensorService = this@SensorService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    fun updateBpm(bpm : Int){
        bpm_song = bpm
    }

    val stepFreq: Int
        get() = newStepFrequency

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

    private val DIRECTORY_NAME = "BuddyBeat Logs"
    data class ValueTimestamp(val timestamp: String, val oldSPM: String, val newSPM: String, val BPM : String)

    private val activityLogs = mutableListOf<ValueTimestamp>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        startForegroundService()
        return START_STICKY
    }

    private val handler = Handler(Looper.getMainLooper())
    private val interval: Long = 1000

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(UnstableApi::class) override fun onCreate() {
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
        handler.postDelayed(writeLogs, interval)
        /*scope.launch {
            while (true) {
                //Log.d("Service", this@SensorService.toString())
                //Log.d("SensorService", "Step Count: $steps")
                //Log.d("SensorService", "Step Cadence: $stepFrequency")



            }
        }*/
    }

    private val writeLogs = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun run() {
            val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            updateActivityLogs(oldStepFrequency.toString(), newStepFrequency.toString(), currentTime, bpm_song.toString())
            handler.postDelayed(this, interval)
            updateNotification()
        }
    }

    @OptIn(UnstableApi::class) override fun onDestroy() {

        Log.d("SensorService", "Distruggo il service")
        // writeToFile("BuddyBeat Logs", "Hello, this is a test!")
        writeToCsvFile(activityLogs)
        //scope.cancel()
        sensorManager.unregisterListener(this, gyroSensor)
        sensorManager.unregisterListener(this, accelSensor)
        super.onDestroy()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor == gyroSensor) {
                //gyroData = "Gyroscope data: ${it.values[0]}, ${it.values[1]}, ${it.values[2]}"
            } else if (it.sensor == accelSensor) {
                val x = it.values[0]
                val y = it.values[1]
                val z = it.values[2]
                //accelData = "Accelerometer data: ${it.values[0]}, ${it.values[1]}, ${it.values[2]}"

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

                if (acceleration > threshold) {
                    // step frequency
                    if (stepTimes.size >= 2) {
                        lastUpdateTime = stepTimes[stepTimes.size - 1].toInt()
                        penultimateUpdateTime = stepTimes[stepTimes.size - 2].toInt()
                        deltaBetweenTwoSteps = lastUpdateTime - penultimateUpdateTime
                        stepFreqNow = (60000 / deltaBetweenTwoSteps)

                        //definizione due liste: current and previous frequency
                        currentFrequency.add(stepFreqNow)
                        if (currentFrequency.size >= 2) {
                            previousStepFrequency.add(currentFrequency[currentFrequency.size - 2])
                        }else{
                            previousStepFrequency.add(0)
                        }

                        //stabilizzazione frequency steps
                        val lastNValues = if (currentFrequency.size >= n) {
                            currentFrequency.takeLast(n)
                        } else {
                            currentFrequency
                        }
                        val weights = List(lastNValues.size) { it * 0.5 + 1.0 }.toDoubleArray()
                        val weightedSum = lastNValues.zip(weights.toList()).sumOf { it.first * it.second }
                        val weightedAverage = weightedSum / weights.sum()
                        frequency.add(weightedAverage.toInt())
                        newStepFrequency = frequency[frequency.size-1]

                    }
                }


                //stepData ="Steps: $steps"

                // Remove times older than one minute
                while ((stepTimes.firstOrNull() ?: Long.MAX_VALUE) < now - unitTime) {
                    stepTimes.removeFirstOrNull()
                }

                // Calculate step frequency
                if (now - startTime < unitTime) {  //60000 millesimi di secondo=1 min
                    inputFreq = ceil((stepTimes.size.toFloat() / (now - startTime))*60000F)
                } else {
                    inputFreq = ceil((stepTimes.size.toFloat()/(unitTime))*60000F)
                }

                outputFreq = alpha * outputFreq + (1 - alpha) * inputFreq
                oldStepFrequency = outputFreq.toInt()

                lastStepTime = now
                //stepFreq = "Frequency: $stepFrequency step/min"
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Sensors Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    @OptIn(UnstableApi::class) @RequiresApi(Build.VERSION_CODES.O)
    private fun startForegroundService() {

        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Sport Activity")
            .setContentText("Steps: $steps\nStep Frequency: $newStepFrequency")
            .setSmallIcon(R.drawable.ic_play)
            .setContentIntent(PendingIntent.getActivity(
                this, 0,
                Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE))
            .build()

        startForeground(1, notification)
    }

    @OptIn(UnstableApi::class) @RequiresApi(Build.VERSION_CODES.O)
    private fun updateNotification() {
        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Sport Activity")
            .setContentText("Steps: $steps\nStep Frequency: $newStepFrequency")
            .setSmallIcon(R.drawable.ic_play)
            .setContentIntent(PendingIntent.getActivity(
                this, 0,
                Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE))
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(1, notification)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
    }

    @OptIn(UnstableApi::class) private fun writeToCsvFile(data: List<ValueTimestamp>) {
        // Generate a timestamp for the file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "$timeStamp.csv"

        // Convert data to CSV format
        val csvContent = StringBuilder()
        csvContent.append("Timestamp,oldSPM,newSPM,BPM\n")  // Add header
        for (entry in data) {
            csvContent.append("${entry.timestamp},${entry.oldSPM},${entry.newSPM},${entry.BPM}\n")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10 and above, use the MediaStore API
            val resolver = contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOCUMENTS}/$DIRECTORY_NAME")
            }

            val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            uri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(csvContent.toString().toByteArray())
                    outputStream.close()
                    Log.d("SensorService", "CSV file written to ${uri.path}")
                }
            }
        } else {
            // For Android 9 and below, use the traditional file method
            val documentsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), DIRECTORY_NAME)
            if (!documentsDir.exists()) {
                documentsDir.mkdirs()
            }
            val file = File(documentsDir, fileName)
            try {
                val fos = FileOutputStream(file)
                fos.write(csvContent.toString().toByteArray())
                fos.close()
                Log.d("SensorService", "CSV file written to ${file.absolutePath}")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun updateActivityLogs(value: String, value2: String, timestamp: String, bpm: String) {
        activityLogs.add(
            ValueTimestamp(
                timestamp = timestamp,
                oldSPM = value,
                newSPM = value2,
                BPM = bpm
            )
        )
    }
}