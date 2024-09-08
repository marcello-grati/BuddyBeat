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
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Date
import java.util.LinkedList
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.sqrt


/*Sensor Service new*/
@AndroidEntryPoint
class SensorService : Service(), SensorEventListener {



    @Inject
    lateinit var dataStoreManager: DataStoreManager

    //for notification
    companion object {
        const val CHANNEL_ID = "SensorsChannel"
    }

    //sensors
    private lateinit var sensorManager: SensorManager
    private var gyroSensor: Sensor? = null
    private var accelSensor: Sensor? = null

    //for steps calculation
    var lastUpdate: Long = 0
    private var lastAcceleration: Float = 0f
    private var acceleration: Float = 0f
    private var currentAcceleration: Float = SensorManager.GRAVITY_EARTH
    private val stepTimes = LinkedList<Long>()
    private var startTime: Long = 0
    private var steps: Int = 0
    private var unitTime = 60000  //60000 millisecond = 60 second

    //mode (off/walking/running)
    private var mode = 0L

    //parameters for Walking and Running modalities
    private var threshold: Float = 4f
    private var deltaTime: Long = 2000L

    private val walkingThreshold = 1.8f//1.3f
    private val walkingDeltaTime = 300L//350L

    private val runningThreshold = 2.25f
    private val runningDeltaTime = 600L //250L

    /* parameters for calculation stepFreq method 1 */
    private var alpha: Float = 0.5f
    private var inputFreq: Float = 0f
    private var outputFreq: Float = 0f
    private var stepFrequency_1: Int = 0

    /* parameters for calculation stepFreq method 2 */
    private var lastUpdateTime: Long = 0L
    private var penultimateUpdateTime: Long = 0L
    private var deltaBetweenTwoSteps: Long = 0L
    private var stepFreqNow: Long = 0L
    private val currentFrequency = mutableListOf<Long>()
    val previousStepFrequency = mutableListOf<Int>()
    private var stepFrequency: Int = 0
    private var stepFrequency_2: Int = 0

    /*private val n = 15 //stabilization of frequency values
    private var stepFrequency_3: Int = 0
    val previousStepFrequency_3 = mutableListOf<Int>()
    private val currentFrequency_3 = mutableListOf<Long>()*/


    //binder for connecting to activity
    private val binder = LocalBinder()
    inner class LocalBinder : Binder() {
        fun getService(): SensorService = this@SensorService
    }
    override fun onBind(intent: Intent): IBinder {
        return binder
    }


    // SPM variable read by other components
    val stepFreq: Int
        get() = if (System.currentTimeMillis() - lastUpdate > 3000) 0 //if time between now and lastStep is greater than 3 seconds you are standing still (returns 0)
        else previousStepFrequency.takeLast(7).takeWhile { it > 65 }.average().toInt() //average of last SPM


    private var bpm_song = 0
    // function called by PlaybackService when changing ratio
    fun updateBpm(bpm: Float) {
        bpm_song = bpm.toInt()
    }
    // parameters for writing in csv
    private val DIRECTORY_NAME = "BuddyBeat Logs"
    private val activityLogs = mutableListOf<ValueTimestamp>()

    data class ValueTimestamp(
        val timestamp: String,
        val SPM_1: String,
        val SPM_2: String,
        val BPM: String,
        val steps: String
    )

    //write in CSV every second
    private val handler = Handler(Looper.getMainLooper())
    private val interval: Long = 1000

    private val writeLogs = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun run() {
            val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            updateActivityLogs(
                currentTime,
                stepFrequency_1.toString(),
                stepFreq.toString(),
                bpm_song.toString(),
                steps.toString()
            )
            updateNotification()
            handler.postDelayed(this, interval)
        }
    }

    private fun updateActivityLogs(
        timestamp: String,
        value1: String,
        value2: String,
        bpm: String,
        steps: String
    ) {
        activityLogs.add(
            ValueTimestamp(
                timestamp = timestamp,
                SPM_1 = value1,
                SPM_2 = value2,
                BPM = bpm,
                steps = steps
            )
        )
    }

    //functions for changing mode
    @OptIn(UnstableApi::class)
    fun changeMode(mode: Long) {
        this@SensorService.mode = mode
        when (mode) {
            1L -> { // walking
                setWalkingMode()
                Log.d("SensorService", "Setting Walking Mode")
            }
            2L -> { //running
                setRunningMode()
                Log.d("SensorService", "Setting Running Mode")
            }
            0L -> {
                reset()
                Log.d("SensorService", "Reset Mode")
            }
        }
    }

    private fun reset() {
        threshold = 4f
        deltaTime = 2000L
    }

    private fun setWalkingMode() {
        threshold = walkingThreshold
        deltaTime = walkingDeltaTime
    }

    private fun setRunningMode() {
        threshold = runningThreshold
        deltaTime = runningDeltaTime
    }


    @OptIn(UnstableApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        return START_STICKY
    }


    /* access of data from sensors */
    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(UnstableApi::class)
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
        handler.postDelayed(writeLogs, interval) // write in csv
    }


    @OptIn(UnstableApi::class)
    override fun onDestroy() {
        Log.d("SensorService", "Destroying service...")
        writeToCsvFile(activityLogs)
        runBlocking {//reset mode
            dataStoreManager.setPreferenceLong(DataStoreManager.MODE, 0L)
            dataStoreManager.setPreferenceLong(DataStoreManager.MODALITY, 0L)
        }
        handler.removeCallbacksAndMessages(null)
        sensorManager.unregisterListener(this, gyroSensor)
        sensorManager.unregisterListener(this, accelSensor)
        super.onDestroy()
    }

    /* Calculation of steps and frequency of steps from sensor data */
    @OptIn(UnstableApi::class)
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

                /* low pass filter respect to delta */
                acceleration = acceleration * 0.9f + delta
                val now = System.currentTimeMillis()

                /* step calculus */
                if (acceleration > threshold) {
                    if ((now - lastUpdate) > deltaTime) {
                        Log.d("SensorService", "deltaTime: $deltaTime + threshold: $threshold + mode: $mode")
                        lastUpdate = now
                        steps++
                        stepTimes.add(now)
                    }
                }

                // METHOD 2
                /* step frequency calculus based on difference between two steps */
                if (acceleration > threshold) {
                    if (stepTimes.size >= 2) {
                        //take last 2 steps
                        val l = stepTimes.takeLast(2)
                        lastUpdateTime = l.last()
                        penultimateUpdateTime = l.first()
                        deltaBetweenTwoSteps = abs(lastUpdateTime - penultimateUpdateTime)
                        //calculate SPM based of difference between last 2 step TimeStamps
                        stepFreqNow = (60000 / deltaBetweenTwoSteps)

                        //save the frequency calculated in a list
                        currentFrequency.add(stepFreqNow)

                        //method 1
                        if (currentFrequency.size >= 5) {
                            //make average of 5 values and clear list
                            val newFreq = currentFrequency.average()
                            currentFrequency.clear()
                            /*stepFrequency_2 = if (newFreq < 65) 0
                            else (alpha * stepFrequency_2 + (1 - alpha) * newFreq).toInt()*/
                            //filter
                            stepFrequency = if (newFreq < 65) //if less than 65 equals 0
                                0 else (alpha * stepFrequency + (1 - alpha) * newFreq).toInt() //filter frequency
                            //if walking keep SPM, if running SPM*2
                            stepFrequency_2 = if(mode==1L) stepFrequency else if(mode==2L) (stepFrequency*2) else stepFrequency
                            Log.d("stepFrequency_2", stepFrequency_2.toString())
                            //save SPM calculated
                            previousStepFrequency.add(stepFrequency_2)
                        }

                        //method 2
                        //stabilization of frequency steps
                        /*currentFrequency_3.add(stepFreqNow)
                        val lastNValues = if (currentFrequency_3.size >= n) {
                            currentFrequency_3.takeLast(n)
                        } else {
                            currentFrequency_3
                        }
                        val weights = List(lastNValues.size) { it * 0.5 + 1.0 }.toDoubleArray()
                        val weightedSum =
                            lastNValues.zip(weights.toList()).sumOf { it.first * it.second }
                        val weightedAverage = weightedSum / weights.sum()
                        val newFreq = weightedAverage.toInt()
                        stepFrequency_3 = if (newFreq < 65)
                            0 else (alpha * stepFrequency_3 + (1 - alpha) * newFreq).toInt()
                        Log.d("stepFrequency_3", stepFrequency_3.toString())
                        previousStepFrequency_3.add(stepFrequency_3)*/
                    }
                }

                // Remove times older than one minute
                while ((stepTimes.firstOrNull() ?: Long.MAX_VALUE) < now - unitTime) {
                    stepTimes.removeFirstOrNull()
                }

                // METHOD 1
                inputFreq =
                    if (now - startTime < unitTime) {  //60000 millisecond = 60 second = 1 min
                        ceil((stepTimes.size.toFloat() / (now - startTime)) * 60000F)
                    } else {
                        ceil((stepTimes.size.toFloat() / (unitTime)) * 60000F)
                    }
                outputFreq = alpha * outputFreq + (1 - alpha) * inputFreq
                stepFrequency_1 = outputFreq.toInt()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // notification
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

    @OptIn(UnstableApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startForegroundService() {
        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Sport Activity")
            .setContentText("Steps: $steps \nStep Frequency: ${if(mode!=0L) stepFreq else "-"}")
            .setSmallIcon(R.drawable.icona_1)
            .setContentIntent(
                PendingIntent.getActivity(
                    this, 0,
                    Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()
        startForeground(1, notification)
    }

    @OptIn(UnstableApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateNotification() {
        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Sport Activity")
            .setContentText("Steps: $steps\nStep Frequency: ${if(mode!=0L) stepFreq else "-"}")
            .setSmallIcon(R.drawable.icona_1)
            .setContentIntent(
                PendingIntent.getActivity(
                    this, 0,
                    Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(1, notification)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
    }

    //write csv
    @OptIn(UnstableApi::class)
    private fun writeToCsvFile(data: List<ValueTimestamp>) {
        // Generate a timestamp for the file name
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "$timeStamp.csv"

        // Convert data to CSV format
        val csvContent = StringBuilder()
        csvContent.append("Timestamp,SPM_1,SPM_2,BPM,steps\n")  // Add header
        for (entry in data) {
            csvContent.append("${entry.timestamp},${entry.SPM_1},${entry.SPM_2},${entry.BPM}, ${entry.steps}\n")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10 and above, use the MediaStore API
            val resolver = contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    "${Environment.DIRECTORY_DOCUMENTS}/$DIRECTORY_NAME"
                )
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
            val documentsDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                DIRECTORY_NAME
            )
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

}