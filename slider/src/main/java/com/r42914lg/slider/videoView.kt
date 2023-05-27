package com.r42914lg.slider

import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.util.MimeTypes

@Composable
fun VideoPlayer(
    uri: Uri,
    page: Int,
    onPlayerCreated: (page: Int, exoPlayer: ExoPlayer) -> Unit,
) {
    val ctx = LocalContext.current

    val mediaItem = MediaItem.Builder()
        .setUri(uri)
        .setMimeType(MimeTypes.APPLICATION_MP4)
        .build()

    val exoPlayer = remember(uri) {
        ExoPlayer.Builder(ctx).build().apply {
            setMediaItem(mediaItem)
            playWhenReady = false
            seekTo(0, 0L)
            prepare()
            onPlayerCreated(page, this)
        }
    }

    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)

    DisposableEffect(
        AndroidView(factory = {
            StyledPlayerView(it).apply {
                player = exoPlayer
            }
        }),
    ) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    exoPlayer.pause()
                }
                Lifecycle.Event.ON_RESUME -> {
                    exoPlayer.play()
                }
                else -> {}
            }
        }
        val lifecycle = lifecycleOwner.value.lifecycle
        lifecycle.addObserver(observer)

        onDispose {
            exoPlayer.stop()
            exoPlayer.release()
        }
    }
}
