package com.universe.audioflared.pages

import com.universe.audioflared.models.BrowseEndpoint
import com.universe.audioflared.models.GridRenderer
import com.universe.audioflared.models.MusicNavigationButtonRenderer
import com.universe.audioflared.models.SectionListRenderer

data class MoodAndGenres(
    val title: String,
    val items: List<Item>,
) {
    data class Item(
        val title: String,
        val stripeColor: Long,
        val endpoint: BrowseEndpoint,
    )

    companion object {
        fun fromSectionListRendererContent(content: SectionListRenderer.Content): MoodAndGenres? {
            return MoodAndGenres(
                title = content.gridRenderer?.header?.gridHeaderRenderer?.title?.runs?.firstOrNull()?.text ?: return null,
                items = content.gridRenderer.items
                    .mapNotNull(GridRenderer.Item::musicNavigationButtonRenderer)
                    .mapNotNull(::fromMusicNavigationButtonRenderer)
            )
        }

        fun fromMusicNavigationButtonRenderer(renderer: MusicNavigationButtonRenderer): Item? {
            return Item(
                title = renderer.buttonText.runs?.firstOrNull()?.text ?: return null,
                stripeColor = renderer.solid?.leftStripeColor ?: return null,
                endpoint = renderer.clickCommand.browseEndpoint ?: return null,
            )
        }
    }
}
