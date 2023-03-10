package com.example.musicwithyou.presentation.screens.main_tabs.songs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicwithyou.domain.usecase.song_usecase.SongUseCases
import com.example.musicwithyou.domain.utils.OrderType
import com.example.musicwithyou.domain.utils.SongOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongsViewModel @Inject constructor(
    private val songUseCases: SongUseCases,
) : ViewModel() {

    var state by mutableStateOf(SongsState())
        private set

    private var getSongsJob: Job? = null

    init {
        getSongs()
    }

    fun onEvent(event: SongsEvent) {
        when (event) {
            is SongsEvent.RefreshSongs -> {
                getSongs()
            }
            is SongsEvent.OrderChange -> {
                if (state.songOrder::class == event.songOrder::class &&
                    state.songOrder.orderType == event.songOrder.orderType
                ) {
                    return
                }
                getSongs(songOrder = event.songOrder)
            }
        }
    }


    private fun getSongs(
        songOrder: SongOrder = SongOrder.Title(OrderType.Ascending),
    ) {
        getSongsJob?.cancel()
        getSongsJob = viewModelScope.launch() {
            state = state.copy(isRefreshing = true)
            val songs = songUseCases.getSongs(songOrder = songOrder)
            state = state.copy(songs = songs, songOrder = songOrder, isRefreshing = false)
        }
    }

}