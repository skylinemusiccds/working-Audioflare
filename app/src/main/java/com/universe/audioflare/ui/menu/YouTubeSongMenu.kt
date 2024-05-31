package com.universe.audioflare.ui.menu

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.universe.audioflared.models.SongItem
import com.universe.audioflared.models.WatchEndpoint
import com.universe.audioflare.LocalDatabase
import com.universe.audioflare.LocalDownloadUtil
import com.universe.audioflare.LocalPlayerConnection
import com.universe.audioflare.R
import com.universe.audioflare.constants.ListItemHeight
import com.universe.audioflare.constants.ListThumbnailSize
import com.universe.audioflare.constants.ThumbnailCornerRadius
import com.universe.audioflare.db.entities.PlaylistSongMap
import com.universe.audioflare.db.entities.SongEntity
import com.universe.audioflare.extensions.toMediaItem
import com.universe.audioflare.models.MediaMetadata
import com.universe.audioflare.models.toMediaMetadata
import com.universe.audioflare.playback.ExoDownloadService
import com.universe.audioflare.playback.queues.YouTubeQueue
import com.universe.audioflare.ui.component.DownloadGridMenu
import com.universe.audioflare.ui.component.GridMenu
import com.universe.audioflare.ui.component.GridMenuItem
import com.universe.audioflare.ui.component.ListDialog
import com.universe.audioflare.ui.component.ListItem
import com.universe.audioflare.utils.joinByBullet
import com.universe.audioflare.utils.makeTimeString
import java.time.LocalDateTime

@Composable
fun YouTubeSongMenu(
    song: SongItem,
    navController: NavController,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val database = LocalDatabase.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val librarySong by database.song(song.id).collectAsState(initial = null)
    val download by LocalDownloadUtil.current.getDownload(song.id).collectAsState(initial = null)
    val artists = remember {
        song.artists.mapNotNull {
            it.id?.let { artistId ->
                MediaMetadata.Artist(id = artistId, name = it.name)
            }
        }
    }

    var showChoosePlaylistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    AddToPlaylistDialog(
        isVisible = showChoosePlaylistDialog,
        onAdd = { playlist ->
            database.transaction {
                insert(song.toMediaMetadata())
                insert(
                    PlaylistSongMap(
                        songId = song.id,
                        playlistId = playlist.id,
                        position = playlist.songCount
                    )
                )
            }
        },
        onDismiss = { showChoosePlaylistDialog = false }
    )

    var showSelectArtistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showSelectArtistDialog) {
        ListDialog(
            onDismiss = { showSelectArtistDialog = false }
        ) {
            items(artists) { artist ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(ListItemHeight)
                        .clickable {
                            navController.navigate("artist/${artist.id}")
                            showSelectArtistDialog = false
                            onDismiss()
                        }
                        .padding(horizontal = 12.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .height(ListItemHeight)
                            .clickable {
                                navController.navigate("artist/${artist.id}")
                                showSelectArtistDialog = false
                                onDismiss()
                            }
                            .padding(horizontal = 24.dp),
                    ) {
                        Text(
                            text = artist.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }

    ListItem(
        title = song.title,
        subtitle = joinByBullet(
            song.artists.joinToString { it.name },
            song.duration?.let { makeTimeString(it * 1000L) }
        ),
        thumbnailContent = {
            AsyncImage(
                model = song.thumbnail,
                contentDescription = null,
                modifier = Modifier
                    .size(ListThumbnailSize)
                    .clip(RoundedCornerShape(ThumbnailCornerRadius))
            )
        },
        trailingContent = {
            IconButton(
                onClick = {
                    database.transaction {
                        librarySong.let { librarySong ->
                            if (librarySong == null) {
                                insert(song.toMediaMetadata(), SongEntity::toggleLike)
                            } else {
                                update(librarySong.song.toggleLike())
                            }
                        }
                    }
                }
            ) {
                Icon(
                    painter = painterResource(if (librarySong?.song?.liked == true) R.drawable.favorite else R.drawable.favorite_border),
                    tint = if (librarySong?.song?.liked == true) MaterialTheme.colorScheme.error else LocalContentColor.current,
                    contentDescription = null
                )
            }
        }
    )

    Divider()

    GridMenu(
        contentPadding = PaddingValues(
            start = 8.dp,
            top = 8.dp,
            end = 8.dp,
            bottom = 8.dp + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
        )
    ) {
        GridMenuItem(
            icon = R.drawable.radio,
            title = R.string.start_radio
        ) {
            playerConnection.playQueue(YouTubeQueue(WatchEndpoint(videoId = song.id), song.toMediaMetadata()))
            onDismiss()
        }
        GridMenuItem(
            icon = R.drawable.playlist_play,
            title = R.string.play_next
        ) {
            playerConnection.playNext(song.toMediaItem())
            onDismiss()
        }
        GridMenuItem(
            icon = R.drawable.queue_music,
            title = R.string.add_to_queue
        ) {
            playerConnection.addToQueue((song.toMediaItem()))
            onDismiss()
        }
        if (librarySong?.song?.inLibrary != null) {
            GridMenuItem(
                icon = R.drawable.library_add_check,
                title = R.string.remove_from_library
            ) {
                database.query {
                    inLibrary(song.id, null)
                }
            }
        } else {
            GridMenuItem(
                icon = R.drawable.library_add,
                title = R.string.add_to_library
            ) {
                database.transaction {
                    insert(song.toMediaMetadata())
                    inLibrary(song.id, LocalDateTime.now())
                }
            }
        }
        GridMenuItem(
            icon = R.drawable.playlist_add,
            title = R.string.add_to_playlist
        ) {
            showChoosePlaylistDialog = true
        }
        DownloadGridMenu(
            state = download?.state,
            onDownload = {
                database.transaction {
                    insert(song.toMediaMetadata())
                }
                val downloadRequest = DownloadRequest.Builder(song.id, song.id.toUri())
                    .setCustomCacheKey(song.id)
                    .setData(song.title.toByteArray())
                    .build()
                DownloadService.sendAddDownload(
                    context,
                    ExoDownloadService::class.java,
                    downloadRequest,
                    false
                )
            },
            onRemoveDownload = {
                DownloadService.sendRemoveDownload(
                    context,
                    ExoDownloadService::class.java,
                    song.id,
                    false
                )
            }
        )
        if (artists.isNotEmpty()) {
            GridMenuItem(
                icon = R.drawable.artist,
                title = R.string.view_artist
            ) {
                if (artists.size == 1) {
                    navController.navigate("artist/${artists[0].id}")
                    onDismiss()
                } else {
                    showSelectArtistDialog = true
                }
            }
        }
        song.album?.let { album ->
            GridMenuItem(
                icon = R.drawable.album,
                title = R.string.view_album
            ) {
                navController.navigate("album/${album.id}")
                onDismiss()
            }
        }
        GridMenuItem(
            icon = R.drawable.share,
            title = R.string.share
        ) {
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, song.shareLink)
            }
            context.startActivity(Intent.createChooser(intent, null))
            onDismiss()
        }
    }
}
