package com.bodycontrol.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bodycontrol.data.Catalog
import com.bodycontrol.data.Category
import com.bodycontrol.data.TrackItem
import com.bodycontrol.player.PlayerController

private fun iconFor(key: String): ImageVector = when (key) {
    "yoga" -> Icons.Filled.SelfImprovement
    "qigong" -> Icons.Filled.Spa
    "breath" -> Icons.Filled.Air
    else -> Icons.Filled.FitnessCenter
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    var selected by remember { mutableStateOf<Category?>(null) }
    val playerState by PlayerController.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selected?.title ?: "身体训练") },
                navigationIcon = {
                    if (selected != null) {
                        IconButton(onClick = { selected = null }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (playerState.trackId != null) {
                MiniPlayer(
                    title = playerState.title,
                    isPlaying = playerState.isPlaying,
                    onToggle = { PlayerController.togglePlayPause() },
                    onStop = { PlayerController.stop() },
                )
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            val current = selected
            if (current == null) {
                CategoryGrid(onSelect = { selected = it })
            } else {
                CategoryDetail(category = current, playingId = playerState.trackId, isPlaying = playerState.isPlaying)
            }
        }
    }
}

@Composable
private fun CategoryGrid(onSelect: (Category) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(Catalog.categories, key = { it.id }) { category ->
            Card(
                onClick = { onSelect(category) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            ) {
                Column(Modifier.padding(20.dp)) {
                    Icon(
                        iconFor(category.iconKey),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        category.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                    Text(
                        category.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Text(
                        "${category.items.size} 项",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryDetail(category: Category, playingId: String?, isPlaying: Boolean) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(category.items, key = { it.id }) { item ->
            TrackRow(
                item = item,
                isCurrent = item.id == playingId,
                isPlaying = isPlaying && item.id == playingId,
                onClick = { if (item.rawResId != null) PlayerController.play(context, item) },
            )
        }
    }
}

@Composable
private fun TrackRow(item: TrackItem, isCurrent: Boolean, isPlaying: Boolean, onClick: () -> Unit) {
    val hasAudio = item.rawResId != null
    Card(
        modifier = Modifier.fillMaxWidth().let { if (hasAudio) it.clickable(onClick = onClick) else it },
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
                if (!hasAudio) {
                    Text(
                        "暂无音频 · 引导练习",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
            val icon = when {
                !hasAudio -> Icons.Filled.MusicOff
                isPlaying -> Icons.Filled.Pause
                else -> Icons.Filled.PlayArrow
            }
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp).padding(start = 12.dp),
                tint = if (hasAudio) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Composable
private fun MiniPlayer(title: String, isPlaying: Boolean, onToggle: () -> Unit, onStop: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.primaryContainer, tonalElevation = 3.dp) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
            )
            IconButton(onClick = onToggle) {
                Icon(
                    if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "暂停" else "播放",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            IconButton(onClick = onStop) {
                Icon(
                    Icons.Filled.MusicOff,
                    contentDescription = "停止",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}
