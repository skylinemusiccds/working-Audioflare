package com.universe.audioflare.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universe.audioflared.YouTube
import com.universe.audioflared.models.PlaylistItem
import com.universe.audioflared.models.SongItem
import com.universe.audioflared.utils.completed
import com.universe.audioflare.utils.reportException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnlinePlaylistViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val playlistId = savedStateHandle.get<String>("playlistId")!!

    val playlist = MutableStateFlow<PlaylistItem?>(null)
    val playlistSongs = MutableStateFlow<List<SongItem>>(emptyList())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            YouTube.playlist(playlistId).completed()
                .onSuccess { playlistPage ->
                    playlist.value = playlistPage.playlist
                    playlistSongs.value = playlistPage.songs
                }.onFailure {
                    reportException(it)
                }
        }
    }
}
