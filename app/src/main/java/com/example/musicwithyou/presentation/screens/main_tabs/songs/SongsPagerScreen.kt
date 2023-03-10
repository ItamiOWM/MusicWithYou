package com.example.musicwithyou.presentation.screens.main_tabs.songs

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.musicwithyou.R
import com.example.musicwithyou.presentation.components.AddToPlaylistSheetContent
import com.example.musicwithyou.presentation.components.SongActionsSheetContent
import com.example.musicwithyou.presentation.components.SongItem
import com.example.musicwithyou.presentation.screens.main.MainViewModel
import com.example.musicwithyou.presentation.screens.main_tabs.songs.components.SongOrderSectionSheetContent
import com.example.musicwithyou.presentation.utils.ActionItem
import com.example.musicwithyou.utils.EMPTY_STRING
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun SongsPagerScreen(
    navController: NavController,
    songsViewModel: SongsViewModel,
    mainViewModel: MainViewModel,
) {

    //States
    val state = songsViewModel.state
    val songs = state.songs
    val songOrder = songsViewModel.state.songOrder
    val swipeRefreshState = rememberSwipeRefreshState(
        isRefreshing = songsViewModel.state.isRefreshing
    )


    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true,
    )

    val bottomSheetScope = rememberCoroutineScope()

    val sheetInitialContent: @Composable (() -> Unit) = { Text(EMPTY_STRING) }

    var customSheetContent by remember { mutableStateOf(sheetInitialContent) }


    ModalBottomSheetLayout(
        sheetContent = {
            customSheetContent()
        },
        sheetState = bottomSheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(15.dp))
                        .clickable {
                            mainViewModel.playShuffled(songs)
                        }
                ) {
                    Icon(
                        painter = painterResource(
                            id = R.drawable.shuffle
                        ),
                        contentDescription = stringResource(R.string.shuffle_icon_desc),
                        tint = MaterialTheme.colors.secondaryVariant,
                        modifier = Modifier
                            .size(30.dp)
                            .align(Alignment.CenterVertically)
                            .padding(end = 5.dp)
                    )
                    Text(
                        text = stringResource(R.string.shuffle, songs.size),
                        style = MaterialTheme.typography.body1.copy(color = MaterialTheme.colors.secondaryVariant),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
                Row() {
                    IconButton(
                        modifier = Modifier
                            .size(25.dp)
                            .align(Alignment.CenterVertically),
                        onClick = {
                            customSheetContent = {
                                SongOrderSectionSheetContent(
                                    songOrder = songOrder,
                                    onOrderChange = { newOrder ->
                                        songsViewModel.onEvent(SongsEvent.OrderChange(newOrder))
                                        bottomSheetScope.launch {
                                            bottomSheetState.hide()
                                        }
                                    },
                                    onCancel = {
                                        bottomSheetScope.launch {
                                            bottomSheetState.hide()
                                        }
                                    }
                                )
                            }
                            bottomSheetScope.launch {
                                bottomSheetState.show()
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(
                                id = R.drawable.sort
                            ),
                            contentDescription = stringResource(R.string.sort_song_icon_desc),
                            tint = MaterialTheme.colors.secondaryVariant,
                            modifier = Modifier
                                .size(25.dp)
                                .align(Alignment.CenterVertically)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))
                    IconButton(
                        modifier = Modifier
                            .size(25.dp)
                            .align(Alignment.CenterVertically),
                        onClick = {
                            //Todo launch song picker screen
                        }
                    ) {
                        Icon(
                            painter = painterResource(
                                id = R.drawable.picker
                            ),
                            contentDescription = stringResource(R.string.songs_picker_desc),
                            tint = MaterialTheme.colors.secondaryVariant,
                            modifier = Modifier
                                .size(25.dp)
                                .align(Alignment.CenterVertically)
                        )
                    }

                }
            }
            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = {
                    songsViewModel.onEvent(SongsEvent.RefreshSongs)
                }
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(songs, key = { it.id }) { song ->
                        SongItem(
                            song = song,
                            isCurrentSong = song == mainViewModel.currentPlayingSong.value,
                            isSongPlaying = mainViewModel.isSongPlaying.value,
                            modifier = Modifier
                                .padding(top = 10.dp, bottom = 10.dp)
                                .fillMaxSize()
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Transparent)
                                .animateItemPlacement(animationSpec = tween(500))
                                .clickable {
                                    mainViewModel.playSong(
                                        song = song,
                                        songs
                                    )
                                },
                            onOptionsClicked = {
                                bottomSheetScope.launch {
                                    customSheetContent = {
                                        SongActionsSheetContent(
                                            song = song,
                                            items = listOf(
                                                ActionItem(
                                                    actionTitle = stringResource(R.string.play_next),
                                                    itemClicked = {
                                                        bottomSheetScope.launch {
                                                            mainViewModel.playNext(song)
                                                            bottomSheetState.hide()
                                                        }
                                                    },
                                                    iconId = R.drawable.play_next
                                                ),
                                                ActionItem(
                                                    actionTitle = stringResource(R.string.add_to_queue),
                                                    itemClicked = {
                                                        bottomSheetScope.launch {
                                                            mainViewModel.addToQueue(song)
                                                            bottomSheetState.hide()
                                                        }

                                                    },
                                                    iconId = R.drawable.queue
                                                ),
                                                ActionItem(
                                                    actionTitle = stringResource(R.string.add_to_playlist),
                                                    itemClicked = {
                                                        bottomSheetScope.launch {
                                                            customSheetContent = {
                                                                AddToPlaylistSheetContent(
                                                                    modifier = Modifier
                                                                        .fillMaxHeight(0.5f),
                                                                    playlistPreviews = mainViewModel.playlistPreviews.value,
                                                                    onCreateNewPlaylist = {
                                                                        mainViewModel.onShowCreatePlaylistDialog(
                                                                            listOf(song)
                                                                        )
                                                                        bottomSheetScope.launch {
                                                                            bottomSheetState.hide()
                                                                        }
                                                                    },
                                                                    onPlaylistClick = {
                                                                        mainViewModel.addToPlaylist(
                                                                            listOf(song),
                                                                            it.id
                                                                        )
                                                                        bottomSheetScope.launch {
                                                                            bottomSheetState.hide()
                                                                        }
                                                                    }
                                                                )
                                                            }
                                                            bottomSheetScope.launch {
                                                                bottomSheetState.show()
                                                            }
                                                        }
                                                    },
                                                    iconId = R.drawable.add_to_playlist
                                                ),
                                                ActionItem(
                                                    actionTitle = stringResource(R.string.install_as_ringtone),
                                                    itemClicked = {
                                                        //Todo install song as ringtone
                                                    },
                                                    iconId = R.drawable.bell
                                                ),
                                                ActionItem(
                                                    actionTitle = stringResource(R.string.delete_from_device),
                                                    itemClicked = {
                                                        //Delete from device
                                                    },
                                                    iconId = R.drawable.delete
                                                )
                                            )
                                        )
                                    }
                                    bottomSheetState.show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}