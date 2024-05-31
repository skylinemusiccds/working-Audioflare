package com.universe.audioflare.utils

import com.universe.audioflare.db.entities.LyricsEntity

object TranslationHelper {
    suspend fun translate(lyrics: LyricsEntity): LyricsEntity = lyrics
    suspend fun clearModels() {}
}