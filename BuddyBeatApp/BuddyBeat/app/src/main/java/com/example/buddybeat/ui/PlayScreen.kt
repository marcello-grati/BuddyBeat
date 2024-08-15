package com.example.buddybeat.ui

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.buddybeat.R
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.buddybeat.ui.components.FilledCardExample
import kotlinx.coroutines.delay
import java.io.File
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayScreenDesign(
    filePath: String = "",
    duration: String = "05:00",
    songName: String = "Feel Good Inc",
    artist: String = "Gorillaz"
) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(0f) }
    val durationInSeconds = durationToSeconds(duration)

    LaunchedEffect(isPlaying, currentTime) {
        if (isPlaying) {
            while (currentTime < durationInSeconds) {
                delay(1000L)
                currentTime += 1f
            }
            isPlaying = false
        }
    }

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
            AsyncImage(
                model = File(filePath),
                placeholder = painterResource(id = R.drawable.scaled),
                error = painterResource(id = R.drawable.scaled),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxHeight(0.9f)
                    .padding(horizontal = 70.dp)
                    .clip(RoundedCornerShape(bottomStart = 150.dp, bottomEnd = 150.dp))
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIos,
                        contentDescription = "",
                    )
                }
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "",
                    )
                }
            }
            Column(
                modifier = Modifier
                    .padding(bottom = 100.dp)
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                Text(
                    text = songName,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.W900
                )
                Text(
                    text = artist,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center, color = Color.White, fontSize = 16.sp
                )
            }

            CircularSlider(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(350.dp),
                progress = currentTime / durationInSeconds
            ) { newProgress ->
                currentTime = newProgress * durationInSeconds
                if (currentTime == 0f) {
                    isPlaying = false
                }
            }
            Row(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.RemoveCircleOutline,
                        contentDescription = "", modifier = Modifier.size(30.dp)
                    )
                }
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.AddCircleOutline,
                        contentDescription = "", modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
        Text(
            text = formatSecondsToDuration(currentTime.toInt()),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center, color = Color.Gray, fontSize = 19.sp
        )


        // BUTTONS

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 25.dp, start = 7.dp, end = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                newButton(name = "Step")
                newButton(name = "bpm")
                newButton(name = "ratio")
            }
            newButton(name = "auto")
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
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = ""
                )
            }
            Box(
                modifier = Modifier
                    .width(150.dp)
                    .fillMaxHeight(), contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF91E4EC), RoundedCornerShape(40.dp)),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.FastRewind,
                            contentDescription = "",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.FastRewind,
                            contentDescription = "",
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(180f)
                        )
                    }
                }
                Card(
                    onClick = {
                        isPlaying = !isPlaying
                    },
                    shape = CircleShape,
                    modifier = Modifier.size(70.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "",
                            modifier = Modifier.size(35.dp), tint = Color.White
                        )
                    }
                }
            }
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = ""
                )
            }
        }

    }
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
        Log.d("CHKPRO", "${appliedAngle} angle and progress : ${progress}")
        if (progress >= 0.998f) {
            appliedAngle = 180f
            onChange?.invoke(0f)
        }
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
                            if (newProgress >= 0.997f) {
                                appliedAngle = 180f
                                onChange?.invoke(0f)
                            } else {
                                onChange?.invoke(newProgress)
                            }
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
                                if (newProgress >= 0.997f) {
                                    appliedAngle = 180f
                                    onChange?.invoke(0f)
                                } else {
                                    onChange?.invoke(newProgress)
                                }
                            }
                        }
                    }

                    MotionEvent.ACTION_UP -> {
                        nearTheThumbIndicator = false
                        Log.d(
                            "CHKPRO",
                            "From MOTION ${appliedAngle} angle and progress : ${progress}"
                        )

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
            sweepAngle = -(180f - appliedAngle), // Progress arc based on the angle
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


fun durationToSeconds(duration: String): Float {
    val parts = duration.split(":")
    Log.d("CHKTIME", "${(parts[0].toFloat() * 60) + parts[1].toFloat()}")
    return (parts[0].toFloat() * 60) + parts[1].toFloat()
}

fun formatSecondsToDuration(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

@Composable
fun newButton(name: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier
            .padding(horizontal = 5.dp)
            .size(width = 70.dp, height = 50.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = name
            )
        }
    }
}
