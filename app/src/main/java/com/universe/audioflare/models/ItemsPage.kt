package com.universe.audioflare.models

import com.universe.audioflared.models.YTItem

data class ItemsPage(
    val items: List<YTItem>,
    val continuation: String?,
)
