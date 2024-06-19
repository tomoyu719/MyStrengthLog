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

package com.example.android.architecture.blueprints.todoapp.workoutdetail

import androidx.lifecycle.SavedStateHandle
import com.example.android.architecture.blueprints.todoapp.MainCoroutineRule
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.WorkoutDestinationsArgs
import com.example.android.architecture.blueprints.todoapp.data.FakeWorkoutRepository
import com.example.android.architecture.blueprints.todoapp.data.Workout
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for the implementation of [WorkoutDetailViewModel]
 */
@ExperimentalCoroutinesApi
class WorkoutDetailViewModelTest {

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    // Subject under test
    private lateinit var workoutDetailViewModel: WorkoutDetailViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var workoutsRepository: FakeWorkoutRepository
    private val workout = Workout(title = "Title1", description = "Description1", id = "0")

    @Before
    fun setupViewModel() {
        workoutsRepository = FakeWorkoutRepository()
        workoutsRepository.addWorkouts(workout)

        workoutDetailViewModel = WorkoutDetailViewModel(
            workoutsRepository,
            SavedStateHandle(mapOf(WorkoutDestinationsArgs.WORKOUT_ID_ARG to "0"))
        )
    }

    @Test
    fun getActiveWorkoutFromRepositoryAndLoadIntoView() = runTest {
        val uiState = workoutDetailViewModel.uiState.first()
        // Then verify that the view was notified
        assertThat(uiState.workout?.title).isEqualTo(workout.title)
        assertThat(uiState.workout?.description).isEqualTo(workout.description)
    }

    @Test
    fun completeWorkout() = runTest {
        // Verify that the workout was active initially
        assertThat(workoutsRepository.savedWorkouts.value[workout.id]?.isCompleted).isFalse()

        // When the ViewModel is asked to complete the workout
        assertThat(workoutDetailViewModel.uiState.first().workout?.id).isEqualTo("0")
        workoutDetailViewModel.setCompleted(true)

        // Then the workout is completed and the snackbar shows the correct message
        assertThat(workoutsRepository.savedWorkouts.value[workout.id]?.isCompleted).isTrue()
        assertThat(workoutDetailViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.workout_marked_complete)
    }

    @Test
    fun activateWorkout() = runTest {
        workoutsRepository.deleteAllWorkouts()
        workoutsRepository.addWorkouts(workout.copy(isCompleted = true))

        // Verify that the workout was completed initially
        assertThat(workoutsRepository.savedWorkouts.value[workout.id]?.isCompleted).isTrue()

        // When the ViewModel is asked to complete the workout
        assertThat(workoutDetailViewModel.uiState.first().workout?.id).isEqualTo("0")
        workoutDetailViewModel.setCompleted(false)

        // Then the workout is not completed and the snackbar shows the correct message
        val newWorkout = workoutsRepository.getWorkout(workout.id)
        assertTrue((newWorkout?.isActive) ?: false)
        assertThat(workoutDetailViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.workout_marked_active)
    }

    @Test
    fun workoutDetailViewModel_repositoryError() = runTest {
        // Given a repository that throws errors
        workoutsRepository.setShouldThrowError(true)

        // Then the workout is null and the snackbar shows a loading error message
        assertThat(workoutDetailViewModel.uiState.value.workout).isNull()
        assertThat(workoutDetailViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.loading_workout_error)
    }

    @Test
    fun workoutDetailViewModel_workoutNotFound() = runTest {
        // Given an ID for a non existent workout
        workoutDetailViewModel = WorkoutDetailViewModel(
            workoutsRepository,
            SavedStateHandle(mapOf(WorkoutDestinationsArgs.WORKOUT_ID_ARG to "nonexistent_id"))
        )

        // The workout is null and the snackbar shows a "not found" error message
        assertThat(workoutDetailViewModel.uiState.value.workout).isNull()
        assertThat(workoutDetailViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.workout_not_found)
    }

    @Test
    fun deleteWorkout() = runTest {
        assertThat(workoutsRepository.savedWorkouts.value.containsValue(workout)).isTrue()

        // When the deletion of a workout is requested
        workoutDetailViewModel.deleteWorkout()

        assertThat(workoutsRepository.savedWorkouts.value.containsValue(workout)).isFalse()
    }

    @Test
    fun loadWorkout_loading() = runTest {
        // Set Main dispatcher to not run coroutines eagerly, for just this one test
        Dispatchers.setMain(StandardTestDispatcher())

        var isLoading: Boolean? = true
        val job = launch {
            workoutDetailViewModel.uiState.collect {
                isLoading = it.isLoading
            }
        }

        // Then progress indicator is shown
        assertThat(isLoading).isTrue()

        // Execute pending coroutines actions
        advanceUntilIdle()

        // Then progress indicator is hidden
        assertThat(isLoading).isFalse()
        job.cancel()
    }
}
