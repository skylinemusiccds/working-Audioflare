package com.universe.audioflared.pages

import com.universe.audioflared.models.SongItem

data class PlaylistContinuationPage(
    val songs: List<SongItem>,
    val continuation: String?,
)
