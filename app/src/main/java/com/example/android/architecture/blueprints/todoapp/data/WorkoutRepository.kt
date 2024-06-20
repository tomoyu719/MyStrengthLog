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

import kotlinx.coroutines.flow.Flow

/**
 * Interface to the data layer.
 */
interface WorkoutRepository {

    fun getWorkoutsStream(): Flow<List<Workout>>

    suspend fun getWorkouts(forceUpdate: Boolean = false): List<Workout>

    suspend fun refresh()

    fun getWorkoutStream(workoutId: String): Flow<Workout?>

    suspend fun getWorkout(workoutId: String, forceUpdate: Boolean = false): Workout?

    suspend fun refreshWorkout(workoutId: String)

    suspend fun createWorkout(title: String, description: String): String

    suspend fun updateWorkout(workoutId: String, title: String, description: String)

    suspend fun completeWorkout(workoutId: String)

    suspend fun activateWorkout(workoutId: String)

    suspend fun clearCompletedWorkouts()

    suspend fun deleteAllWorkouts()

    suspend fun deleteWorkout(workoutId: String)
}
