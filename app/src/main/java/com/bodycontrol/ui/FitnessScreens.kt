package com.bodycontrol.ui

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.bodycontrol.data.FitnessCatalog
import com.bodycontrol.data.FitnessSeries
import com.bodycontrol.data.PracticeRepository
import kotlinx.coroutines.delay

private val seriesColors: List<List<Color>> = listOf(
    listOf(Color(0xFFF472B6), Color(0xFFDB2777)),
    listOf(Color(0xFF60A5FA), Color(0xFF2563EB)),
    listOf(Color(0xFFFBBF24), Color(0xFFF97316)),
    listOf(Color(0xFF34D399), Color(0xFF0E9F7E)),
    listOf(Color(0xFFA78BFA), Color(0xFF7C3AED)),
)

private fun colorsFor(index: Int) = seriesColors[index % seriesColors.size]

/** 供 GIF 动图解码的 Coil ImageLoader。 */
@Composable
private fun rememberGifLoader(): ImageLoader {
    val context = LocalContext.current
    return remember {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }
}

/* ---------- 系列列表 ---------- */

@Composable
fun FitnessListScreen(
    bottomInset: Dp,
    onOpen: (FitnessSeries) -> Unit,
    onBack: () -> Unit,
) {
    val loader = rememberGifLoader()
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp + bottomInset),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFF97316), Color(0xFFDB2777))))
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
                    Text(
                        "健身操",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 18.dp),
                    )
                    Text(
                        "常用动作序列，每个动作默认 2 分钟",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                    )
                }
            }
        }
        items(FitnessCatalog.series, key = { it.id }) { series ->
            val idx = FitnessCatalog.series.indexOf(series)
            SeriesCard(
                series = series,
                colors = colorsFor(idx),
                loader = loader,
                onClick = { onOpen(series) },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

@Composable
private fun SeriesCard(
    series: FitnessSeries,
    colors: List<Color>,
    loader: ImageLoader,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Row(
        modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(22.dp), spotColor = colors.last())
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.linearGradient(colors)),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context).data(series.moves.first().assetUri).build(),
                imageLoader = loader,
                contentDescription = series.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
            )
        }
        Column(Modifier.weight(1f).padding(start = 14.dp)) {
            Text(
                series.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                series.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 3.dp),
            )
            Box(
                Modifier
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Brush.linearGradient(colors))
                    .padding(horizontal = 10.dp, vertical = 3.dp),
            ) {
                Text(
                    "${series.moves.size} 个动作",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/* ---------- 播放器 ---------- */

private val durationOptions = listOf(60, 120, 180)

@Composable
fun FitnessPlayerScreen(
    series: FitnessSeries,
    seriesIndex: Int,
    bottomInset: Dp,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val loader = rememberGifLoader()
    val colors = colorsFor(seriesIndex)
    val moves = series.moves

    var durationSec by remember { mutableIntStateOf(120) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var remaining by remember { mutableIntStateOf(durationSec) }
    var running by remember { mutableStateOf(true) }
    var finished by remember { mutableStateOf(false) }

    val tone = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 100) }
    DisposableEffect(Unit) { onDispose { tone.release() } }

    fun goTo(index: Int) {
        currentIndex = index.coerceIn(0, moves.lastIndex)
        remaining = durationSec
        finished = false
    }

    // 倒计时：running 变化或切换动作/时长时重启。
    LaunchedEffect(currentIndex, running, durationSec, finished) {
        if (!running || finished) return@LaunchedEffect
        while (remaining > 0) {
            delay(1000)
            remaining -= 1
        }
        // 本动作结束：提示音 + 切换
        tone.startTone(ToneGenerator.TONE_PROP_BEEP, 350)
        if (currentIndex < moves.lastIndex) {
            currentIndex += 1
            remaining = durationSec
        } else {
            running = false
            finished = true
            tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 700)
            PracticeRepository.logPractice(context, "fitness_${series.id}", series.title)
        }
    }

    val move = moves[currentIndex]

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 顶部栏
        Row(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = MaterialTheme.colorScheme.onBackground)
            }
            Column(Modifier.weight(1f).padding(start = 4.dp)) {
                Text(series.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text("动作 ${currentIndex + 1}/${moves.size}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // GIF 画面
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context).data(move.assetUri).build(),
                imageLoader = loader,
                contentDescription = move.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
            if (finished) {
                Box(
                    Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("已完成 🎉", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Brush.linearGradient(colors))
                                .clickable {
                                    goTo(0)
                                    running = true
                                }
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                        ) {
                            Text("再来一组", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // 动作名 + 左右切换
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            SwitchButton(
                icon = Icons.Filled.ChevronLeft,
                enabled = currentIndex > 0,
                colors = colors,
                onClick = { goTo(currentIndex - 1) },
                contentDescription = "上一个动作",
            )
            Text(
                move.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            SwitchButton(
                icon = Icons.Filled.ChevronRight,
                enabled = currentIndex < moves.lastIndex,
                colors = colors,
                onClick = { goTo(currentIndex + 1) },
                contentDescription = "下一个动作",
            )
        }

        // 倒计时
        Text(
            formatTime(remaining),
            fontSize = 56.sp,
            fontWeight = FontWeight.ExtraBold,
            color = colors.last(),
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )

        // 进度点
        Row(
            Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            moves.forEachIndexed { i, _ ->
                Box(
                    Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (i == currentIndex) 10.dp else 7.dp)
                        .clip(CircleShape)
                        .background(
                            if (i <= currentIndex) colors.last()
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // 播放/暂停
        Box(
            Modifier
                .align(Alignment.CenterHorizontally)
                .size(72.dp)
                .shadow(10.dp, CircleShape, spotColor = colors.last())
                .clip(CircleShape)
                .background(Brush.linearGradient(colors))
                .clickable {
                    if (finished) {
                        goTo(0)
                        running = true
                    } else {
                        running = !running
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                when {
                    finished -> Icons.Filled.Refresh
                    running -> Icons.Filled.Pause
                    else -> Icons.Filled.PlayArrow
                },
                contentDescription = if (running) "暂停" else "开始",
                tint = Color.White,
                modifier = Modifier.size(36.dp),
            )
        }

        Spacer(Modifier.height(20.dp))

        // 单个动作时长
        Text(
            "单个动作时长",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 20.dp),
        )
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            durationOptions.forEach { sec ->
                val selectedDur = sec == durationSec
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (selectedDur) Brush.linearGradient(colors)
                            else Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surfaceVariant,
                                )
                            )
                        )
                        .clickable {
                            durationSec = sec
                            remaining = sec
                            finished = false
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "${sec / 60} 分钟",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selectedDur) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(Modifier.height(bottomInset))
    }
}

@Composable
private fun SwitchButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    colors: List<Color>,
    onClick: () -> Unit,
    contentDescription: String,
) {
    Box(
        Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(
                if (enabled) Brush.linearGradient(colors)
                else Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.surfaceVariant,
                    )
                )
            )
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = if (enabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(30.dp),
        )
    }
}

private fun formatTime(totalSec: Int): String {
    val m = totalSec / 60
    val s = totalSec % 60
    return "%d:%02d".format(m, s)
}
