package com.example.buddybeat.ui.screens

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.media3.common.util.UnstableApi
import com.example.buddybeat.R
import com.example.buddybeat.player.PlaybackService.Companion.AUTO_MODE
import com.example.buddybeat.player.PlaybackService.Companion.MANUAL_MODE
import com.example.buddybeat.player.PlaybackService.Companion.OFF_MODE
import com.example.buddybeat.ui.CurrentSong
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/*PLAY SCREEN*/
@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayScreenDesign(
    onNavigateUp: () -> Unit,
    song: CurrentSong,
    duration: Long,
    isPlaying: Boolean,
    progress: Float,
    onProgress: (Float) -> Unit,
    onStart: () -> Unit,
    nextSong: () -> Unit,
    prevSong: () -> Unit,
    toggleMode: () -> Unit,
    plus: () -> Unit,
    minus: () -> Unit,
    bpm: String,
    queue: () -> Unit,
    target: String,
    modality: Long,
    addToFavorite: (Long) -> Unit,
    removeFavorite: (Long) -> Unit,
    favoriteContainsSong: (Long) -> LiveData<Int>,
    colorUI: Color,
    shouldShowHelpScreen: () -> Unit
) {
    //text button auto/manual/off
    val text = when (modality) {
        OFF_MODE -> "off"
        AUTO_MODE -> "auto"
        MANUAL_MODE -> "manual"
        else -> "error"
    }
    val isFavorite by favoriteContainsSong(song.id).observeAsState(initial = 0)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)

    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight(0.6f)
                .fillMaxWidth()
        ) {
            //cover picture
            CoverPicture(song = song)
            //top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onNavigateUp() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIos,
                        contentDescription = "back",
                    )
                }
                IconButton(onClick = { shouldShowHelpScreen() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.help),
                        contentDescription = "help",
                    )
                }
            }
            //title - artist
            Column(
                modifier = Modifier
                    .padding(bottom = 150.dp)
                    .padding(horizontal = 70.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Text(
                    text = song.title,
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp),
                    textAlign = TextAlign.Center,
                    color = Color(0xFFFDFEFF),
                    fontSize = 25.sp,
                    fontWeight = FontWeight.W900,
                    style = TextStyle(
                        fontSize = 24.sp,
                        shadow = Shadow(
                            color = Color.DarkGray, offset = Offset(5f, 5f), blurRadius = 2f
                        )
                    )
                )
                Text(
                    text = song.artist,
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp),
                    textAlign = TextAlign.Center, color = Color(0xFFFDFEFF), fontSize = 20.sp,
                    style = TextStyle(
                        fontSize = 24.sp,
                        shadow = Shadow(
                            color = Color.DarkGray, offset = Offset(5.0f, 5.0f), blurRadius = 1f
                        )
                    )
                )
            }
            //slider
            CircularSlider(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(350.dp),
                progress = progress,
                onChange = {
                    onProgress(it * 100)
                }
            )
        }
        //progress
        Text(
            text = if (progress * duration <= duration)
                formatSecondsToDuration((progress * duration).toLong()) else "00:00",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center, color = Color.Gray, fontSize = 19.sp
        )
        // BUTTONS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 25.dp, start = 7.dp, end = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween, // Distribute items across the row
            verticalAlignment = Alignment.CenterVertically // Align items vertically centered
        ) {
            // Left side - BPM box
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = colorUI
                ),
                modifier = Modifier
                    .padding(horizontal = 5.dp)
                    .size(width = 70.dp, height = 50.dp),
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (bpm != "0" && bpm != "-1") bpm else "-",
                    )
                }
            }
            // Center - Plus and Minus buttons + targetBpm
            val enabled = modality == MANUAL_MODE
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = {
                    minus()
                }, enabled = enabled) {
                    Icon(
                        imageVector = Icons.Default.RemoveCircleOutline,
                        contentDescription = "minus",
                        modifier = Modifier.size(30.dp)
                    )
                }
                //targetBpm
                NewButton2(name = target, enabled = enabled, colorUI = colorUI)
                IconButton(onClick = {
                    plus()
                }, enabled = enabled) {
                    Icon(
                        imageVector = Icons.Default.AddCircleOutline,
                        contentDescription = "plus",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
            // Right side - Manual and Auto buttons
            NewButton(
                name = text, onClick = {
                    toggleMode()
                }, colorUI = colorUI
            )
        }
        // Play ROW
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            //Favorite button
            IconButton(onClick = {
                if (isFavorite == 0) {
                    addToFavorite(song.id)
                } else {
                    removeFavorite(song.id)
                }
            }) {
                Icon(
                    imageVector = if (isFavorite != 0) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "favorite",
                    modifier = Modifier.size(30.dp),
                    tint = Color.Black
                )
            }
            // next - prev
            Box(
                modifier = Modifier
                    .width(150.dp)
                    .fillMaxHeight(), contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorUI, RoundedCornerShape(40.dp)),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { prevSong() }) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "previous",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = { nextSong() }) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "next",
                            modifier = Modifier
                                .size(20.dp)
                        )
                    }
                }
                //play button
                Card(
                    onClick = {
                        onStart()
                    },
                    shape = CircleShape,
                    modifier = Modifier.size(70.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "play_pause",
                            modifier = Modifier.size(35.dp), tint = Color.White
                        )
                    }
                }
            }
            //queue button
            IconButton(onClick = { queue() }) {
                Icon(
                    imageVector = Icons.Default.QueueMusic,
                    contentDescription = "queue",
                    modifier = Modifier.size(30.dp),
                    tint = Color.Black
                )
            }

        }

    }
}


@Composable
fun CoverPicture(
    song: CurrentSong
) {
    //retrieve cover picture, if any
    var art: Bitmap? = BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.bblogo)
    var bitmap = art?.asImageBitmap()
    val mmr = MediaMetadataRetriever()
    val bfo = BitmapFactory.Options()
    if (song.uri != "") {
        mmr.setDataSource(LocalContext.current, song.uri.toUri())
    }
    val rawArt: ByteArray? = mmr.embeddedPicture
    if (null != rawArt)
        art = BitmapFactory.decodeByteArray(rawArt, 0, rawArt.size, bfo)
    if (art != null) {
        bitmap = art.asImageBitmap()
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = "coverPicture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxHeight(0.85f)
                .padding(horizontal = 70.dp)
                .clip(RoundedCornerShape(bottomStart = 150.dp, bottomEnd = 150.dp)),
            colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0.5f) })
        )
    } else
        Image(
            painter = painterResource(id = R.drawable.scaled),
            contentDescription = "defaultCoverPicture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxHeight(0.85f)
                .padding(horizontal = 70.dp)
                .clip(RoundedCornerShape(bottomStart = 150.dp, bottomEnd = 150.dp))
        )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CircularSlider(
    modifier: Modifier = Modifier,
    padding: Float = 15f,
    stroke: Float = 15f,
    cap: StrokeCap = StrokeCap.Round,
    touchStroke: Float = 90f,
    progress: Float = 0f,
    onChange: ((Float) -> Unit)? = null
) {
    var width by remember { mutableStateOf(0) }
    var height by remember { mutableStateOf(0) }
    var angle by remember { mutableStateOf(180f) } // Start at the bottom left
    var nearTheThumbIndicator by remember { mutableStateOf(false) }
    val progressColor by remember { mutableStateOf(Color.Black) }
    val gradient by remember { mutableStateOf(Color(0xFFD2D2D2)) }
    var radius by remember { mutableStateOf(0f) }
    var center by remember { mutableStateOf(Offset.Zero) }
    var appliedAngle by remember { mutableStateOf(180f) }

    LaunchedEffect(key1 = progress) {
        appliedAngle = 180f * (1 - progress) // Dynamically update the angle based on progress
    }
    Canvas(
        modifier = modifier
            .onGloballyPositioned {
                width = it.size.width
                height = it.size.height
                center = Offset(width / 2f, height / 2f)
                radius = min(width.toFloat(), height.toFloat()) / 2f - padding - stroke / 2f
            }
            .pointerInteropFilter {
                val x = it.x
                val y = it.y
                val offset = Offset(x, y)
                when (it.action) {
                    MotionEvent.ACTION_DOWN -> {
                        val d = distance(offset, center)
                        val a = angle(center, offset)
                        if (d >= radius - touchStroke / 2f && d <= radius + touchStroke / 2f && a in 0f..180f) {
                            nearTheThumbIndicator = true
                            angle = a
                            val newProgress = 1 - (angle / 180f)
                            onChange?.invoke(newProgress)
                        } else {
                            nearTheThumbIndicator = false
                        }
                    }

                    MotionEvent.ACTION_MOVE -> {
                        if (nearTheThumbIndicator) {
                            val a = angle(center, offset)
                            if (a in 0f..180f) {
                                angle = a
                                val newProgress = 1 - (angle / 180f)
                                onChange?.invoke(newProgress)
                            }
                        }
                    }

                    MotionEvent.ACTION_UP -> {
                        nearTheThumbIndicator = false
                    }

                    else -> return@pointerInteropFilter false
                }
                return@pointerInteropFilter true
            }
    ) {
        // Draw the background arc
        drawArc(
            color = gradient,
            startAngle = 180f,
            sweepAngle = -180f,
            topLeft = center - Offset(radius, radius),
            size = Size(radius * 2, radius * 2),
            useCenter = false,
            style = Stroke(
                width = stroke,
                cap = cap
            )
        )

        // Draw the progress arc
        drawArc(
            color = progressColor,
            startAngle = 180f,
            sweepAngle = -(180f - appliedAngle), // Progress arc based on the com.example.buddybeat.ui.screens.angle
            topLeft = center - Offset(radius, radius),
            size = Size(radius * 2, radius * 2),
            useCenter = false,
            style = Stroke(
                width = stroke,
                cap = cap
            )
        )

        // Draw the thumb indicator
        drawCircle(
            color = Color.Black,
            radius = 32f,
            center = center + Offset(
                radius * cos((appliedAngle) * PI / 180f).toFloat(),
                radius * sin((appliedAngle) * PI / 180f).toFloat()
            )
        )
    }
}

fun angle(center: Offset, offset: Offset): Float {
    val rad = atan2(offset.y - center.y, offset.x - center.x)
    val deg = Math.toDegrees(rad.toDouble())
    return if (deg < 0) 360f + deg.toFloat() else deg.toFloat()
}

fun distance(first: Offset, second: Offset): Float {
    return sqrt((first.x - second.x).square() + (first.y - second.y).square())
}

fun Float.square(): Float {
    return this * this
}

fun formatSecondsToDuration(milliseconds: Long): String {
    val seconds: Int = milliseconds.toInt() / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewButton(
    name: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    colorUI: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = colorUI,
            disabledContainerColor = colorUI.copy(alpha = 0.3f)
        ),
        modifier = Modifier
            .padding(horizontal = 5.dp)
            .size(width = 70.dp, height = 50.dp)
            .shadow(
                ambientColor = colorUI.copy(alpha = 0.3f),
                spotColor = colorUI.copy(alpha = 0.3f),
                elevation = 2.dp,
                shape = CardDefaults.elevatedShape
            )
            .background(colorUI.copy(alpha = 0.3f)),
        onClick = onClick,
        enabled = enabled
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = name,
            )
        }
    }
}

@Composable
fun NewButton2(
    name: String,
    enabled: Boolean = true,
    colorUI: Color
) {
    val color = if (enabled) colorUI else colorUI.copy(alpha = 0.3f)
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color,
            disabledContainerColor = color.copy(alpha = 0.3f)
        ),
        modifier = Modifier
            .padding(horizontal = 5.dp)
            .size(width = 70.dp, height = 50.dp)
            .shadow(
                ambientColor = color,
                spotColor = color,
                elevation = 2.dp,
                shape = CardDefaults.elevatedShape
            )
            .background(color),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = name,
                color = if (enabled) Color.Black else Color.Black.copy(alpha = 0.5f)
            )
        }
    }
}