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

package com.example.android.architecture.blueprints.todoapp.workoutdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Checkbox
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.Workout
import com.example.android.architecture.blueprints.todoapp.util.LoadingContent
import com.example.android.architecture.blueprints.todoapp.util.WorkoutDetailTopAppBar
import com.google.accompanist.appcompattheme.AppCompatTheme

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun WorkoutDetailScreen(
        onEditWorkout: (String) -> Unit,
        onBack: () -> Unit,
        onDeleteWorkout: () -> Unit,
        modifier: Modifier = Modifier,
        viewModel: WorkoutDetailViewModel = hiltViewModel(),
        scaffoldState: ScaffoldState = rememberScaffoldState()
) {
    Scaffold(
        scaffoldState = scaffoldState,
        modifier = modifier.fillMaxSize(),
        topBar = {
            WorkoutDetailTopAppBar(onBack = onBack, onDelete = viewModel::deleteWorkout)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onEditWorkout(viewModel.workoutId) }) {
                Icon(Icons.Filled.Edit, stringResource(id = R.string.edit_workout))
            }
        }
    ) { paddingValues ->
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        EditWorkoutContent(
            loading = uiState.isLoading,
            empty = uiState.workout == null && !uiState.isLoading,
            workout = uiState.workout,
            onRefresh = viewModel::refresh,
            onWorkoutCheck = viewModel::setCompleted,
            modifier = Modifier.padding(paddingValues)
        )

        // Check for user messages to display on the screen
        uiState.userMessage?.let { userMessage ->
            val snackbarText = stringResource(userMessage)
            LaunchedEffect(scaffoldState, viewModel, userMessage, snackbarText) {
                scaffoldState.snackbarHostState.showSnackbar(snackbarText)
                viewModel.snackbarMessageShown()
            }
        }

        // Check if the workout is deleted and call onDeleteWorkout
        LaunchedEffect(uiState.isWorkoutDeleted) {
            if (uiState.isWorkoutDeleted) {
                onDeleteWorkout()
            }
        }
    }
}

@Composable
private fun EditWorkoutContent(
        loading: Boolean,
        empty: Boolean,
        workout: Workout?,
        onWorkoutCheck: (Boolean) -> Unit,
        onRefresh: () -> Unit,
        modifier: Modifier = Modifier
) {
    val screenPadding = Modifier.padding(
        horizontal = dimensionResource(id = R.dimen.horizontal_margin),
        vertical = dimensionResource(id = R.dimen.vertical_margin),
    )
    val commonModifier = modifier
        .fillMaxWidth()
        .then(screenPadding)

    LoadingContent(
        loading = loading,
        empty = empty,
        emptyContent = {
            Text(
                text = stringResource(id = R.string.no_data),
                modifier = commonModifier
            )
        },
        onRefresh = onRefresh
    ) {
        Column(commonModifier.verticalScroll(rememberScrollState())) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .then(screenPadding),

            ) {
                if (workout != null) {
                    Checkbox(workout.isCompleted, onWorkoutCheck)
                    Column {
                        Text(text = workout.title, style = MaterialTheme.typography.h6)
                        Text(text = workout.description, style = MaterialTheme.typography.body1)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun EditWorkoutContentPreview() {
    AppCompatTheme {
        Surface {
            EditWorkoutContent(
                loading = false,
                empty = false,
                Workout(
                    title = "Title",
                    description = "Description",
                    isCompleted = false,
                    id = "ID"
                ),
                onWorkoutCheck = { },
                onRefresh = { }
            )
        }
    }
}

@Preview
@Composable
private fun EditWorkoutContentWorkoutCompletedPreview() {
    AppCompatTheme {
        Surface {
            EditWorkoutContent(
                loading = false,
                empty = false,
                Workout(
                    title = "Title",
                    description = "Description",
                    isCompleted = false,
                    id = "ID"
                ),
                onWorkoutCheck = { },
                onRefresh = { }
            )
        }
    }
}

@Preview
@Composable
private fun EditWorkoutContentEmptyPreview() {
    AppCompatTheme {
        Surface {
            EditWorkoutContent(
                loading = false,
                empty = true,
                Workout(
                    title = "Title",
                    description = "Description",
                    isCompleted = false,
                    id = "ID"
                ),
                onWorkoutCheck = { },
                onRefresh = { }
            )
        }
    }
}
