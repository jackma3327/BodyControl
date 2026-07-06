package com.bodycontrol.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bodycontrol.data.Catalog
import com.bodycontrol.data.Category
import com.bodycontrol.data.TrackItem
import com.bodycontrol.player.PlayerController

/* ---------- 分类视觉映射 ---------- */

private data class CategoryTheme(val icon: ImageVector, val colors: List<Color>)

private fun themeFor(id: String): CategoryTheme = when (id) {
    "yoga" -> CategoryTheme(Icons.Filled.SelfImprovement, listOf(Color(0xFF34D399), Color(0xFF0E9F7E)))
    "qigong" -> CategoryTheme(Icons.Filled.Spa, listOf(Color(0xFFA78BFA), Color(0xFF7C3AED)))
    "breath" -> CategoryTheme(Icons.Filled.Air, listOf(Color(0xFF38BDF8), Color(0xFF2563EB)))
    else -> CategoryTheme(Icons.Filled.FitnessCenter, listOf(Color(0xFFFBBF24), Color(0xFFF97316)))
}

/* ---------- 顶层 ---------- */

@Composable
fun App() {
    var selected by remember { mutableStateOf<Category?>(null) }
    val playerState by PlayerController.state.collectAsStateWithLifecycle()
    val playing = playerState.trackId != null
    val bottomInset = if (playing) 96.dp else 0.dp

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val current = selected
        if (current == null) {
            HomeScreen(bottomInset = bottomInset, onSelect = { selected = it })
        } else {
            DetailScreen(
                category = current,
                playingId = playerState.trackId,
                isPlaying = playerState.isPlaying,
                bottomInset = bottomInset,
                onBack = { selected = null },
            )
        }

        if (playing) {
            MiniPlayer(
                title = playerState.title,
                isPlaying = playerState.isPlaying,
                onToggle = { PlayerController.togglePlayPause() },
                onStop = { PlayerController.stop() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }
    }
}

/* ---------- 首页 ---------- */

@Composable
private fun HomeScreen(bottomInset: androidx.compose.ui.unit.Dp, onSelect: (Category) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp + bottomInset),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(Modifier.padding(top = 20.dp, bottom = 6.dp)) {
                Text(
                    "你好",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "身体训练",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    "选择今天的练习",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        items(Catalog.categories, key = { it.id }) { category ->
            CategoryCard(category = category, onClick = { onSelect(category) })
        }
    }
}

@Composable
private fun CategoryCard(category: Category, onClick: () -> Unit) {
    val theme = themeFor(category.id)
    Box(
        Modifier
            .fillMaxWidth()
            .height(168.dp)
            .shadow(10.dp, RoundedCornerShape(24.dp), spotColor = theme.colors.last())
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(theme.colors))
            .clickable(onClick = onClick)
            .padding(18.dp),
    ) {
        Box(
            Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(theme.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
        }
        Column(Modifier.align(Alignment.BottomStart)) {
            Text(
                category.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Text(
                category.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.9f),
                maxLines = 2,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        CountChip(
            text = "${category.items.size} 项",
            modifier = Modifier.align(Alignment.TopEnd),
        )
    }
}

@Composable
private fun CountChip(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.25f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}

/* ---------- 分类详情 ---------- */

@Composable
private fun DetailScreen(
    category: Category,
    playingId: String?,
    isPlaying: Boolean,
    bottomInset: androidx.compose.ui.unit.Dp,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp + bottomInset),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { DetailHeader(category = category, onBack = onBack) }
        items(category.items, key = { it.id }) { item ->
            TrackRow(
                item = item,
                categoryColors = themeFor(category.id).colors,
                fallbackIcon = themeFor(category.id).icon,
                isCurrent = item.id == playingId,
                isPlaying = isPlaying && item.id == playingId,
                onClick = { if (item.rawResId != null) PlayerController.play(context, item) },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

@Composable
private fun DetailHeader(category: Category, onBack: () -> Unit) {
    val theme = themeFor(category.id)
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(Brush.linearGradient(theme.colors))
            .statusBarsPadding()
            .padding(20.dp),
    ) {
        Column {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Color.White)
            }
            Row(
                Modifier.padding(top = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(theme.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(38.dp))
                }
                Column(Modifier.padding(start = 16.dp)) {
                    Text(
                        category.title,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                    )
                    Text(
                        category.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackRow(
    item: TrackItem,
    categoryColors: List<Color>,
    fallbackIcon: ImageVector,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasAudio = item.rawResId != null
    val container = if (isCurrent) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
    Row(
        modifier
            .fillMaxWidth()
            .shadow(if (isCurrent) 8.dp else 3.dp, RoundedCornerShape(20.dp), spotColor = categoryColors.last())
            .clip(RoundedCornerShape(20.dp))
            .background(container)
            .then(if (hasAudio) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 圆形徽标
        Box(
            Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    if (hasAudio) Brush.linearGradient(categoryColors)
                    else Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surfaceVariant,
                        )
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            when {
                isPlaying -> EqualizerBars(color = Color.White)
                hasAudio -> Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                else -> Icon(fallbackIcon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(26.dp))
            }
        }
        Column(Modifier.weight(1f).padding(start = 14.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(
                item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 3.dp),
            )
            if (!hasAudio) {
                Box(
                    Modifier
                        .padding(top = 6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text("引导练习", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (hasAudio && isCurrent) {
            Icon(
                if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = categoryColors.last(),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

/* ---------- 播放中动效 ---------- */

@Composable
private fun EqualizerBars(color: Color) {
    val transition = rememberInfiniteTransition(label = "eq")
    val heights = (0..2).map { i ->
        transition.animateFloat(
            initialValue = 0.35f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 480 + i * 120),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "bar$i",
        )
    }
    Row(
        Modifier.height(22.dp).width(24.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        heights.forEach { h ->
            Box(
                Modifier
                    .width(4.dp)
                    .fillMaxHeight(h.value)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color),
            )
        }
    }
}

/* ---------- 迷你播放器 ---------- */

@Composable
private fun MiniPlayer(
    title: String,
    isPlaying: Boolean,
    onToggle: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth().shadow(16.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF34D399), Color(0xFF0E9F7E)))),
                contentAlignment = Alignment.Center,
            ) {
                if (isPlaying) EqualizerBars(color = Color.White)
                else Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text("正在播放", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
            }
            IconButton(onToggle) {
                Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = if (isPlaying) "暂停" else "播放", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
            }
            IconButton(onStop) {
                Icon(Icons.Filled.Close, contentDescription = "关闭", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun IconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        Modifier.size(44.dp).clip(CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}
