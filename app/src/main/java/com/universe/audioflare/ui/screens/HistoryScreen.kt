package com.universe.audioflare.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.universe.audioflared.models.WatchEndpoint
import com.universe.audioflare.LocalPlayerAwareWindowInsets
import com.universe.audioflare.LocalPlayerConnection
import com.universe.audioflare.R
import com.universe.audioflare.extensions.togglePlayPause
import com.universe.audioflare.models.toMediaMetadata
import com.universe.audioflare.playback.queues.YouTubeQueue
import com.universe.audioflare.ui.component.IconButton
import com.universe.audioflare.ui.component.LocalMenuState
import com.universe.audioflare.ui.component.NavigationTitle
import com.universe.audioflare.ui.component.SongListItem
import com.universe.audioflare.ui.menu.SongMenu
import com.universe.audioflare.ui.utils.backToMain
import com.universe.audioflare.viewmodels.DateAgo
import com.universe.audioflare.viewmodels.HistoryViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val menuState = LocalMenuState.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val events by viewModel.events.collectAsState()

    LazyColumn(
        contentPadding = LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom).asPaddingValues(),
        modifier = Modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Top))
    ) {
        events.forEach { (dateAgo, events) ->
            stickyHeader {
                NavigationTitle(
                    title = when (dateAgo) {
                        DateAgo.Today -> stringResource(R.string.today)
                        DateAgo.Yesterday -> stringResource(R.string.yesterday)
                        DateAgo.ThisWeek -> stringResource(R.string.this_week)
                        DateAgo.LastWeek -> stringResource(R.string.last_week)
                        is DateAgo.Other -> dateAgo.date.format(DateTimeFormatter.ofPattern("yyyy/MM"))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                )
            }

            items(
                items = events,
                key = { it.event.id }
            ) { event ->
                SongListItem(
                    song = event.song,
                    isActive = event.song.id == mediaMetadata?.id,
                    isPlaying = isPlaying,
                    showInLibraryIcon = true,
                    trailingContent = {
                        IconButton(
                            onClick = {
                                menuState.show {
                                    SongMenu(
                                        originalSong = event.song,
                                        event = event.event,
                                        navController = navController,
                                        onDismiss = menuState::dismiss
                                    )
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.more_vert),
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable {
                            if (event.song.id == mediaMetadata?.id) {
                                playerConnection.player.togglePlayPause()
                            } else {
                                playerConnection.playQueue(
                                    YouTubeQueue(
                                        endpoint = WatchEndpoint(videoId = event.song.id),
                                        preloadItem = event.song.toMediaMetadata()
                                    )
                                )
                            }
                        }
                        .animateItemPlacement()
                )
            }
        }
    }

    TopAppBar(
        title = { Text(stringResource(R.string.history)) },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain
            ) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = null
                )
            }
        }
    )
}
