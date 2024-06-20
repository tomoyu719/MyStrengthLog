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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.WorkoutDestinationsArgs
import com.example.android.architecture.blueprints.todoapp.data.Workout
import com.example.android.architecture.blueprints.todoapp.data.WorkoutRepository
import com.example.android.architecture.blueprints.todoapp.util.Async
import com.example.android.architecture.blueprints.todoapp.util.WhileUiSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UiState for the Details screen.
 */
data class WorkoutDetailUiState(
        val workout: Workout? = null,
        val isLoading: Boolean = false,
        val userMessage: Int? = null,
        val isWorkoutDeleted: Boolean = false
)

/**
 * ViewModel for the Details screen.
 */
@HiltViewModel
class WorkoutDetailViewModel @Inject constructor(
        private val workoutRepository: WorkoutRepository,
        savedStateHandle: SavedStateHandle
) : ViewModel() {

    val workoutId: String = savedStateHandle[WorkoutDestinationsArgs.WORKOUT_ID_ARG]!!

    private val _userMessage: MutableStateFlow<Int?> = MutableStateFlow(null)
    private val _isLoading = MutableStateFlow(false)
    private val _isWorkoutDeleted = MutableStateFlow(false)
    private val _workoutAsync = workoutRepository.getWorkoutStream(workoutId)
        .map { handleWorkout(it) }
        .catch { emit(Async.Error(R.string.loading_workout_error)) }

    val uiState: StateFlow<WorkoutDetailUiState> = combine(
        _userMessage, _isLoading, _isWorkoutDeleted, _workoutAsync
    ) { userMessage, isLoading, isWorkoutDeleted, workoutAsync ->
        when (workoutAsync) {
            Async.Loading -> {
                WorkoutDetailUiState(isLoading = true)
            }
            is Async.Error -> {
                WorkoutDetailUiState(
                    userMessage = workoutAsync.errorMessage,
                    isWorkoutDeleted = isWorkoutDeleted
                )
            }
            is Async.Success -> {
                WorkoutDetailUiState(
                    workout = workoutAsync.data,
                    isLoading = isLoading,
                    userMessage = userMessage,
                    isWorkoutDeleted = isWorkoutDeleted
                )
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = WhileUiSubscribed,
            initialValue = WorkoutDetailUiState(isLoading = true)
        )

    fun deleteWorkout() = viewModelScope.launch {
        workoutRepository.deleteWorkout(workoutId)
        _isWorkoutDeleted.value = true
    }

    fun setCompleted(completed: Boolean) = viewModelScope.launch {
        val workout = uiState.value.workout ?: return@launch
        if (completed) {
            workoutRepository.completeWorkout(workout.id)
            showSnackbarMessage(R.string.workout_marked_complete)
        } else {
            workoutRepository.activateWorkout(workout.id)
            showSnackbarMessage(R.string.workout_marked_active)
        }
    }

    fun refresh() {
        _isLoading.value = true
        viewModelScope.launch {
            workoutRepository.refreshWorkout(workoutId)
            _isLoading.value = false
        }
    }

    fun snackbarMessageShown() {
        _userMessage.value = null
    }

    private fun showSnackbarMessage(message: Int) {
        _userMessage.value = message
    }

    private fun handleWorkout(workout: Workout?): Async<Workout?> {
        if (workout == null) {
            return Async.Error(R.string.workout_not_found)
        }
        return Async.Success(workout)
    }
}
