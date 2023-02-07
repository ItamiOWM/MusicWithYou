package com.example.musicwithyou.presentation.screens.current_song

import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import coil.compose.AsyncImage
import com.example.musicwithyou.R
import com.example.musicwithyou.navigation.Screen
import com.example.musicwithyou.presentation.MainViewModel
import com.example.musicwithyou.presentation.screens.main_tabs.songs.components.SongActionsSheetContent
import com.example.musicwithyou.presentation.utils.ActionItem
import com.example.musicwithyou.utils.EMPTY_STRING
import com.example.musicwithyou.utils.timestampToDuration
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PlayingNowScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    playingNowViewModel: PlayingNowViewModel = hiltViewModel(),
) {

    val currentSong = mainViewModel.currentPlayingSong.value

    val modalBottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden
    )

    val coroutineScope = rememberCoroutineScope()

    val sheetInitialContent: @Composable (() -> Unit) = { Text(EMPTY_STRING) }

    var customSheetContent by remember { mutableStateOf(sheetInitialContent) }

    val songProgress by animateFloatAsState(
        targetValue = mainViewModel.currentSongProgress.value
    )


    if (currentSong != null) {
        ModalBottomSheetLayout(
            sheetContent = {
                customSheetContent()
            },
            sheetState = modalBottomSheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_down),
                        contentDescription = stringResource(id = R.string.close_current_playing_desc),
                        modifier = Modifier
                            .size(35.dp)
                            .clickable {
                                navController.popBackStack()
                            }
                    )
                    Text(
                        text = stringResource(id = R.string.now_playing_title),
                        style = MaterialTheme.typography.subtitle2
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.playlist),
                        contentDescription = stringResource(R.string.queue_songs_desc),
                        modifier = Modifier
                            .size(25.dp)
                            .clickable {
                                navController.navigate(
                                    Screen.CurrentQueueScreen.route
                                ) {
                                    popUpTo(
                                        id = navController.currentDestination?.id
                                            ?: navController.graph.findStartDestination().id
                                    ) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                    )
                }
                Spacer(modifier = Modifier.height(25.dp))
                AsyncImage(
                    model = currentSong.imageUri,
                    contentDescription = stringResource(id = R.string.song_image_desc),
                    error = painterResource(id = R.drawable.unknown_song),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(300.dp)
                        .shadow(20.dp, RoundedCornerShape(30.dp))
                )
                Spacer(modifier = Modifier.height(50.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Icon(
                        painter = painterResource(id = R.drawable.isnt_favorite),
                        contentDescription = stringResource(id = R.string.close_current_playing_desc),
                        modifier = Modifier
                            .weight(0.1f)
                            .align(Alignment.Top)
                            .padding(top = 5.dp)
                            .size(25.dp)
                            .clickable {
                                //TODO add to favorites playlist
                            }
                    )
                    Spacer(modifier = Modifier.width(15.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = currentSong.title,
                            style = MaterialTheme.typography.h4,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Text(
                            text = currentSong.artistName,
                            style = MaterialTheme.typography.subtitle1,
                            color = MaterialTheme.colors.secondaryVariant,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                    Spacer(modifier = Modifier.width(15.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.options),
                        contentDescription = stringResource(id = R.string.close_current_playing_desc),
                        modifier = Modifier
                            .weight(0.1f)
                            .align(Alignment.Top)
                            .padding(top = 5.dp)
                            .size(25.dp)
                            .clickable {
                                coroutineScope.launch {
                                    customSheetContent = {
                                        SongActionsSheetContent(
                                            song = currentSong,
                                            items = listOf(
                                                ActionItem(
                                                    actionTitle = stringResource(R.string.add_to_playlist),
                                                    itemClicked = {
                                                        //Todo Launch new sheet content with playlists
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
                                                        //Todo delete from device
                                                    },
                                                    iconId = R.drawable.delete
                                                )
                                            )
                                        )
                                    }
                                    modalBottomSheetState.show()
                                }
                            }
                    )
                }
                Spacer(modifier = Modifier.height(40.dp))
                Column(
                    modifier = Modifier
                        .padding(start = 5.dp, end = 5.dp)
                        .fillMaxWidth()
                ) {
                    Slider(
                        modifier = Modifier,
                        value = songProgress,
                        onValueChange = {
                            mainViewModel.seekTo(it)
                        },
                        enabled = true,
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colors.secondaryVariant,
                            activeTrackColor = MaterialTheme.colors.secondary,
                        ),
                    )
                    Row(
                        modifier = Modifier
                            .padding(start = 5.dp, end = 5.dp)
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = mainViewModel.currentPlaybackPosition.timestampToDuration(),
                            style = MaterialTheme.typography.body2,
                        )
                        Text(
                            text = mainViewModel.currentDuration.timestampToDuration(),
                            style = MaterialTheme.typography.body2
                        )
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.shuffle),
                        contentDescription = stringResource(R.string.shuffle_mode_desc),
                        tint = if (mainViewModel.shuffleMode.value == PlaybackStateCompat.SHUFFLE_MODE_NONE)
                            MaterialTheme.colors.secondaryVariant else MaterialTheme.colors.secondary,
                        modifier = Modifier
                            .size(25.dp)
                            .align(Alignment.CenterVertically)
                            .clickable {
                                mainViewModel.shuffleMode()
                            }
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.skip_back),
                        contentDescription = stringResource(R.string.shuffle_mode_desc),
                        tint = MaterialTheme.colors.secondary,
                        modifier = Modifier
                            .size(35.dp)
                            .align(Alignment.CenterVertically)
                            .clickable {
                                mainViewModel.skipToPrevious()
                            }
                    )
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape.copy(CornerSize(40.dp)))
                            .background(MaterialTheme.colors.secondary)
                            .align(Alignment.CenterVertically)
                            .clickable {
                                mainViewModel.playSong(
                                    currentSong,
                                    mainViewModel.songList
                                )
                            }
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (mainViewModel.isSongPlaying.value) R.drawable.pause
                                else R.drawable.play
                            ),
                            contentDescription = stringResource(R.string.play_or_stop_desc),
                            tint = MaterialTheme.colors.primary,
                            modifier = Modifier
                                .padding(5.dp)
                                .align(Alignment.Center)
                                .fillMaxSize(0.4f)
                        )
                    }
                    Icon(
                        painter = painterResource(id = R.drawable.skip_next),
                        contentDescription = stringResource(R.string.shuffle_mode_desc),
                        tint = MaterialTheme.colors.secondary,
                        modifier = Modifier
                            .size(35.dp)
                            .align(Alignment.CenterVertically)
                            .clickable {
                                mainViewModel.skipToNext()
                            }
                    )
                    Icon(
                        painter = painterResource(
                            id = when (mainViewModel.repeatMode.value) {
                                PlaybackStateCompat.REPEAT_MODE_ONE -> R.drawable.repeat_single
                                else -> R.drawable.repeat_all
                            }
                        ),
                        contentDescription = stringResource(R.string.shuffle_mode_desc),
                        tint = if (mainViewModel.repeatMode.value == PlaybackStateCompat.REPEAT_MODE_NONE)
                            MaterialTheme.colors.secondaryVariant else MaterialTheme.colors.secondary,
                        modifier = Modifier
                            .size(25.dp)
                            .align(Alignment.CenterVertically)
                            .clickable {
                                mainViewModel.repeatMode()
                            }
                    )
                }
            }
        }
    }
}