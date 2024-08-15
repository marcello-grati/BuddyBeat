package com.example.buddybeat.player

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.buddybeat.MainActivity
import com.example.buddybeat.SensorService
import com.example.buddybeat.data.models.Song
import com.example.buddybeat.data.repository.AudioRepository
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import java.util.Dictionary
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log2
import kotlin.math.min


@UnstableApi @AndroidEntryPoint
class PlaybackService : MediaSessionService(), MediaSession.Callback{

    companion object {
        //var playlist: MutableMap<String, MediaItem> = mutableMapOf()
        var playlist : MutableList<String> = mutableListOf()
    }

    @Inject
    lateinit var songRepo: AudioRepository

    private var mediaSession: MediaSession? = null

    private lateinit var mService: SensorService
    private var mBound: Boolean = false

    var ratio = 1f

    lateinit var audiolist : LiveData<MutableList<Song>>

    private val handler = Handler(Looper.getMainLooper())
    private val interval: Long = 1000

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
        audiolist = songRepo.getAllSongs()
        handler.postDelayed(sensorDataRunnable, interval)
        handler.postDelayed(orderSongsRunnable, interval)
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
        playlist.add(updatedMediaItems.last().mediaId)
        Log.d("onAddMediaItems", playlist.toString())
        return Futures.immediateFuture(updatedMediaItems)
    }


    private val sensorDataRunnable = object : Runnable {
        override fun run() {
            if (mBound) {
                updateSpeedSong()
                handler.postDelayed(this, interval)
            }
        }
    }

    private fun updateSpeedSong() {
        val stepFreq = mService.stepFreq
        Log.d("StepCadence from PlaybackService", stepFreq.toString())
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



    private val orderSongsRunnable = object : Runnable {
        override fun run() {
            Log.d("IOOOO", "orderSongsRunnable")
            if (mBound) {
                val pos = mediaSession?.player?.currentPosition
                val dur = mediaSession?.player?.duration
                Log.d("pos", pos.toString())
                Log.d("dur", dur.toString())
                if(pos!=null && dur!=null && dur>0) {
                    Log.d("pos", pos.toString())
                    Log.d("dur", dur.toString())
                    if (pos >= dur - min(5000.0, 0.9 * dur)) {
                        val l = orderSongs()
                        l?.forEach {
                            Log.d(it.toString(), it.bpm.toString())
                        }
                        while(true){
                            val nextSong = l?.removeFirstOrNull()
                            if(nextSong != null) {
                                val media = buildMediaItem(nextSong)
                                if (!playlist.contains(media.mediaId)) {
                                    setSongInPlaylist(media)
                                    break
                                }

                            }
                        }
                    }
                }
            }
            handler.postDelayed(this, interval*5)
        }
    }

    private fun setSongInPlaylist(media: MediaItem){
        Log.d("IOOOO", "media: $media")
        mediaSession?.player!!.addMediaItem(media)
        Log.d("IOOOO", "currentMediaItemIndex + currentMediaItem" +mediaSession?.player?.currentMediaItemIndex.toString() + "   " + mediaSession?.player?.currentMediaItem.toString())
        Log.d("IOOOO", "mediaItemCount: " + mediaSession?.player?.mediaItemCount.toString())
    }
    fun orderSongs(): MutableList<Song>? {

        Log.d("Ordering", "Song list reordered from PlayBackService")
        val myCustomComparator = Comparator<Song> { a, b ->

            when {
                a.bpm <= 0 && b.bpm <= 0 -> return@Comparator 0
                a.bpm <= 0 -> return@Comparator 1
                b.bpm <= 0 -> return@Comparator -1
                else -> {
                    var logBpmA = log2(a.bpm.toFloat())
                    var logBpmB = log2(b.bpm.toFloat())
                    val stepFreq = mService.stepFreq.toFloat()
                    var logStepFreq = log2(stepFreq)
                    Log.d("stepFreq from reordering", stepFreq.toString())
                    logBpmA -= floor(logBpmA)
                    logBpmB -= floor(logBpmB)
                    logStepFreq -= floor(logStepFreq)
                    var distA = abs(logBpmA - logStepFreq)
                    var distB = abs(logBpmB - logStepFreq)
                    if (distA > 0.5f)
                        distA = 1.0f - distA
                    if (distB > 0.5f)
                        distB = 1.0f - distB
                    //Log.d("Comparator", "Comparing ${a.bpm} and ${b.bpm} => result: $distA, $distB")
                    when {
                        distA < distB -> return@Comparator -1
                        distA > distB -> return@Comparator 1
                        else -> return@Comparator 0
                    }
                }
            }

        }
        val list = audiolist.value?.toMutableList()
        return list?.sortedWith(myCustomComparator)?.toMutableList()
    }

    private fun buildMediaItem(audio: Song): MediaItem {
        return MediaItem.Builder()
            .setMediaId(audio.uri)
            .setUri(audio.uri.toUri())
            .setRequestMetadata(
                MediaItem.RequestMetadata.Builder()
                    .setMediaUri(audio.uri.toUri())
                    .build()
            )
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setAlbumArtist(audio.artist)
                    .setArtist(audio.artist)
                    .setTitle(audio.title)
                    .setDisplayTitle(audio.title)
                    .setExtras(Bundle().apply {
                        putInt("bpm", audio.bpm)
                        putLong("id", audio.songId)
                    })
                    .build()
            )
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession
}