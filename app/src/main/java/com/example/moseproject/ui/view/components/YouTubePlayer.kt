package com.example.moseproject.ui.view.components

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions

@Composable
fun VideoPlayerExo(videoUrl: String, lifecycleOwner: LifecycleOwner) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var playbackFailed by remember(videoUrl) { mutableStateOf(false) }

    Column {
        key(videoUrl, lifecycleOwner) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(16.dp)),
                factory = {
                    YouTubePlayerView(context).apply {
                        enableAutomaticInitialization = false
                        lifecycleOwner.lifecycle.addObserver(this)
                        // origin also sets the WebView base URL and HTTP Referer: identify this app.
                        val options = IFramePlayerOptions.Builder()
                            .origin("https://${context.packageName}")
                            .controls(1)
                            .build()
                        initialize(object : AbstractYouTubePlayerListener() {
                            override fun onStateChange(
                                youTubePlayer: YouTubePlayer,
                                state: PlayerConstants.PlayerState
                            ) {
                                if (state == PlayerConstants.PlayerState.PLAYING) {
                                    playbackFailed = false
                                }
                                Log.d("YouTubePlayer", "video=$videoUrl state=$state")
                            }

                            override fun onError(
                                youTubePlayer: YouTubePlayer,
                                error: PlayerConstants.PlayerError
                            ) {
                                playbackFailed = true
                                Log.w("YouTubePlayer", "video=$videoUrl error=$error")
                            }
                        }, true, options, videoUrl)
                    }
                },
                onReset = null,
                onRelease = { playerView ->
                    lifecycleOwner.lifecycle.removeObserver(playerView)
                    playerView.release()
                }
            )
        }
        if (playbackFailed) {
            Text(
                "YouTube no permite reproducir este vídeo aquí.",
                color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            TextButton(onClick = {
                uriHandler.openUri("https://www.youtube.com/watch?v=$videoUrl")
            }) {
                Text("Ver en YouTube")
            }
        }
    }
}
