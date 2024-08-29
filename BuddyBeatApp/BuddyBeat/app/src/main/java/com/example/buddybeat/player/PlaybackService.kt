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
import com.example.buddybeat.SensorService
import com.example.buddybeat.data.models.Song
import com.example.buddybeat.data.repository.AudioRepository
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log2
import kotlin.math.min


@UnstableApi @AndroidEntryPoint
class PlaybackService : MediaSessionService(), MediaSession.Callback{

    companion object {
        //var playlist: MutableMap<String, MediaItem> = mutableMapOf()
        var playlist : MutableList<String> = mutableListOf() //already played
        var audiolist : MutableStateFlow<MutableList<Song>> = MutableStateFlow(mutableListOf()) //list of possible songs
        var audioListId = MutableStateFlow(0L)
        var queue : MutableList<Song> = mutableListOf()

        val AUTO_MODE = 0
        val MANUAL_MODE = 1
        val OFF_MODE = 2
        val DEFAULT_BPM = 100
        val ALPHA = 0.9f
        val BPM_STEP = 2

        var speedMode = AUTO_MODE
        var manualBpm = DEFAULT_BPM
        var ratio = 1f
    }

    @Inject
    lateinit var customMediaNotificationProvider : CustomMediaNotificationProvider

    @Inject
    lateinit var songRepo: AudioRepository

    private var mediaSession: MediaSession? = null

    private lateinit var mService: SensorService
    private var mBound: Boolean = false



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
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.putExtra("DESTINATION", "player")
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT)

        mediaSession = MediaSession.Builder(this, player).setCallback(this).setSessionActivity(pendingIntent)
            /*.setSessionActivity(PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE))*/
            .build()

        mediaSession!!.setCustomLayout(notificationPlayerCustomCommandButtons)
        Intent(this, SensorService::class.java).also { int ->
            bindService(int, connection, Context.BIND_AUTO_CREATE)
        }
        handler.postDelayed(sensorDataRunnable, 30000)
        handler.postDelayed(orderSongsRunnable, 30000)
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
        if(playlist.size > ((audiolist.value.size.div(2))))
            playlist.removeFirstOrNull()
        Log.d("onAddMediaItems", playlist.toString())
        return Futures.immediateFuture(updatedMediaItems)
    }


    private fun onAddSong(media : MediaItem){
        playlist.add(media.mediaId)
        if(playlist.size > (audiolist.value.size.div(2)))
            playlist.removeFirstOrNull()
        Log.d("onAddSong", playlist.toString())
    }

    private val sensorDataRunnable = object : Runnable {
        override fun run() {
            if (mBound) {
                updateSpeedSong()
                handler.postDelayed(this, interval)
            }
        }
    }

//    private fun updateSpeedSong() {
//        val stepFreq = mService.stepFreq
//        Log.d("StepCadence from PlaybackService", stepFreq.toString())
//        var bpm = mediaSession?.player?.mediaMetadata?.extras?.getInt("bpm")
//        var rat = 1f
//        if (bpm != 0 && bpm != null && bpm!=-1) {
//
//            // We compute the log_2() of step frequency and of double, half and original value of BPM
//            val logStepFreq = log2(stepFreq.toFloat())
//            val logBpm = log2(bpm.toFloat())
//            val logHalfBPM = log2(bpm.toFloat() / 2.0f)
//            val logDoubleBPM = log2(bpm.toFloat() * 2.0f)
//
//            // We update BPM if one of its multiples has a smaller distance value
//            if (abs(logStepFreq - logBpm) > abs(logStepFreq - logHalfBPM)) {
//                bpm /= 2
//            } else if (abs(logStepFreq - logBpm) > abs(logStepFreq - logDoubleBPM)) {
//                bpm *= 2
//            }
//            // Speed-up ratio computed as step frequency / BPM
//            rat = stepFreq.toFloat() / bpm.toFloat()
//        }
//        if (stepFreq < 60)
//            rat = 1f
//
//        // ratio forced into [0.8, 1.2] range
//        rat = rat.coerceAtLeast(0.8f)
//        rat = rat.coerceAtMost(1.2f)
//
//        ratio = rat
//
//        Log.d("RATIO_3", ratio.toString())
//
//        mediaSession?.player?.setPlaybackSpeed(ratio)
//    }

    private fun updateSpeedSong() {

        if (speedMode != OFF_MODE) {

            var bpm = mediaSession?.player?.mediaMetadata?.extras?.getInt("bpm")

            if (bpm != 0 && bpm != null && bpm != -1) {

                val stepFreq = when (speedMode) {
                    AUTO_MODE -> mService.stepFreq.toFloat()
                    MANUAL_MODE -> manualBpm.toFloat()
                    else -> throw Exception("Invalid speed mode")
                }

                var inRatio: Float
                var outRatio = ratio


                // We compute the log_2() of step frequency and of double, half and original value of BPM
                val logStepFreq = log2(stepFreq)
                var logBpm = log2(bpm.toFloat())
                var logHalfBPM = log2(bpm.toFloat() / 2.0f)
                var logDoubleBPM = log2(bpm.toFloat() * 2.0f)

                // We update BPM if one of its multiples has a smaller distance value
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

                if (stepFreq < 60)
                    inRatio = 1f

                // ratio forced into [0.8, 1.2] range
                inRatio = inRatio.coerceAtLeast(0.8f)
                inRatio = inRatio.coerceAtMost(1.25f)

                outRatio = ALPHA * outRatio + (1 - ALPHA) * inRatio

                ratio = outRatio
            }
        } else {
            ratio = 1f
        }
        mediaSession?.player?.setPlaybackSpeed(ratio)
    }


    private val orderSongsRunnable = object : Runnable {
        override fun run() {
            if (mBound) {
                val pos = mediaSession?.player?.currentPosition
                val dur = mediaSession?.player?.duration
                if(pos!=null && dur!=null && dur>0) {
                    Log.d("pos", pos.toString())
                    Log.d("dur", dur.toString())
                    if (pos >= dur - min(5000.0, 0.9 * dur) && pos <= dur) {
                        nextSong()
                    }
                }
            }
            handler.postDelayed(this, interval*6)
        }
    }

    private fun nextSong() {
        val queueNext = queue.removeFirstOrNull()
        if(queueNext!=null){
            val media = buildMediaItem(queueNext)
            setSongInPlaylist(media)
            return
        }
        val l = orderSongs()
        l.forEach {
            Log.d(it.toString(), it.bpm.toString())
        }
        while(true){
            val nextSong = l.removeFirstOrNull()
            if(nextSong != null) {
                val media = buildMediaItem(nextSong)
                Log.d("IOOOO","From Playback service, next song ${media.mediaId}?")
                if (!playlist.contains(media.mediaId)) {
                    Log.d("IOOOO","From Playback service, it does not contain ${media.mediaId}")
                    setSongInPlaylist(media)
                    //mediaSession?.player?.seekToDefaultPosition(mediaSession?.player!!.currentMediaItemIndex + 1)
                    break
                }

            }
        }
    }

    private fun setSongInPlaylist(media: MediaItem){
        Log.d("IOOOO", "setting media: $media + id : ${media.mediaId} + title: ${media.mediaMetadata.title}")
        Log.d("IOOOO", "currentMediaItemIndex + currentMediaItem" +mediaSession?.player?.currentMediaItemIndex.toString() + "   " + mediaSession?.player?.currentMediaItem.toString())
        mediaSession?.player!!.addMediaItem(mediaSession?.player!!.currentMediaItemIndex+1,media)
        onAddSong(media)
        Log.d("IOOOO", "currentMediaItemIndex + currentMediaItem" +mediaSession?.player?.currentMediaItemIndex.toString() + "   " + mediaSession?.player?.currentMediaItem.toString())
        Log.d("IOOOO", "mediaItemCount: " + mediaSession?.player?.mediaItemCount.toString())
    }
    fun orderSongs(): MutableList<Song> {

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
        val list = audiolist.value.toMutableList()
        return list.sortedWith(myCustomComparator).toMutableList()
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

    /*private val notificationPlayerCustomCommandButtons =
        NotificationPlayerCustomCommandButton.entries.map { command -> command.commandButton }*/

    /*override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        if (session.isMediaNotificationController(controller)) {
            val sessionCommands =
                notificationPlayerCustomCommandButtons[0].sessionCommand.let {
                    val m = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                    if(it!=null)
                        m.add(it)
                    m.build()
                }
            val playerCommands =
                MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS.buildUpon()
                    .remove(COMMAND_SEEK_TO_PREVIOUS)
                    .remove(COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                    .remove(COMMAND_SEEK_TO_NEXT)
                    .remove(COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                    .build()
            // Custom layout and available commands to configure the legacy/framework session.
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setCustomLayout(
                    notificationPlayerCustomCommandButtons
                )
                .setAvailablePlayerCommands(playerCommands)
                .setAvailableSessionCommands(sessionCommands)
                .build()
        }
        // Default commands with default custom layout for all other controllers.
        return MediaSession.ConnectionResult.AcceptedResultBuilder(session).build()
    }*/

    private val notificationPlayerCustomCommandButtons =
        NotificationPlayerCustomCommandButton.values().map { command -> command.commandButton }

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
