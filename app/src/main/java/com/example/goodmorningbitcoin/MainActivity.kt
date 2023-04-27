package com.example.goodmorningbitcoin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.goodmorningbitcoin.ui.theme.GoodMorningBitcoinTheme

class MainActivity : ComponentActivity() {
    private val channelId = "media_playback_channel"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create a notification channel (required for Android Oreo and above)
        createNotificationChannel()

        setContent {
            GoodMorningBitcoinTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Greeting("Android")

                        // Remember the state of the media player (playing or stopped)
                        val isPlaying = remember { mutableStateOf(false) }

                        Button(
                            onClick = {
                                // Toggle the isPlaying state
                                isPlaying.value = !isPlaying.value

                                val action = if (isPlaying.value) {
                                    MediaPlayerService.ACTION_PLAY
                                } else {
                                    MediaPlayerService.ACTION_STOP
                                }
                                val intent = Intent(this@MainActivity, MediaPlayerService::class.java).apply {
                                    this.action = action
                                }
                                startService(intent)
                            }
                        ) {
                            // Choose the correct icon and text based on the isPlaying state
                            val icon = if (isPlaying.value) R.drawable.ic_stop else R.drawable.ic_play_arrow
                            val text = if (isPlaying.value) "Stop" else "Play"

                            Icon(painter = painterResource(id = icon), contentDescription = text)
                            Text(text)
                        }
                    }
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GoodMorningBitcoinTheme {
        Greeting("Android")
    }
}
