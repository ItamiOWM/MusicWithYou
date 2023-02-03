package com.example.musicwithyou.presentation

import android.support.v4.media.MediaBrowserCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicwithyou.domain.models.Song
import com.example.musicwithyou.domain.usecase.song_usecase.GetSongsUseCase
import com.example.musicwithyou.media.exoplayer.MediaPlayerServiceConnection
import com.example.musicwithyou.media.service.MediaPlayerService
import com.example.musicwithyou.media.utils.MEDIA_ROOT_ID
import com.example.musicwithyou.media.utils.PLAYBACK_UPDATE_INTERVAL
import com.example.musicwithyou.media.utils.currentPosition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val getSongsUseCase: GetSongsUseCase,
    serviceConnection: MediaPlayerServiceConnection,
) : ViewModel() {

    var songList by mutableStateOf<List<Song>>(emptyList())
        private set

    val currentPlayingSong = serviceConnection.currentPlayingSong

    val repeatMode = serviceConnection.repeatMode

    val shuffleMode = serviceConnection.shuffleMode

    var currentPlaybackPosition by mutableStateOf(0L)
        private set

    val isSongPlaying = serviceConnection.isPlaying

    val currentDuration: Long
        get() = MediaPlayerService.currentDuration

    var currentSongProgress = mutableStateOf(0f)
        private set

    lateinit var rootMediaId: String
        private set

    private val isConnected = serviceConnection.isConnected

    private val playbackState = serviceConnection.playBackState

    private var updatePosition = true

    private val subscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>,
        ) {
            super.onChildrenLoaded(parentId, children)
        }
    }

    private val serviceConnection = serviceConnection.also {
        updatePlayback()
    }


    init {
        viewModelScope.launch {
            songList = getSongs()
            isConnected.collect {
                if (it) {
                    rootMediaId = serviceConnection.rootMediaId
                    serviceConnection.playBackState.value?.apply {
                        currentPlaybackPosition = position
                    }
                    serviceConnection.subscribe(rootMediaId, subscriptionCallback)
                }
            }
        }
    }

    fun playSong(currentSong: Song, currentSongList: List<Song>) {
        songList = currentSongList
        serviceConnection.playAudio(songList)
        if (currentSong.id == currentPlayingSong.value?.id) {
            if (isSongPlaying.value) {
                serviceConnection.transportControl.pause()
            } else {
                serviceConnection.transportControl.play()
            }
        } else {
            serviceConnection.transportControl.playFromMediaId(currentSong.id.toString(), null)
        }
    }



    fun shuffle() {
        serviceConnection.shuffle()
    }

    fun repeat() {
        serviceConnection.repeat()
    }

    fun stopPlayback() {
        serviceConnection.transportControl.stop()
    }

    fun fastForward() {
        serviceConnection.fastForward()
    }

    fun rewind() {
        serviceConnection.rewind()
    }

    fun skipToNext() {
        serviceConnection.skipToNext()
    }

    fun skipToPrevious() {
        serviceConnection.skipToPrevious()
    }

    fun seekTo(value: Float) {
        serviceConnection.transportControl.seekTo(
            (currentDuration * value / 100f).toLong()
        )
    }

    private fun updatePlayback() {
        viewModelScope.launch {
            val position = playbackState.value?.currentPosition ?: 0

            if (currentPlaybackPosition != position) {
                currentPlaybackPosition = position
            }

            if (currentDuration > 0) {
                currentSongProgress.value = (
                        currentPlaybackPosition.toFloat() / currentDuration.toFloat())
            }

            delay(PLAYBACK_UPDATE_INTERVAL)

            if (updatePosition) {
                updatePlayback()
            }
        }
    }

    private suspend fun getSongs(): List<Song> {
        return getSongsUseCase()
    }

    override fun onCleared() {
        super.onCleared()
        serviceConnection.unSubscribe(
            MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {}
        )
        updatePosition = false
    }
}