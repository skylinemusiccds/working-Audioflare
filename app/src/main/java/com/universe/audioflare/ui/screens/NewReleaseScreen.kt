package com.universe.audioflare.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.universe.audioflare.LocalPlayerAwareWindowInsets
import com.universe.audioflare.LocalPlayerConnection
import com.universe.audioflare.R
import com.universe.audioflare.constants.GridThumbnailHeight
import com.universe.audioflare.ui.component.IconButton
import com.universe.audioflare.ui.component.LocalMenuState
import com.universe.audioflare.ui.component.YouTubeGridItem
import com.universe.audioflare.ui.component.shimmer.GridItemPlaceHolder
import com.universe.audioflare.ui.component.shimmer.ShimmerHost
import com.universe.audioflare.ui.menu.YouTubeAlbumMenu
import com.universe.audioflare.ui.utils.backToMain
import com.universe.audioflare.viewmodels.NewReleaseViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NewReleaseScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: NewReleaseViewModel = hiltViewModel(),
) {
    val menuState = LocalMenuState.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val newReleaseAlbums by viewModel.newReleaseAlbums.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = GridThumbnailHeight + 24.dp),
        contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues()
    ) {
        items(
            items = newReleaseAlbums,
            key = { it.id }
        ) { album ->
            YouTubeGridItem(
                item = album,
                isActive = mediaMetadata?.album?.id == album.id,
                isPlaying = isPlaying,
                fillMaxWidth = true,
                coroutineScope = coroutineScope,
                modifier = Modifier
                    .combinedClickable(
                        onClick = {
                            navController.navigate("album/${album.id}")
                        },
                        onLongClick = {
                            menuState.show {
                                YouTubeAlbumMenu(
                                    albumItem = album,
                                    navController = navController,
                                    onDismiss = menuState::dismiss
                                )
                            }
                        }
                    )
            )
        }

        if (newReleaseAlbums.isEmpty()) {
            items(8) {
                ShimmerHost {
                    GridItemPlaceHolder(fillMaxWidth = true)
                }
            }
        }
    }

    TopAppBar(
        title = { Text(stringResource(R.string.new_release_albums)) },
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
        },
        scrollBehavior = scrollBehavior
    )
}
