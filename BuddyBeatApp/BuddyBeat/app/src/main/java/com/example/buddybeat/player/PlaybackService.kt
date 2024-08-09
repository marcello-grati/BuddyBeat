package com.example.buddybeat.player

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.buddybeat.MainActivity
import com.example.buddybeat.SensorService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs
import kotlin.math.log2


@UnstableApi @AndroidEntryPoint
class PlaybackService : MediaSessionService(), MediaSession.Callback{

    //@Inject
    private var mediaSession: MediaSession? = null

    private lateinit var mService: SensorService
    private var mBound: Boolean = false

    var ratio = 1f

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance.
            val binder = service as SensorService.LocalBinder
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }


    // Create your Player and MediaSession in the onCreate lifecycle event
     @OptIn(UnstableApi::class) override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(AudioAttributes.DEFAULT, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()
        mediaSession = MediaSession.Builder(this, player).setCallback(this).setSessionActivity(PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)).build()

        Intent(this, SensorService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        handler.postDelayed(sensorDataRunnable, interval)
     }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player != null) {
            if (player.playWhenReady) {
                // Make sure the service is not in foreground.
                player.pause()
            }
        }
        stopSelf()
    }

    // Remember to release the player and media session in onDestroy
    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        unbindService(connection)
        mBound = false
        super.onDestroy()
    }

    override fun onAddMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: MutableList<MediaItem>
    ): ListenableFuture<List<MediaItem>> {
        val updatedMediaItems = mediaItems.map { mediaitem -> mediaitem.buildUpon().setUri(mediaitem.requestMetadata.mediaUri).build() }
        return Futures.immediateFuture(updatedMediaItems)
    }

    private val handler = Handler(Looper.getMainLooper())
    private val interval: Long = 1000

    private fun updateDataTextView() {
        run{
            Log.d("StepCadence from PlaybackService", mService.stepFreq.toString())
            updateSpeedSong()
        }

    }

    private fun updateSpeedSong() {
        val stepFreq = mService.stepFreq
        var bpm = mediaSession?.player?.mediaMetadata?.extras?.getInt("bpm")
        var rat = 1f
        if (bpm != 0 && bpm != null) {

            // We compute the log_2() of step frequency and of double, half and original value of BPM
            val logStepFreq = log2(stepFreq.toFloat())
            val logBpm = log2(bpm.toFloat())
            val logHalfBPM = log2(bpm.toFloat() / 2.0f)
            val logDoubleBPM = log2(bpm.toFloat() * 2.0f)

            // We update BPM if one of its multiples has a smaller distance value
            if (abs(logStepFreq - logBpm) > abs(logStepFreq - logHalfBPM)) {
                bpm /= 2
            } else if (abs(logStepFreq - logBpm) > abs(logStepFreq - logDoubleBPM)) {
                bpm *= 2
            }
            // Speed-up ratio computed as step frequency / BPM
            rat = stepFreq.toFloat() / bpm.toFloat()
        }
        if (stepFreq < 60)
            rat = 1f

        // ratio forced into [0.8, 1.2] range
        rat = rat.coerceAtLeast(0.8f)
        rat = rat.coerceAtMost(1.2f)

        ratio = rat

        Log.d("RATIO_3", ratio.toString())

        mediaSession?.player?.setPlaybackSpeed(ratio)
    }

    private val sensorDataRunnable = object : Runnable {
        override fun run() {
            if (mBound) {
                updateDataTextView()
                handler.postDelayed(this, interval)
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession
}