/*
 * Copyright 2019 The Android Open Source Project
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

import androidx.lifecycle.SavedStateHandle
import com.example.android.architecture.blueprints.todoapp.ADD_EDIT_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.DELETE_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.EDIT_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.MainCoroutineRule
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.FakeWorkoutRepository
import com.example.android.architecture.blueprints.todoapp.data.Workout
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for the implementation of [WorkoutsViewModel]
 */
@ExperimentalCoroutinesApi
class WorkoutsViewModelTest {

    // Subject under test
    private lateinit var workoutsViewModel: WorkoutsViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var workoutsRepository: FakeWorkoutRepository

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupViewModel() {
        // We initialise the workouts to 3, with one active and two completed
        workoutsRepository = FakeWorkoutRepository()
        val workout1 = Workout(id = "1", title = "Title1", description = "Desc1")
        val workout2 = Workout(id = "2", title = "Title2", description = "Desc2", isCompleted = true)
        val workout3 = Workout(id = "3", title = "Title3", description = "Desc3", isCompleted = true)
        workoutsRepository.addWorkouts(workout1, workout2, workout3)

        workoutsViewModel = WorkoutsViewModel(workoutsRepository, SavedStateHandle())
    }

    @Test
    fun loadAllWorkoutsFromRepository_loadingTogglesAndDataLoaded() = runTest {
        // Set Main dispatcher to not run coroutines eagerly, for just this one test
        Dispatchers.setMain(StandardTestDispatcher())

        // Given an initialized WorkoutsViewModel with initialized workouts
        // When loading of Workouts is requested
        workoutsViewModel.setFiltering(WorkoutsFilterType.ALL_WORKOUTS)

        // Trigger loading of workouts
        workoutsViewModel.refresh()

        // Then progress indicator is shown
        assertThat(workoutsViewModel.uiState.first().isLoading).isTrue()

        // Execute pending coroutines actions
        advanceUntilIdle()

        // Then progress indicator is hidden
        assertThat(workoutsViewModel.uiState.first().isLoading).isFalse()

        // And data correctly loaded
        assertThat(workoutsViewModel.uiState.first().items).hasSize(3)
    }

    @Test
    fun loadActiveWorkoutsFromRepositoryAndLoadIntoView() = runTest {
        // Given an initialized WorkoutsViewModel with initialized workouts
        // When loading of Workouts is requested
        workoutsViewModel.setFiltering(WorkoutsFilterType.ACTIVE_WORKOUTS)

        // Load workouts
        workoutsViewModel.refresh()

        // Then progress indicator is hidden
        assertThat(workoutsViewModel.uiState.first().isLoading).isFalse()

        // And data correctly loaded
        assertThat(workoutsViewModel.uiState.first().items).hasSize(1)
    }

    @Test
    fun loadCompletedWorkoutsFromRepositoryAndLoadIntoView() = runTest {
        // Given an initialized WorkoutsViewModel with initialized workouts
        // When loading of Workouts is requested
        workoutsViewModel.setFiltering(WorkoutsFilterType.COMPLETED_WORKOUTS)

        // Load workouts
        workoutsViewModel.refresh()

        // Then progress indicator is hidden
        assertThat(workoutsViewModel.uiState.first().isLoading).isFalse()

        // And data correctly loaded
        assertThat(workoutsViewModel.uiState.first().items).hasSize(2)
    }

    @Test
    fun loadWorkouts_error() = runTest {
        // Make the repository throw errors
        workoutsRepository.setShouldThrowError(true)

        // Load workouts
        workoutsViewModel.refresh()

        // Then progress indicator is hidden
        assertThat(workoutsViewModel.uiState.first().isLoading).isFalse()

        // And the list of items is empty
        assertThat(workoutsViewModel.uiState.first().items).isEmpty()
        assertThat(workoutsViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.loading_workouts_error)
    }

    @Test
    fun clearCompletedWorkouts_clearsWorkouts() = runTest {
        // When completed workouts are cleared
        workoutsViewModel.clearCompletedWorkouts()

        // Fetch workouts
        workoutsViewModel.refresh()

        // Fetch workouts
        val allWorkouts = workoutsViewModel.uiState.first().items
        val completedWorkouts = allWorkouts?.filter { it.isCompleted }

        // Verify there are no completed workouts left
        assertThat(completedWorkouts).isEmpty()

        // Verify active workout is not cleared
        assertThat(allWorkouts).hasSize(1)

        // Verify snackbar is updated
        assertThat(workoutsViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.completed_workouts_cleared)
    }

    @Test
    fun showEditResultMessages_editOk_snackbarUpdated() = runTest {
        // When the viewmodel receives a result from another destination
        workoutsViewModel.showEditResultMessage(EDIT_RESULT_OK)

        // The snackbar is updated
        assertThat(workoutsViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.successfully_saved_workout_message)
    }

    @Test
    fun showEditResultMessages_addOk_snackbarUpdated() = runTest {
        // When the viewmodel receives a result from another destination
        workoutsViewModel.showEditResultMessage(ADD_EDIT_RESULT_OK)

        // The snackbar is updated
        assertThat(workoutsViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.successfully_added_workout_message)
    }

    @Test
    fun showEditResultMessages_deleteOk_snackbarUpdated() = runTest {
        // When the viewmodel receives a result from another destination
        workoutsViewModel.showEditResultMessage(DELETE_RESULT_OK)

        // The snackbar is updated
        assertThat(workoutsViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.successfully_deleted_workout_message)
    }

    @Test
    fun completeWorkout_dataAndSnackbarUpdated() = runTest {
        // With a repository that has an active workout
        val workout = Workout(id = "id", title = "Title", description = "Description")
        workoutsRepository.addWorkouts(workout)

        // Complete workout
        workoutsViewModel.completeWorkout(workout, true)

        // Verify the workout is completed
        assertThat(workoutsRepository.savedWorkouts.value[workout.id]?.isCompleted).isTrue()

        // The snackbar is updated
        assertThat(workoutsViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.workout_marked_complete)
    }

    @Test
    fun activateWorkout_dataAndSnackbarUpdated() = runTest {
        // With a repository that has a completed workout
        val workout = Workout(id = "id", title = "Title", description = "Description", isCompleted = true)
        workoutsRepository.addWorkouts(workout)

        // Activate workout
        workoutsViewModel.completeWorkout(workout, false)

        // Verify the workout is active
        assertThat(workoutsRepository.savedWorkouts.value[workout.id]?.isActive).isTrue()

        // The snackbar is updated
        assertThat(workoutsViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.workout_marked_active)
    }
}
