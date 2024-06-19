/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.workouts

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Checkbox
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.Workout
import com.example.android.architecture.blueprints.todoapp.workouts.WorkoutsFilterType.ACTIVE_WORKOUTS
import com.example.android.architecture.blueprints.todoapp.workouts.WorkoutsFilterType.ALL_WORKOUTS
import com.example.android.architecture.blueprints.todoapp.workouts.WorkoutsFilterType.COMPLETED_WORKOUTS
import com.example.android.architecture.blueprints.todoapp.util.LoadingContent
import com.example.android.architecture.blueprints.todoapp.util.WorkoutsTopAppBar
import com.google.accompanist.appcompattheme.AppCompatTheme

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun WorkoutsScreen(
        @StringRes userMessage: Int,
        onAddWorkout: () -> Unit,
        onWorkoutClick: (Workout) -> Unit,
        onUserMessageDisplayed: () -> Unit,
        modifier: Modifier = Modifier,
        viewModel: WorkoutsViewModel = hiltViewModel(),
        scaffoldState: ScaffoldState = rememberScaffoldState()
) {
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            WorkoutsTopAppBar(
                onFilterAllWorkouts = { viewModel.setFiltering(ALL_WORKOUTS) },
                onFilterActiveWorkouts = { viewModel.setFiltering(ACTIVE_WORKOUTS) },
                onFilterCompletedWorkouts = { viewModel.setFiltering(COMPLETED_WORKOUTS) },
                onClearCompletedWorkouts = { viewModel.clearCompletedWorkouts() },
                onRefresh = { viewModel.refresh() }
            )
        },
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = onAddWorkout) {
                Icon(Icons.Filled.Add, stringResource(id = R.string.add_workout))
            }
        }
    ) { paddingValues ->
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        WorkoutsContent(
            loading = uiState.isLoading,
            workouts = uiState.items,
            currentFilteringLabel = uiState.filteringUiInfo.currentFilteringLabel,
            noWorkoutsLabel = uiState.filteringUiInfo.noWorkoutsLabel,
            noWorkoutsIconRes = uiState.filteringUiInfo.noWorkoutIconRes,
            onRefresh = viewModel::refresh,
            onWorkoutClick = onWorkoutClick,
            onWorkoutCheckedChange = viewModel::completeWorkout,
            modifier = Modifier.padding(paddingValues)
        )

        // Check for user messages to display on the screen
        uiState.userMessage?.let { message ->
            val snackbarText = stringResource(message)
            LaunchedEffect(scaffoldState, viewModel, message, snackbarText) {
                scaffoldState.snackbarHostState.showSnackbar(snackbarText)
                viewModel.snackbarMessageShown()
            }
        }

        // Check if there's a userMessage to show to the user
        val currentOnUserMessageDisplayed by rememberUpdatedState(onUserMessageDisplayed)
        LaunchedEffect(userMessage) {
            if (userMessage != 0) {
                viewModel.showEditResultMessage(userMessage)
                currentOnUserMessageDisplayed()
            }
        }
    }
}

@Composable
private fun WorkoutsContent(
        loading: Boolean,
        workouts: List<Workout>,
        @StringRes currentFilteringLabel: Int,
        @StringRes noWorkoutsLabel: Int,
        @DrawableRes noWorkoutsIconRes: Int,
        onRefresh: () -> Unit,
        onWorkoutClick: (Workout) -> Unit,
        onWorkoutCheckedChange: (Workout, Boolean) -> Unit,
        modifier: Modifier = Modifier
) {
    LoadingContent(
        loading = loading,
        empty = workouts.isEmpty() && !loading,
        emptyContent = { WorkoutsEmptyContent(noWorkoutsLabel, noWorkoutsIconRes, modifier) },
        onRefresh = onRefresh
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = dimensionResource(id = R.dimen.horizontal_margin))
        ) {
            Text(
                text = stringResource(currentFilteringLabel),
                modifier = Modifier.padding(
                    horizontal = dimensionResource(id = R.dimen.list_item_padding),
                    vertical = dimensionResource(id = R.dimen.vertical_margin)
                ),
                style = MaterialTheme.typography.h6
            )
            LazyColumn {
                items(workouts) { workout ->
                    WorkoutItem(
                        workout = workout,
                        onWorkoutClick = onWorkoutClick,
                        onCheckedChange = { onWorkoutCheckedChange(workout, it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutItem(
        workout: Workout,
        onCheckedChange: (Boolean) -> Unit,
        onWorkoutClick: (Workout) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(id = R.dimen.horizontal_margin),
                vertical = dimensionResource(id = R.dimen.list_item_padding),
            )
            .clickable { onWorkoutClick(workout) }
    ) {
        Checkbox(
            checked = workout.isCompleted,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = workout.titleForList,
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(
                start = dimensionResource(id = R.dimen.horizontal_margin)
            ),
            textDecoration = if (workout.isCompleted) {
                TextDecoration.LineThrough
            } else {
                null
            }
        )
    }
}

@Composable
private fun WorkoutsEmptyContent(
        @StringRes noWorkoutsLabel: Int,
        @DrawableRes noWorkoutsIconRes: Int,
        modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = noWorkoutsIconRes),
            contentDescription = stringResource(R.string.no_workouts_image_content_description),
            modifier = Modifier.size(96.dp)
        )
        Text(stringResource(id = noWorkoutsLabel))
    }
}

@Preview
@Composable
private fun WorkoutsContentPreview() {
    AppCompatTheme {
        Surface {
            WorkoutsContent(
                loading = false,
                workouts = listOf(
                    Workout(
                        title = "Title 1",
                        description = "Description 1",
                        isCompleted = false,
                        id = "ID 1"
                    ),
                    Workout(
                        title = "Title 2",
                        description = "Description 2",
                        isCompleted = true,
                        id = "ID 2"
                    ),
                    Workout(
                        title = "Title 3",
                        description = "Description 3",
                        isCompleted = true,
                        id = "ID 3"
                    ),
                    Workout(
                        title = "Title 4",
                        description = "Description 4",
                        isCompleted = false,
                        id = "ID 4"
                    ),
                    Workout(
                        title = "Title 5",
                        description = "Description 5",
                        isCompleted = true,
                        id = "ID 5"
                    ),
                ),
                currentFilteringLabel = R.string.label_all,
                noWorkoutsLabel = R.string.no_workouts_all,
                noWorkoutsIconRes = R.drawable.logo_no_fill,
                onRefresh = { },
                onWorkoutClick = { },
                onWorkoutCheckedChange = { _, _ -> },
            )
        }
    }
}

@Preview
@Composable
private fun WorkoutsContentEmptyPreview() {
    AppCompatTheme {
        Surface {
            WorkoutsContent(
                loading = false,
                workouts = emptyList(),
                currentFilteringLabel = R.string.label_all,
                noWorkoutsLabel = R.string.no_workouts_all,
                noWorkoutsIconRes = R.drawable.logo_no_fill,
                onRefresh = { },
                onWorkoutClick = { },
                onWorkoutCheckedChange = { _, _ -> },
            )
        }
    }
}

@Preview
@Composable
private fun WorkoutsEmptyContentPreview() {
    AppCompatTheme {
        Surface {
            WorkoutsEmptyContent(
                noWorkoutsLabel = R.string.no_workouts_all,
                noWorkoutsIconRes = R.drawable.logo_no_fill
            )
        }
    }
}

@Preview
@Composable
private fun WorkoutItemPreview() {
    AppCompatTheme {
        Surface {
            WorkoutItem(
                workout = Workout(
                    title = "Title",
                    description = "Description",
                    id = "ID"
                ),
                onWorkoutClick = { },
                onCheckedChange = { }
            )
        }
    }
}

@Preview
@Composable
private fun WorkoutItemCompletedPreview() {
    AppCompatTheme {
        Surface {
            WorkoutItem(
                workout = Workout(
                    title = "Title",
                    description = "Description",
                    isCompleted = true,
                    id = "ID"
                ),
                onWorkoutClick = { },
                onCheckedChange = { }
            )
        }
    }
}
