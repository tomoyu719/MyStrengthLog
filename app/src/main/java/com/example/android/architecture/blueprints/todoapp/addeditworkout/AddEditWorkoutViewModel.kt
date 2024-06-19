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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.WorkoutDestinationsArgs
import com.example.android.architecture.blueprints.todoapp.data.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UiState for the Add/Edit screen
 */
data class AddEditWorkoutUiState(
        val title: String = "",
        val description: String = "",
        val isWorkoutCompleted: Boolean = false,
        val isLoading: Boolean = false,
        val userMessage: Int? = null,
        val isWorkoutSaved: Boolean = false
)

/**
 * ViewModel for the Add/Edit screen.
 */
@HiltViewModel
class AddEditWorkoutViewModel @Inject constructor(
        private val workoutRepository: WorkoutRepository,
        savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val workoutId: String? = savedStateHandle[WorkoutDestinationsArgs.WORKOUT_ID_ARG]

    // A MutableStateFlow needs to be created in this ViewModel. The source of truth of the current
    // editable Workout is the ViewModel, we need to mutate the UI state directly in methods such as
    // `updateTitle` or `updateDescription`
    private val _uiState = MutableStateFlow(AddEditWorkoutUiState())
    val uiState: StateFlow<AddEditWorkoutUiState> = _uiState.asStateFlow()

    init {
        if (workoutId != null) {
            loadWorkout(workoutId)
        }
    }

    // Called when clicking on fab.
    fun saveWorkout() {
        if (uiState.value.title.isEmpty() || uiState.value.description.isEmpty()) {
            _uiState.update {
                it.copy(userMessage = R.string.empty_workout_message)
            }
            return
        }

        if (workoutId == null) {
            createNewWorkout()
        } else {
            updateWorkout()
        }
    }

    fun snackbarMessageShown() {
        _uiState.update {
            it.copy(userMessage = null)
        }
    }

    fun updateTitle(newTitle: String) {
        _uiState.update {
            it.copy(title = newTitle)
        }
    }

    fun updateDescription(newDescription: String) {
        _uiState.update {
            it.copy(description = newDescription)
        }
    }

    private fun createNewWorkout() = viewModelScope.launch {
        workoutRepository.createWorkout(uiState.value.title, uiState.value.description)
        _uiState.update {
            it.copy(isWorkoutSaved = true)
        }
    }

    private fun updateWorkout() {
        if (workoutId == null) {
            throw RuntimeException("updateWorkout() was called but workout is new.")
        }
        viewModelScope.launch {
            workoutRepository.updateWorkout(
                workoutId,
                title = uiState.value.title,
                description = uiState.value.description,
            )
            _uiState.update {
                it.copy(isWorkoutSaved = true)
            }
        }
    }

    private fun loadWorkout(workoutId: String) {
        _uiState.update {
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            workoutRepository.getWorkout(workoutId).let { workout ->
                if (workout != null) {
                    _uiState.update {
                        it.copy(
                            title = workout.title,
                            description = workout.description,
                            isWorkoutCompleted = workout.isCompleted,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false)
                    }
                }
            }
        }
    }
}
