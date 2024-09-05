import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.example.buddybeat.R // Ensure this matches your actual package structure

@Composable
fun HelpScreen(onNavigateUp: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        // Top Row with Navigation and Help Icon
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
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
            IconButton(onClick = { }) {
                Icon(
                    painter = painterResource(id = R.drawable.help),
                    contentDescription = "Help",
                    tint = Color.DarkGray
                )
            }
        }

        // Introduction to the App
        Text(
            text = "Help & Instructions",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Welcome to BuddyBeat! This app moves with you, adjusting the speed of your music to match your workout intensity. This short tutorial will guide you through the basic features.",
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Explanation of the BPM
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE6E8E5)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "BPM (Beats Per Minute)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "BPM represents the tempo of the song. Higher BPM means a faster song, and lower BPM means a slower one. You can adjust the BPM to match the pace of your workout, or it will be done automatically for you.",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // Explanation of the + and - buttons
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE6E8E5)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "+ and - Buttons",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Use the '+' button to increase the BPM of the current song. Use the '-' button to decrease the BPM. These adjustments allow you to match the tempo of the song with your workout intensity.",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.RemoveCircleOutline,
                            contentDescription = "Minus",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = "Plus",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
        }

        // Explanation of the Manual/Auto Mode Button
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE6E8E5)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Manual/Auto Mode Buttons",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Toggle between Manual and Auto modes. In Manual mode, you control the BPM with the '+' and '-' buttons. In Auto mode, the app adjusts the BPM automatically based on your activity.",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
                // Sample Manual and Auto Buttons
                Row(
                    modifier = Modifier
                        .padding(top = 16.dp) // Increased space between the buttons
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        shape = MaterialTheme.shapes.small // Less rounded corners
                    ) {
                        Text(text = "Manual", color = Color.White)
                    }
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        shape = MaterialTheme.shapes.small // Less rounded corners
                    ) {
                        Text(text = "Auto", color = Color.White)
                    }
                }
            }
        }

        // Back button to navigate away from the help screen
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Button(
                onClick = onNavigateUp,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = MaterialTheme.shapes.small // Less rounded corners
            ) {
                Text(text = "Back", color = Color.White)
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun HelpScreenPreview() {
    HelpScreen(onNavigateUp = {})
}
