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

package com.example.android.architecture.blueprints.todoapp.data

import androidx.annotation.VisibleForTesting
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Implementation of a workouts repository with static access to the data for easy testing.
 */
class FakeWorkoutRepository : WorkoutRepository {

    private var shouldThrowError = false

    private val _savedWorkouts = MutableStateFlow(LinkedHashMap<String, Workout>())
    val savedWorkouts: StateFlow<LinkedHashMap<String, Workout>> = _savedWorkouts.asStateFlow()

    private val observableWorkouts: Flow<List<Workout>> = savedWorkouts.map {
        if (shouldThrowError) {
            throw Exception("Test exception")
        } else {
            it.values.toList()
        }
    }

    fun setShouldThrowError(value: Boolean) {
        shouldThrowError = value
    }

    override suspend fun refresh() {
        // Workouts already refreshed
    }

    override suspend fun refreshWorkout(workoutId: String) {
        refresh()
    }

    override suspend fun createWorkout(title: String, description: String): String {
        val workoutId = generateWorkoutId()
        Workout(title = title, description = description, id = workoutId).also {
            saveWorkout(it)
        }
        return workoutId
    }

    override fun getWorkoutsStream(): Flow<List<Workout>> = observableWorkouts

    override fun getWorkoutStream(workoutId: String): Flow<Workout?> {
        return observableWorkouts.map { workouts ->
            return@map workouts.firstOrNull { it.id == workoutId }
        }
    }

    override suspend fun getWorkout(workoutId: String, forceUpdate: Boolean): Workout? {
        if (shouldThrowError) {
            throw Exception("Test exception")
        }
        return savedWorkouts.value[workoutId]
    }

    override suspend fun getWorkouts(forceUpdate: Boolean): List<Workout> {
        if (shouldThrowError) {
            throw Exception("Test exception")
        }
        return observableWorkouts.first()
    }

    override suspend fun updateWorkout(workoutId: String, title: String, description: String) {
        val updatedWorkout = _savedWorkouts.value[workoutId]?.copy(
            title = title,
            description = description
        ) ?: throw Exception("Workout (id $workoutId) not found")

        saveWorkout(updatedWorkout)
    }

    private fun saveWorkout(workout: Workout) {
        _savedWorkouts.update { workouts ->
            val newWorkouts = LinkedHashMap<String, Workout>(workouts)
            newWorkouts[workout.id] = workout
            newWorkouts
        }
    }

    override suspend fun completeWorkout(workoutId: String) {
        _savedWorkouts.value[workoutId]?.let {
            saveWorkout(it.copy(isCompleted = true))
        }
    }

    override suspend fun activateWorkout(workoutId: String) {
        _savedWorkouts.value[workoutId]?.let {
            saveWorkout(it.copy(isCompleted = false))
        }
    }

    override suspend fun clearCompletedWorkouts() {
        _savedWorkouts.update { workouts ->
            workouts.filterValues {
                !it.isCompleted
            } as LinkedHashMap<String, Workout>
        }
    }

    override suspend fun deleteWorkout(workoutId: String) {
        _savedWorkouts.update { workouts ->
            val newWorkouts = LinkedHashMap<String, Workout>(workouts)
            newWorkouts.remove(workoutId)
            newWorkouts
        }
    }

    override suspend fun deleteAllWorkouts() {
        _savedWorkouts.update {
            LinkedHashMap()
        }
    }

    private fun generateWorkoutId() = UUID.randomUUID().toString()

    @VisibleForTesting
    fun addWorkouts(vararg workouts: Workout) {
        _savedWorkouts.update { oldWorkouts ->
            val newWorkouts = LinkedHashMap<String, Workout>(oldWorkouts)
            for (workout in workouts) {
                newWorkouts[workout.id] = workout
            }
            newWorkouts
        }
    }
}
