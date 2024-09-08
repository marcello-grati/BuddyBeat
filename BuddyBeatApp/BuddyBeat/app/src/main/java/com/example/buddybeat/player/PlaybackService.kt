package com.example.buddybeat.player

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
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
import androidx.compose.runtime.mutableStateListOf
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.example.buddybeat.DataStoreManager
import com.example.buddybeat.SensorService
import com.example.buddybeat.data.models.Song
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import java.io.FileNotFoundException
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log2
import kotlin.math.min

/*Playback Service for playback functionality (also in background))*/
@UnstableApi @AndroidEntryPoint
class PlaybackService : MediaSessionService(), MediaSession.Callback{

    companion object {
        var playlist : MutableList<String> = mutableListOf() //already played
        var audiolist =  mutableStateListOf<Song>()//list of possible songs
        var audioListId = MutableStateFlow(0L)
        var queue =  mutableStateListOf<Song>()

        const val OFF_MODE = 0L
        const val AUTO_MODE = 1L
        const val MANUAL_MODE = 2L

        var DEFAULT_BPM = 100
        var ALPHA = 0.5f
        const val BPM_STEP = 2

        var speedMode = OFF_MODE
        var manualBpm = DEFAULT_BPM
        var ratio = 1f
    }

    @Inject
    lateinit var customMediaNotificationProvider : CustomMediaNotificationProvider

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    //mediaSession
    private var mediaSession: MediaSession? = null

    //connection to SensorService
    private lateinit var mService: SensorService
    private var mBound: Boolean = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as SensorService.LocalBinder
            mService = binder.getService()
            mBound = true
        }
        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }


    // Create Player and MediaSession
     @OptIn(UnstableApi::class) override fun onCreate() {
        super.onCreate()
        //player
        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(AudioAttributes.DEFAULT, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()
        //if opened from the notification
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.putExtra("DESTINATION", "player")
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT)
        //mediaSession
        mediaSession = MediaSession.Builder(this, player).setCallback(this).setSessionActivity(pendingIntent).build()
        mediaSession!!.setCustomLayout(notificationPlayerCustomCommandButtons)
        Intent(this, SensorService::class.java).also { int ->
            bindService(int, connection, Context.BIND_AUTO_CREATE)
        }
        handler.postDelayed(sensorDataRunnable, 15000) // after 10 seconds it starts to updateSpeedSongs
        handler.postDelayed(orderSongsRunnable, 2000) // after 2 seconds it start to check if it's necessary to calculate the next song
        setMediaNotificationProvider(customMediaNotificationProvider)
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


    // Release the player and media session
    override fun onDestroy() {
        if (!mBound) {
            //reset mode and modality
            runBlocking {
                dataStoreManager.setPreferenceLong(DataStoreManager.MODE, 0L)
                mService.changeMode(0L)
            }
            runBlocking {
                dataStoreManager.setPreferenceLong(DataStoreManager.MODALITY, 0L)
            }
        }
        //release
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        //reset values
        speedMode = OFF_MODE
        manualBpm = DEFAULT_BPM
        audiolist.clear()
        audioListId.value = 0L
        playlist.clear()
        queue.clear()
        unbindService(connection)
        handler.removeCallbacksAndMessages(null)
        mBound = false
        super.onDestroy()
    }

    //when MediaItem is added from controller, it will add it to playlist, that is the list of already played song
    override fun onAddMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: MutableList<MediaItem>
    ): ListenableFuture<List<MediaItem>> {
        val updatedMediaItems = mediaItems.map { mediaitem -> mediaitem.buildUpon().setUri(mediaitem.requestMetadata.mediaUri).build() }
        playlist.add(updatedMediaItems.last().mediaId)
        if(playlist.size > ((audiolist.size.div(2)))) {
            playlist.removeFirstOrNull()
        }
        Log.d("Playlist", playlist.toString())
        return Futures.immediateFuture(updatedMediaItems)
    }


    //when MediaItem is added from this Service,it will add it to playlist, that is the list of already played song
    private fun onAddSong(media : MediaItem){
        playlist.add(media.mediaId)
        if(playlist.size > (audiolist.size.div(2)))
            playlist.removeFirstOrNull()
        Log.d("Playlist", playlist.toString())
    }

    private val handler = Handler(Looper.getMainLooper())
    private val interval: Long = 1000

    //updateSpeedSong every 4 seconds
    private val sensorDataRunnable = object : Runnable {
        override fun run() {
            if (mBound) {
                updateSpeedSong()
                handler.postDelayed(this, interval*4) //every 4 seconds change ratio
            }
        }
    }

    private fun updateSpeedSong() {

        var inRatio: Float
        var outRatio = ratio
        if (speedMode != OFF_MODE) {
            //get bpm song playing
            var bpm = mediaSession?.player?.mediaMetadata?.extras?.getInt("bpm")
            //if bpm valid
            if (bpm != 0 && bpm != null && bpm != -1) {
                //collect SPM
                val stepFreq = when (speedMode) {
                    AUTO_MODE -> {
                        if(System.currentTimeMillis()-mService.lastUpdate>3000)
                            0f
                        else {
                            //average of last StepFrequencies
                            val d = mService.previousStepFrequency.takeLast(7).takeWhile { it > 65 }
                            var l = d.average()
                            if (l.isNaN()) {
                                l = 0.0
                            }
                            l.toFloat()
                        }
                    }
                    MANUAL_MODE -> {
                        manualBpm.toFloat()
                    }
                    else -> throw Exception("Invalid speed mode")
                }
                Log.d("SPM for updating speed song", stepFreq.toString())
                if (stepFreq < 60 && speedMode==AUTO_MODE)
                    inRatio = 1f
                else {
                    //calculation of ratio
                    // We compute the log_2() of step frequency and of double, half and original value of BPM
                    val logStepFreq = log2(stepFreq)
                    var logBpm = log2(bpm.toFloat())
                    var logHalfBPM = log2(bpm.toFloat() / 2.0f)
                    var logDoubleBPM = log2(bpm.toFloat() * 2.0f)

                    // We update BPM if one of its multiples has a smaller com.example.buddybeat.ui.screens.distance value
                    while (abs(logStepFreq - logBpm) > abs(logStepFreq - logHalfBPM)) {
                        bpm /= 2
                        logBpm = logHalfBPM
                        logHalfBPM = log2(bpm.toFloat() / 2.0f)
                    }
                    while (abs(logStepFreq - logBpm) > abs(logStepFreq - logDoubleBPM)) {
                        bpm *= 2
                        logBpm = logDoubleBPM
                        logDoubleBPM = log2(bpm.toFloat() * 2.0f)
                    }
                    // Speed-up ratio computed as step frequency / BPM
                    inRatio = stepFreq / bpm.toFloat()

                    // ratio forced into [0.8, 1.2] range
                    inRatio = inRatio.coerceAtLeast(0.8f)
                    inRatio = inRatio.coerceAtMost(1.25f)
                }
                //filter ratio
                outRatio = ALPHA * outRatio + (1 - ALPHA) * inRatio

                ratio = outRatio
                mService.updateBpm(ratio*bpm) //update Bpm in csv
            }
            else { //if bpm not valid
                ratio = 1f
            }
        } else { // if off mode
            ratio = 1f
        }
        mediaSession?.player?.setPlaybackSpeed(ratio)
        Log.d("PlaybackService - ratio", ratio.toString())

    }


    //check if it's necessary to calculate next song every 6 second
    private val orderSongsRunnable = object : Runnable {
        override fun run() {
            var int = interval*6
            if (mBound) {
                val pos = mediaSession?.player?.currentPosition
                val dur = mediaSession?.player?.duration
                if(pos!=null && dur!=null && dur>0) {
                    //if 5 seconds before the end of song start calculating next song
                    if (pos >= dur - min(5000.0, 0.9 * dur) && pos <= dur ) {
                        nextSong()
                    }
                    else if(pos>=dur-7000.0){
                        int = 2000
                    }
                }
            }
            handler.postDelayed(this, int)
        }
    }

    private fun nextSong() {
        //search for songs in queue (inserted by user)
        while (true) {
            val queueNext = queue.removeFirstOrNull()
            if (queueNext != null) {
                val media = buildMediaItem(queueNext)
                if (media != null) {
                    setSongInPlaylist(media)
                    return
                }
            } else break
        }
        //get target Bpm according to the modality
        val target = when (speedMode) {
            AUTO_MODE -> run{
                val d = mService.previousStepFrequency.takeLast(7).takeWhile { it > 65 }
                var l = d.average()
                if(l.isNaN()){
                    l=0.0
                }
                l
            }
            MANUAL_MODE -> manualBpm.toDouble()
            OFF_MODE -> 0.0
            else -> throw Exception("Invalid speed mode")
        }
        Log.d("SPM for choosing next Song", target.toString())
        //orderSongs according to targetBpm
        val l = if (target!=0.0) orderSongs(target) else audiolist.toMutableList()
        //removing songs without bpm
        if(speedMode!=OFF_MODE)
            l.removeAll { it.bpm == -1 || it.bpm == 0 }
        //insert most fitting song in queue
        while(true){
            val nextSong = l.removeFirstOrNull()
            if(nextSong != null) {
                val media = buildMediaItem(nextSong)
                if(media!=null) {
                    //check if already played recently
                    if (!playlist.contains(media.mediaId)) {
                        setSongInPlaylist(media)
                        break
                    }
                }
            }else return
        }
    }

    private fun setSongInPlaylist(media: MediaItem){
        Log.d("Setting media:", media.mediaMetadata.title.toString() + ", " + media.mediaId)
        mediaSession?.player!!.addMediaItem(mediaSession?.player!!.currentMediaItemIndex+1,media)
        onAddSong(media)
    }

    //function for reordering songs
    private fun orderSongs(target : Double): MutableList<Song> {
        val myCustomComparator = Comparator<Song> { a, b ->
            when {
                a.bpm <= 0 && b.bpm <= 0 -> return@Comparator 0
                a.bpm <= 0 -> return@Comparator 1
                b.bpm <= 0 -> return@Comparator -1
                else -> {
                    var logBpmA = log2(a.bpm.toFloat())
                    var logBpmB = log2(b.bpm.toFloat())
                    var logTarget = log2(target)
                    logBpmA -= floor(logBpmA)
                    logBpmB -= floor(logBpmB)
                    logTarget -= floor(logTarget)
                    var distA = abs(logBpmA - logTarget)
                    var distB = abs(logBpmB - logTarget)
                    if (distA > 0.5f)
                        distA = 1.0f - distA
                    if (distB > 0.5f)
                        distB = 1.0f - distB
                    when {
                        distA < distB -> return@Comparator -1
                        distA > distB -> return@Comparator 1
                        else -> return@Comparator 0
                    }
                }
            }

        }
        val list = audiolist.toMutableList()
        return list.sortedWith(myCustomComparator).toMutableList()
    }

    private fun buildMediaItem(audio: Song): MediaItem? {
        //check if audio file exists
        try {
            contentResolver.openInputStream(audio.uri.toUri())?.close()
        } catch (e: FileNotFoundException) {
            Log.d("buildMediaItem", e.toString())
            audiolist.remove(audio)
            playlist.remove(audio.uri)
            queue.remove(audio)
            return null
        }
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


    // CUSTOM NOTIFICATION
    private val notificationPlayerCustomCommandButtons =
        NotificationPlayerCustomCommandButton.entries.map { command -> command.commandButton }

    @SuppressLint("WrongConstant")
    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        val connectionResult = super.onConnect(session, controller)
        val availableSessionCommands = connectionResult.availableSessionCommands.buildUpon()

        /* Registering custom player command buttons for player notification. */
        notificationPlayerCustomCommandButtons.forEach { commandButton ->
            commandButton.sessionCommand?.let(availableSessionCommands::add)
        }
        val avail = connectionResult.availablePlayerCommands.buildUpon()
        avail.remove(Player.COMMAND_SEEK_TO_NEXT)
        avail.remove(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)

        return MediaSession.ConnectionResult.accept(
            availableSessionCommands.build(),
            avail.build()
        )
    }

    override fun onPostConnect(session: MediaSession, controller: MediaSession.ControllerInfo) {
        super.onPostConnect(session, controller)
        if (notificationPlayerCustomCommandButtons.isNotEmpty()) {
            /* Setting custom player command buttons to mediaLibrarySession for player notification. */
            mediaSession!!.setCustomLayout(notificationPlayerCustomCommandButtons)
        }
    }

    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle
    ): ListenableFuture<SessionResult> {
        /* Handling custom command buttons from player notification. */
        if (customCommand.customAction == NotificationPlayerCustomCommandButton.NEXT.customAction) {
            nextSong()
            mediaSession?.player?.seekToDefaultPosition(mediaSession?.player!!.currentMediaItemIndex + 1)
        }
        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
    }
}

private const val CUSTOM_COMMAND_NEXT = "NEXT"

enum class NotificationPlayerCustomCommandButton(
    val customAction: String,
    val commandButton: CommandButton,
) {
    NEXT(
        customAction = CUSTOM_COMMAND_NEXT,
        commandButton = CommandButton.Builder()
            .setDisplayName("Next")
            .setExtras(Bundle().apply { putInt("COMMAND_KEY_COMPACT_VIEW_INDEX",0)})
            .setSessionCommand(SessionCommand(CUSTOM_COMMAND_NEXT, Bundle()))
            .setIconResId(androidx.media3.session.R.drawable.media3_notification_seek_to_next )
            .build(),
    );
}

@UnstableApi
class CustomMediaNotificationProvider(context: Context) : DefaultMediaNotificationProvider(context) {

    override fun addNotificationActions(
        mediaSession: MediaSession,
        mediaButtons: ImmutableList<CommandButton>,
        builder: NotificationCompat.Builder,
        actionFactory: MediaNotification.ActionFactory
    ): IntArray {
        /* Retrieving notification default play/pause button from mediaButtons list. */
        val previous = mediaButtons.getOrNull(0)
        val play = mediaButtons.getOrNull(1)
        val notificationMediaButtons = if (previous != null && play != null) {
            /* Overriding received mediaButtons list to ensure required buttons order: [rewind15, play/pause, forward15]. */
            ImmutableList.builder<CommandButton>().apply {
                add(previous)
                add(play)
                add(NotificationPlayerCustomCommandButton.NEXT.commandButton)
            }.build()
        } else {
            /* Fallback option to handle nullability, in case retrieving default play/pause button fails for some reason (should never happen). */
            mediaButtons

        }
        super.addNotificationActions(
            mediaSession,
            notificationMediaButtons,
            builder,
            actionFactory)
        return intArrayOf(1,2)
    }
}
