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

package com.example.android.architecture.blueprints.todoapp.addeditworkout

import androidx.lifecycle.SavedStateHandle
import com.example.android.architecture.blueprints.todoapp.MainCoroutineRule
import com.example.android.architecture.blueprints.todoapp.R.string
import com.example.android.architecture.blueprints.todoapp.WorkoutDestinationsArgs
import com.example.android.architecture.blueprints.todoapp.data.FakeWorkoutRepository
import com.example.android.architecture.blueprints.todoapp.data.Workout
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for the implementation of [AddEditWorkoutViewModel].
 */
@ExperimentalCoroutinesApi
class AddEditWorkoutViewModelTest {

    // Subject under test
    private lateinit var addEditWorkoutViewModel: AddEditWorkoutViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var workoutsRepository: FakeWorkoutRepository
    private val workout = Workout(title = "Title1", description = "Description1", id = "0")

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupViewModel() {
        // We initialise the repository with no Workouts
        workoutsRepository = FakeWorkoutRepository().apply {
            addWorkouts(workout)
        }
    }

    @Test
    fun saveNewWorkoutToRepository_showsSuccessMessageUi() {
        addEditWorkoutViewModel = AddEditWorkoutViewModel(
            workoutsRepository,
            SavedStateHandle(mapOf(WorkoutDestinationsArgs.WORKOUT_ID_ARG to "0"))
        )

        val newTitle = "New Workout Title"
        val newDescription = "Some Workout Description"
        addEditWorkoutViewModel.apply {
            updateTitle(newTitle)
            updateDescription(newDescription)
        }
        addEditWorkoutViewModel.saveWorkout()

        val newWorkout = workoutsRepository.savedWorkouts.value.values.first()

        // Then a Workout is saved in the repository and the view updated
        assertThat(newWorkout.title).isEqualTo(newTitle)
        assertThat(newWorkout.description).isEqualTo(newDescription)
    }

    @Test
    fun loadWorkouts_loading() = runTest {
        // Set Main dispatcher to not run coroutines eagerly, for just this one test
        Dispatchers.setMain(StandardTestDispatcher())

        addEditWorkoutViewModel = AddEditWorkoutViewModel(
            workoutsRepository,
            SavedStateHandle(mapOf(WorkoutDestinationsArgs.WORKOUT_ID_ARG to "0"))
        )

        // Then progress indicator is shown
        assertThat(addEditWorkoutViewModel.uiState.value.isLoading).isTrue()

        // Execute pending coroutines actions
        advanceUntilIdle()

        // Then progress indicator is hidden
        assertThat(addEditWorkoutViewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun loadWorkouts_workoutShown() {
        addEditWorkoutViewModel = AddEditWorkoutViewModel(
            workoutsRepository,
            SavedStateHandle(mapOf(WorkoutDestinationsArgs.WORKOUT_ID_ARG to "0"))
        )

        // Add workout to repository
        workoutsRepository.addWorkouts(workout)

        // Verify a workout is loaded
        val uiState = addEditWorkoutViewModel.uiState.value
        assertThat(uiState.title).isEqualTo(workout.title)
        assertThat(uiState.description).isEqualTo(workout.description)
        assertThat(uiState.isLoading).isFalse()
    }

    @Test
    fun saveNewWorkoutToRepository_emptyTitle_error() {
        addEditWorkoutViewModel = AddEditWorkoutViewModel(
            workoutsRepository,
            SavedStateHandle(mapOf(WorkoutDestinationsArgs.WORKOUT_ID_ARG to "0"))
        )

        saveWorkoutAndAssertUserMessage("", "Some Workout Description")
    }

    @Test
    fun saveNewWorkoutToRepository_emptyDescription_error() {
        addEditWorkoutViewModel = AddEditWorkoutViewModel(
            workoutsRepository,
            SavedStateHandle(mapOf(WorkoutDestinationsArgs.WORKOUT_ID_ARG to "0"))
        )

        saveWorkoutAndAssertUserMessage("Title", "")
    }

    @Test
    fun saveNewWorkoutToRepository_emptyDescriptionEmptyTitle_error() {
        addEditWorkoutViewModel = AddEditWorkoutViewModel(
            workoutsRepository,
            SavedStateHandle(mapOf(WorkoutDestinationsArgs.WORKOUT_ID_ARG to "0"))
        )

        saveWorkoutAndAssertUserMessage("", "")
    }

    private fun saveWorkoutAndAssertUserMessage(title: String, description: String) {
        addEditWorkoutViewModel.apply {
            updateTitle(title)
            updateDescription(description)
        }

        // When saving an incomplete workout
        addEditWorkoutViewModel.saveWorkout()

        assertThat(
            addEditWorkoutViewModel.uiState.value.userMessage
        ).isEqualTo(string.empty_workout_message)
    }
}
