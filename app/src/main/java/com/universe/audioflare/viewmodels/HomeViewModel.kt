package com.universe.audioflare.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universe.audioflared.YouTube
import com.universe.audioflared.pages.ExplorePage
import com.universe.audioflare.db.MusicDatabase
import com.universe.audioflare.db.entities.Artist
import com.universe.audioflare.db.entities.Song
import com.universe.audioflare.utils.reportException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val database: MusicDatabase,
) : ViewModel() {
    val isRefreshing = MutableStateFlow(false)

    val quickPicks = MutableStateFlow<List<Song>?>(null)
    val explorePage = MutableStateFlow<ExplorePage?>(null)

    private suspend fun load() {
        quickPicks.value = database.quickPicks().first().shuffled().take(20)
        YouTube.explore().onSuccess { page ->
            val artists: Set<String>
            val favouriteArtists: Set<String>
            database.artistsByCreateDateAsc().first().let { list ->
                artists = list.map(Artist::id).toHashSet()
                favouriteArtists = list
                    .filter { it.artist.bookmarkedAt != null }
                    .map { it.id }
                    .toHashSet()
            }
            explorePage.value = page.copy(
                newReleaseAlbums = page.newReleaseAlbums
                    .sortedBy { album ->
                        if (album.artists.orEmpty().any { it.id in favouriteArtists }) 0
                        else if (album.artists.orEmpty().any { it.id in artists }) 1
                        else 2
                    }
            )
        }.onFailure {
            reportException(it)
        }
    }

    fun refresh() {
        if (isRefreshing.value) return
        viewModelScope.launch(Dispatchers.IO) {
            isRefreshing.value = true
            load()
            isRefreshing.value = false
        }
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            load()
        }
    }
}
