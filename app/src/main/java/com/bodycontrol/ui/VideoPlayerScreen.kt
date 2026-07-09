package com.bodycontrol.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.bodycontrol.data.CustomItem
import com.bodycontrol.data.PracticeRepository

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen(item: CustomItem, onBack: () -> Unit) {
    val context = LocalContext.current

    val exoPlayer = remember(item.id) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.fromFile(item.file(context))))
            prepare()
            playWhenReady = true
        }
    }
    DisposableEffect(item.id) {
        onDispose { exoPlayer.release() }
    }
    LaunchedEffect(item.id) {
        PracticeRepository.logPractice(context, item.id, item.title, item.category.ifBlank { "自定义" })
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                }
            },
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            Modifier
                .statusBarsPadding()
                .padding(12.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Color.White)
        }
    }
}
