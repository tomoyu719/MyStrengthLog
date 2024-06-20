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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.architecture.blueprints.todoapp.ADD_EDIT_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.DELETE_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.EDIT_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.Workout
import com.example.android.architecture.blueprints.todoapp.data.WorkoutRepository
import com.example.android.architecture.blueprints.todoapp.workouts.WorkoutsFilterType.ACTIVE_WORKOUTS
import com.example.android.architecture.blueprints.todoapp.workouts.WorkoutsFilterType.ALL_WORKOUTS
import com.example.android.architecture.blueprints.todoapp.workouts.WorkoutsFilterType.COMPLETED_WORKOUTS
import com.example.android.architecture.blueprints.todoapp.util.Async
import com.example.android.architecture.blueprints.todoapp.util.WhileUiSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UiState for the Workout list screen.
 */
data class WorkoutsUiState(
        val items: List<Workout> = emptyList(),
        val isLoading: Boolean = false,
        val filteringUiInfo: FilteringUiInfo = FilteringUiInfo(),
        val userMessage: Int? = null
)

/**
 * ViewModel for the Workout list screen.
 */
@HiltViewModel
class WorkoutsViewModel @Inject constructor(
        private val workoutRepository: WorkoutRepository,
        private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _savedFilterType =
        savedStateHandle.getStateFlow(WORKOUTS_FILTER_SAVED_STATE_KEY, ALL_WORKOUTS)

    private val _filterUiInfo = _savedFilterType.map { getFilterUiInfo(it) }.distinctUntilChanged()
    private val _userMessage: MutableStateFlow<Int?> = MutableStateFlow(null)
    private val _isLoading = MutableStateFlow(false)
    private val _filteredWorkoutsAsync =
        combine(workoutRepository.getWorkoutsStream(), _savedFilterType) { workouts, type ->
            filterWorkouts(workouts, type)
        }
            .map { Async.Success(it) }
            .catch<Async<List<Workout>>> { emit(Async.Error(R.string.loading_workouts_error)) }

    val uiState: StateFlow<WorkoutsUiState> = combine(
        _filterUiInfo, _isLoading, _userMessage, _filteredWorkoutsAsync
    ) { filterUiInfo, isLoading, userMessage, workoutsAsync ->
        when (workoutsAsync) {
            Async.Loading -> {
                WorkoutsUiState(isLoading = true)
            }
            is Async.Error -> {
                WorkoutsUiState(userMessage = workoutsAsync.errorMessage)
            }
            is Async.Success -> {
                WorkoutsUiState(
                    items = workoutsAsync.data,
                    filteringUiInfo = filterUiInfo,
                    isLoading = isLoading,
                    userMessage = userMessage
                )
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = WhileUiSubscribed,
            initialValue = WorkoutsUiState(isLoading = true)
        )

    fun setFiltering(requestType: WorkoutsFilterType) {
        savedStateHandle[WORKOUTS_FILTER_SAVED_STATE_KEY] = requestType
    }

    fun clearCompletedWorkouts() {
        viewModelScope.launch {
            workoutRepository.clearCompletedWorkouts()
            showSnackbarMessage(R.string.completed_workouts_cleared)
            refresh()
        }
    }

    fun completeWorkout(workout: Workout, completed: Boolean) = viewModelScope.launch {
        if (completed) {
            workoutRepository.completeWorkout(workout.id)
            showSnackbarMessage(R.string.workout_marked_complete)
        } else {
            workoutRepository.activateWorkout(workout.id)
            showSnackbarMessage(R.string.workout_marked_active)
        }
    }

    fun showEditResultMessage(result: Int) {
        when (result) {
            EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_saved_workout_message)
            ADD_EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_added_workout_message)
            DELETE_RESULT_OK -> showSnackbarMessage(R.string.successfully_deleted_workout_message)
        }
    }

    fun snackbarMessageShown() {
        _userMessage.value = null
    }

    private fun showSnackbarMessage(message: Int) {
        _userMessage.value = message
    }

    fun refresh() {
        _isLoading.value = true
        viewModelScope.launch {
            workoutRepository.refresh()
            _isLoading.value = false
        }
    }

    private fun filterWorkouts(workouts: List<Workout>, filteringType: WorkoutsFilterType): List<Workout> {
        val workoutsToShow = ArrayList<Workout>()
        // We filter the workouts based on the requestType
        for (workout in workouts) {
            when (filteringType) {
                ALL_WORKOUTS -> workoutsToShow.add(workout)
                ACTIVE_WORKOUTS -> if (workout.isActive) {
                    workoutsToShow.add(workout)
                }
                COMPLETED_WORKOUTS -> if (workout.isCompleted) {
                    workoutsToShow.add(workout)
                }
            }
        }
        return workoutsToShow
    }

    private fun getFilterUiInfo(requestType: WorkoutsFilterType): FilteringUiInfo =
        when (requestType) {
            ALL_WORKOUTS -> {
                FilteringUiInfo(
                    R.string.label_all, R.string.no_workouts_all,
                    R.drawable.logo_no_fill
                )
            }
            ACTIVE_WORKOUTS -> {
                FilteringUiInfo(
                    R.string.label_active, R.string.no_workouts_active,
                    R.drawable.ic_check_circle_96dp
                )
            }
            COMPLETED_WORKOUTS -> {
                FilteringUiInfo(
                    R.string.label_completed, R.string.no_workouts_completed,
                    R.drawable.ic_verified_user_96dp
                )
            }
        }
}

// Used to save the current filtering in SavedStateHandle.
const val WORKOUTS_FILTER_SAVED_STATE_KEY = "WORKOUTS_FILTER_SAVED_STATE_KEY"

data class FilteringUiInfo(
        val currentFilteringLabel: Int = R.string.label_all,
        val noWorkoutsLabel: Int = R.string.no_workouts_all,
        val noWorkoutIconRes: Int = R.drawable.logo_no_fill,
)
