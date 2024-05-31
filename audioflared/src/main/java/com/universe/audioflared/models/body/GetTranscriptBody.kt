package com.universe.audioflared.models.body

import com.universe.audioflared.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class GetTranscriptBody(
    val context: Context,
    val params: String,
)
