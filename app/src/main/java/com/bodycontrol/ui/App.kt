package com.bodycontrol.ui

import android.widget.Toast
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideoLibrary
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bodycontrol.data.Catalog
import com.bodycontrol.data.Category
import com.bodycontrol.data.CustomItem
import com.bodycontrol.data.CustomRepository
import com.bodycontrol.data.FitnessCatalog
import com.bodycontrol.data.FitnessSeries
import com.bodycontrol.data.PracticeRepository
import com.bodycontrol.data.TrackItem
import com.bodycontrol.player.PlayerController

/* ---------- 分类视觉映射 ---------- */

private data class CategoryTheme(val icon: ImageVector, val colors: List<Color>)

private fun greeting(): String = when (java.time.LocalTime.now().hour) {
    in 5..10 -> "早上好"
    in 11..13 -> "中午好"
    in 14..18 -> "下午好"
    else -> "晚上好"
}

private fun themeFor(id: String): CategoryTheme = when (id) {
    "yoga" -> CategoryTheme(Icons.Filled.SelfImprovement, listOf(Color(0xFF34D399), Color(0xFF0E9F7E)))
    "qigong" -> CategoryTheme(Icons.Filled.Spa, listOf(Color(0xFFA78BFA), Color(0xFF7C3AED)))
    "breath" -> CategoryTheme(Icons.Filled.Air, listOf(Color(0xFF38BDF8), Color(0xFF2563EB)))
    else -> CategoryTheme(Icons.Filled.FitnessCenter, listOf(Color(0xFFFBBF24), Color(0xFFF97316)))
}

/* ---------- 顶层 ---------- */

private enum class Tab { Home, Custom, Mine }

@Composable
fun App() {
    var tab by remember { mutableStateOf(Tab.Home) }
    var selected by remember { mutableStateOf<Category?>(null) }
    var showFitnessList by remember { mutableStateOf(false) }
    var activeSeries by remember { mutableStateOf<FitnessSeries?>(null) }
    var customVideo by remember { mutableStateOf<CustomItem?>(null) }
    val playerState by PlayerController.state.collectAsStateWithLifecycle()
    val records by PracticeRepository.records.collectAsStateWithLifecycle()
    val reminders by PracticeRepository.reminders.collectAsStateWithLifecycle()
    val customItems by CustomRepository.items.collectAsStateWithLifecycle()
    val playing = playerState.trackId != null

    // 全屏视频时隐藏底部栏与迷你播放器。
    val immersive = tab == Tab.Custom && customVideo != null

    // 底部预留：导航栏 + 播放中时的迷你播放器高度。
    val bottomInset = if (immersive) 0.dp else 72.dp + if (playing) 84.dp else 0.dp

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when (tab) {
            Tab.Home -> {
                val series = activeSeries
                val current = selected
                when {
                    series != null -> FitnessPlayerScreen(
                        series = series,
                        seriesIndex = FitnessCatalog.series.indexOf(series),
                        bottomInset = bottomInset,
                        onBack = { activeSeries = null },
                    )
                    showFitnessList -> FitnessListScreen(
                        bottomInset = bottomInset,
                        onOpen = { activeSeries = it },
                        onBack = { showFitnessList = false },
                    )
                    current != null -> DetailScreen(
                        category = current,
                        playingId = playerState.trackId,
                        isPlaying = playerState.isPlaying,
                        bottomInset = bottomInset,
                        onBack = { selected = null },
                    )
                    else -> HomeScreen(
                        bottomInset = bottomInset,
                        todayCount = records.count { it.date == java.time.LocalDate.now() },
                        onSelect = { selected = it },
                        onOpenFitness = { showFitnessList = true },
                    )
                }
            }
            Tab.Custom -> {
                val video = customVideo
                if (video != null) {
                    VideoPlayerScreen(item = video, onBack = { customVideo = null })
                } else {
                    CustomScreen(
                        mediaItems = customItems,
                        bottomInset = bottomInset,
                        onPlayVideo = { customVideo = it },
                    )
                }
            }
            Tab.Mine -> MineScreen(
                records = records,
                reminders = reminders,
                bottomInset = bottomInset,
            )
        }

        if (!immersive) {
            Column(Modifier.align(Alignment.BottomCenter)) {
                if (playing) {
                    MiniPlayer(
                        title = playerState.title,
                        isPlaying = playerState.isPlaying,
                        positionMs = playerState.positionMs,
                        durationMs = playerState.durationMs,
                        onSeek = { PlayerController.seekTo(it) },
                        onToggle = { PlayerController.togglePlayPause() },
                        onStop = { PlayerController.stop() },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
                BottomBar(current = tab, onSelect = { tab = it })
            }
        }
    }
}

/* ---------- 底部导航 ---------- */

@Composable
private fun BottomBar(current: Tab, onSelect: (Tab) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().shadow(16.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            BottomBarItem(Icons.Filled.Home, "练习", current == Tab.Home) { onSelect(Tab.Home) }
            BottomBarItem(Icons.Filled.VideoLibrary, "自定义", current == Tab.Custom) { onSelect(Tab.Custom) }
            BottomBarItem(Icons.Filled.Person, "我的", current == Tab.Mine) { onSelect(Tab.Mine) }
        }
    }
}

@Composable
private fun BottomBarItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    val tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(26.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = tint,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

/* ---------- 首页 ---------- */

@Composable
private fun HomeScreen(
    bottomInset: androidx.compose.ui.unit.Dp,
    todayCount: Int,
    onSelect: (Category) -> Unit,
    onOpenFitness: () -> Unit,
) {
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
                    greeting(),
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
                    if (todayCount > 0) "今天已练习 $todayCount 次，继续保持 💪" else "选择今天的练习开始吧",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            FitnessBanner(onClick = onOpenFitness)
        }
        items(Catalog.categories, key = { it.id }) { category ->
            CategoryCard(category = category, onClick = { onSelect(category) })
        }
    }
}

@Composable
private fun FitnessBanner(onClick: () -> Unit) {
    val colors = listOf(Color(0xFFF97316), Color(0xFFDB2777))
    Row(
        Modifier
            .fillMaxWidth()
            .height(96.dp)
            .shadow(10.dp, RoundedCornerShape(24.dp), spotColor = colors.last())
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(colors))
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.FitnessCenter, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
        }
        Column(Modifier.weight(1f).padding(start = 16.dp)) {
            Text("健身操", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text(
                "跟练动图，${FitnessCatalog.series.size} 套常用动作序列",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.9f),
            )
        }
        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
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
                onClick = {
                    if (item.rawResId != null) {
                        PlayerController.play(context, item)
                    } else {
                        PracticeRepository.logPractice(context, item.id, item.title, category.title)
                        Toast.makeText(context, "已记录练习：${item.title}", Toast.LENGTH_SHORT).show()
                    }
                },
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
            .clickable(onClick = onClick)
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
    positionMs: Int,
    durationMs: Int,
    onSeek: (Int) -> Unit,
    onToggle: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth().shadow(16.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
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
            MiniSeekBar(
                positionMs = positionMs,
                durationMs = durationMs,
                onSeek = onSeek,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun MiniSeekBar(
    positionMs: Int,
    durationMs: Int,
    onSeek: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val fraction = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(formatMs(positionMs), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Box(
            Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
                .height(16.dp)
                .pointerInput(durationMs) {
                    detectTapGestures { offset ->
                        if (durationMs > 0) {
                            onSeek((offset.x / size.width * durationMs).toInt().coerceIn(0, durationMs))
                        }
                    }
                }
                .pointerInput(durationMs) {
                    detectHorizontalDragGestures { change, _ ->
                        if (durationMs > 0) {
                            onSeek((change.position.x / size.width * durationMs).toInt().coerceIn(0, durationMs))
                        }
                    }
                },
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            Box(
                Modifier
                    .fillMaxWidth(fraction)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
        Text(formatMs(durationMs), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun formatMs(ms: Int): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    return "%d:%02d".format(totalSec / 60, totalSec % 60)
}

@Composable
private fun IconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        Modifier.size(44.dp).clip(CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}
