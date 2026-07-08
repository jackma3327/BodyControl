package com.bodycontrol.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import com.bodycontrol.data.PracticeRepository
import com.bodycontrol.data.TrackItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PlayerState(
    val trackId: String? = null,
    val title: String = "",
    val isPlaying: Boolean = false,
)

/** 进程内单例音频播放器，基于 res/raw 资源，使用 MediaPlayer。 */
object PlayerController {

    private var mediaPlayer: MediaPlayer? = null

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    /** 播放指定条目；若点击的是当前条目则切换播放/暂停。 */
    fun play(context: Context, item: TrackItem) {
        val resId = item.rawResId ?: return

        if (_state.value.trackId == item.id && mediaPlayer != null) {
            togglePlayPause()
            return
        }

        release()
        val mp = MediaPlayer.create(context.applicationContext, resId) ?: return
        mp.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        mp.setOnCompletionListener {
            _state.value = _state.value.copy(isPlaying = false)
        }
        mediaPlayer = mp
        mp.start()
        _state.value = PlayerState(trackId = item.id, title = item.title, isPlaying = true)
        PracticeRepository.logPractice(context, item.id, item.title)
    }

    fun togglePlayPause() {
        val mp = mediaPlayer ?: return
        if (mp.isPlaying) {
            mp.pause()
            _state.value = _state.value.copy(isPlaying = false)
        } else {
            mp.start()
            _state.value = _state.value.copy(isPlaying = true)
        }
    }

    fun stop() {
        release()
        _state.value = PlayerState()
    }

    private fun release() {
        mediaPlayer?.let {
            runCatching { it.stop() }
            it.release()
        }
        mediaPlayer = null
    }
}
