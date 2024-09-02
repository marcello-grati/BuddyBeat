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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
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

    //sensors
    private lateinit var sensorManager: SensorManager
    private var gyroSensor: Sensor? = null
    private var accelSensor: Sensor? = null

    //for steps calculation
    private var lastUpdate: Long = 0
    private var lastAcceleration: Float = 0f
    private var acceleration: Float = 0f
    private var currentAcceleration: Float = SensorManager.GRAVITY_EARTH
    private val stepTimes = LinkedList<Long>()
    private var startTime: Long = 0
    private var steps: Int = 0


    /* parameters for steps calculation */
    private var unitTime = 60000  //60000 millisecond = 60 second

    /*SILVIA
    private var threshold = mutableDoubleStateOf(0.7)  //for step calculus
    private var deltaTime = mutableIntStateOf(250) // 0.5s interval of data refresh
    */

    @OptIn(UnstableApi::class)
    fun changeMode(mode:Long){
        if(mode == 1L) { // walking
            setWalkingMode()
        }
        else if (mode == 2L){ //running
            setRunningMode()
        }
    }

    /*CHIARA*/
    // Walking and Running modalities
    private var threshold: Float = 0.0f
    private var deltaTime: Long = 0L

    private val walkingThreshold = 1.3f
    private val walkingDeltaTime = 350L

    private val runningThreshold = 2.25f
    private val runningDeltaTime = 250L

    private fun setWalkingMode() {
        threshold = walkingThreshold
        deltaTime = walkingDeltaTime
    }
    private fun setRunningMode() {
        threshold = runningThreshold
        deltaTime = runningDeltaTime
    }

    /* parameters for calculation stepFreq method 1 */
    private var alpha: Float = 0.5f
    private var inputFreq: Float = 0f
    private var outputFreq: Float = 0f
    private var stepFrequency_1: Int = 0


    /* parameters for calculation stepFreq method 2 */
    private var lastUpdateTime : Int = 0 //new
    private var penultimateUpdateTime: Int = 0 //new
    private var deltaBetweenTwoSteps: Int = 0 //new
    private var stepFreqNow : Int = 0 //new: starting value of frequency steps
    private val previousStepFrequency = mutableListOf<Int>()
    private val currentFrequency = mutableListOf<Int>()
    private val frequency = mutableListOf<Int>()
    private var stepFrequency_2: Int = 0
    private val n = 15 //stabilization of frequency values


    /* parameters for calculation stepFreq method 3 */
    private var lastUpdateTime_3: Long = 0 //new
    private var penultimateUpdateTime_3: Long = 0 //new
    private var deltaBetweenTwoSteps_3: Long = 0 //new
    private var stepFreqNow_3: Long = 0 //new: starting value of frequency steps
    val previousStepFrequency_3 = mutableListOf<Int>()
    private val currentFrequency_3 = mutableListOf<Long>()
    private var stepFrequency_3: Int = 0


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


    // parameters for writing in csv
    private val DIRECTORY_NAME = "BuddyBeat Logs"
    private var bpm_song = 0
    private val activityLogs = mutableListOf<ValueTimestamp>()
    data class ValueTimestamp(
        val timestamp: String,
        val SPM_1: String,
        val SPM_2: String,
        val SPM_3: String,
        val BPM: String,
        val steps : String
    )

    // function called when changing ratio
    fun updateBpm(bpm: Float) {
        bpm_song = bpm.toInt()
    }

    // variable read by other components
    val stepFreq: Int
        get() = stepFrequency_3


    private val handler = Handler(Looper.getMainLooper())
    private val interval: Long = 1000

    private val writeLogs = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun run() {
            val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            updateActivityLogs(
                currentTime,
                stepFrequency_1.toString(),
                stepFrequency_2.toString(),
                stepFrequency_3.toString(),
                bpm_song.toString(),
                steps.toString()
            )
            updateNotification()
            handler.postDelayed(this, interval)

        }
    }

    private fun updateActivityLogs(timestamp: String, value1: String, value2: String, value3 : String, bpm: String, steps : String) {
        activityLogs.add(
            ValueTimestamp(
                timestamp = timestamp,
                SPM_1 = value1,
                SPM_2 = value2,
                SPM_3 = value3,
                BPM = bpm,
                steps = steps
            )
        )
    }

    // METHOD 3
    private val calculateFreq = object : Runnable {
        override fun run() {
            calculationNewFreq()
            handler.postDelayed(this, 200)
        }
    }

    @OptIn(UnstableApi::class)
    private fun calculationNewFreq() {
        if (stepTimes.size >= 2) {

            val l = stepTimes.takeLast(2)
            lastUpdateTime_3 = l.last()

            if (System.currentTimeMillis() - lastUpdateTime_3 > 3000) {
                stepFrequency_3 = 0
                Log.d("stepFrequency_3", stepFrequency_3.toString())
                previousStepFrequency_3.add(stepFrequency_3)
            } else {
                penultimateUpdateTime_3 = l.first()
                deltaBetweenTwoSteps_3 = abs(lastUpdateTime_3 - penultimateUpdateTime_3)
                stepFreqNow_3 = (60000 / deltaBetweenTwoSteps_3)
                currentFrequency_3.add(stepFreqNow_3)

                //stabilization of frequency steps
                if (currentFrequency_3.size >= 10) {
                    val newFreq = currentFrequency_3.average()
                    currentFrequency_3.clear()
                    stepFrequency_3 = if (newFreq < 65)
                        0
                    else (alpha * stepFrequency_3 + (1 - alpha) * newFreq).toInt()
                    Log.d("stepFrequency_3", stepFrequency_3.toString())
                    previousStepFrequency_3.add(stepFrequency_3)
                }

            }
        }
    }

    @OptIn(UnstableApi::class) @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()

        /*CHIARA*/
        intent?.action?.let { action ->
            Log.d("SensorService", "Received action: $action") // Log per tracciare l'azione ricevuta
            when (action) {
                "SET_WALKING_MODE" -> {
                    Log.d("SensorService", "Setting Walking Mode") // Log per tracciare la modalità camminata
                    setWalkingMode()
                }
                "SET_RUNNING_MODE" -> {
                    Log.d("SensorService", "Setting Running Mode") // Log per tracciare la modalità corsa
                    setRunningMode()
                }
                else -> {
                    Log.d("SensorService", "Unknown action received: $action") // Log per azioni sconosciute
                }
            }
        }

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
        handler.postDelayed(calculateFreq, 500) // calculate stepFreq_3
    }

    /*SILVIA*/
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    @OptIn(UnstableApi::class)
    override fun onDestroy() {
        Log.d("SensorService", "Distruggo il service")
        writeToCsvFile(activityLogs)
        scope.launch {
            dataStoreManager.setPreferenceLong(DataStoreManager.MODE, 0L)
            dataStoreManager.setPreferenceLong(DataStoreManager.MODALITY, 0L)
        }
        handler.removeCallbacksAndMessages(null)
        sensorManager.unregisterListener(this, gyroSensor)
        sensorManager.unregisterListener(this, accelSensor)
        job.cancelChildren()
        super.onDestroy()
    }

    /* Calculation of steps and frequency of steps from sensor data */
    @OptIn(UnstableApi::class) override fun onSensorChanged(event: SensorEvent?) {
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
                /*SILVIA
                if (acceleration > threshold.value) {
                    if ((now - lastUpdate) > deltaTime.value) {
                */
                if (acceleration > threshold) {
                    if ((now - lastUpdate) > deltaTime) {
                        lastUpdate = now
                        steps++
                        stepTimes.add(now)
                    }
                }

                // METHOD 2
                /* step frequency calculus based on difference between two steps */
                /* SILVIA
                if (acceleration > threshold.value) {
                */
                if (acceleration > threshold) {
                    // step frequency
                    if (stepTimes.size >= 2) {
                        lastUpdateTime = stepTimes[stepTimes.size - 1].toInt()
                        penultimateUpdateTime = stepTimes[stepTimes.size - 2].toInt()
                        deltaBetweenTwoSteps = lastUpdateTime - penultimateUpdateTime
                        stepFreqNow = (60000 / deltaBetweenTwoSteps)

                        //definition of two lists: current and previous frequency
                        currentFrequency.add(stepFreqNow)
                        if (currentFrequency.size >= 2) {
                            previousStepFrequency.add(currentFrequency[currentFrequency.size - 2])
                        }else{
                            previousStepFrequency.add(0)
                        }

                        //stabilization of frequency steps
                        val lastNValues = if (currentFrequency.size >= n) {
                            currentFrequency.takeLast(n)
                        } else {
                            currentFrequency
                        }
                        val weights = List(lastNValues.size) { it * 0.5 + 1.0 }.toDoubleArray()
                        val weightedSum = lastNValues.zip(weights.toList()).sumOf { it.first * it.second }
                        val weightedAverage = weightedSum / weights.sum()
                        frequency.add(weightedAverage.toInt())
                        val newFreq = frequency[frequency.size-1]
                        stepFrequency_2 = (alpha * stepFrequency_2 + (1 - alpha) * newFreq).toInt()
                        Log.d("stepFrequency_2", stepFrequency_2.toString())
                    }
                }

                // Remove times older than one minute
                while ((stepTimes.firstOrNull() ?: Long.MAX_VALUE) < now - unitTime) {
                    stepTimes.removeFirstOrNull()
                }

                // METHOD 1
                inputFreq = if (now - startTime < unitTime) {  //60000 millisecond = 60 second = 1 min
                    ceil((stepTimes.size.toFloat() / (now - startTime)) * 60000F)
                } else {
                    ceil((stepTimes.size.toFloat() / (unitTime)) * 60000F)
                }
                outputFreq = alpha * outputFreq + (1 - alpha) * inputFreq
                stepFrequency_1 = outputFreq.toInt()
                Log.d("stepFrequency_1", stepFrequency_1.toString())
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
            .setContentText("Steps: $steps \nStep Frequency: $stepFreq")
            .setSmallIcon(R.drawable.ic_play)
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
            .setContentText("Steps: $steps\nStep Frequency: $stepFreq")
            .setSmallIcon(R.drawable.ic_play)
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

    @OptIn(UnstableApi::class)
    private fun writeToCsvFile(data: List<ValueTimestamp>) {
        // Generate a timestamp for the file name
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "$timeStamp.csv"

        // Convert data to CSV format
        val csvContent = StringBuilder()
        csvContent.append("Timestamp,SPM_1,SPM_2,SPM_3,BPM,steps\n")  // Add header
        for (entry in data) {
            csvContent.append("${entry.timestamp}, ${entry.SPM_1}, ${entry.SPM_2}, ${entry.SPM_3}, ${entry.BPM}, ${entry.steps}\n")
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