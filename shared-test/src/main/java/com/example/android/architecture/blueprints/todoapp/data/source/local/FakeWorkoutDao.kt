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

package com.example.android.architecture.blueprints.todoapp.data.source.local

import kotlinx.coroutines.flow.Flow

class FakeWorkoutDao(initialWorkouts: List<LocalWorkout>? = emptyList()) : WorkoutDao {

    private var _workouts: MutableMap<String, LocalWorkout>? = null

    var workouts: List<LocalWorkout>?
        get() = _workouts?.values?.toList()
        set(newWorkouts) {
            _workouts = newWorkouts?.associateBy { it.id }?.toMutableMap()
        }

    init {
        workouts = initialWorkouts
    }

    override suspend fun getAll() = workouts ?: throw Exception("Workout list is null")

    override suspend fun getById(workoutId: String): LocalWorkout? = _workouts?.get(workoutId)

    override suspend fun upsertAll(workouts: List<LocalWorkout>) {
        _workouts?.putAll(workouts.associateBy { it.id })
    }

    override suspend fun upsert(workout: LocalWorkout) {
        _workouts?.put(workout.id, workout)
    }

    override suspend fun updateCompleted(workoutId: String, completed: Boolean) {
        _workouts?.get(workoutId)?.let { it.isCompleted = completed }
    }

    override suspend fun deleteAll() {
        _workouts?.clear()
    }

    override suspend fun deleteById(workoutId: String): Int {
        return if (_workouts?.remove(workoutId) == null) {
            0
        } else {
            1
        }
    }

    override suspend fun deleteCompleted(): Int {
        _workouts?.apply {
            val originalSize = size
            entries.removeIf { it.value.isCompleted }
            return originalSize - size
        }
        return 0
    }

    override fun observeAll(): Flow<List<LocalWorkout>> {
        TODO("Not implemented")
    }

    override fun observeById(workoutId: String): Flow<LocalWorkout> {
        TODO("Not implemented")
    }
}
