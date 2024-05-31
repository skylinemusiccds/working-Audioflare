package com.universe.audioflared.models

import kotlinx.serialization.Serializable

@Serializable
data class MusicQueueRenderer(
    val content: Content?,
) {
    @Serializable
    data class Content(
        val playlistPanelRenderer: PlaylistPanelRenderer,
    )
}