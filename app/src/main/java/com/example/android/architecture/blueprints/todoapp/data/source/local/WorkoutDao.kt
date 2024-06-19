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

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the workout table.
 */
@Dao
interface WorkoutDao {

    /**
     * Observes list of workouts.
     *
     * @return all workouts.
     */
    @Query("SELECT * FROM workout")
    fun observeAll(): Flow<List<LocalWorkout>>

    /**
     * Observes a single workout.
     *
     * @param workoutId the  id.
     * @return the  with Id.
     */
    @Query("SELECT * FROM workout WHERE id = :workoutId")
    fun observeById(workoutId: String): Flow<LocalWorkout>

    /**
     * Select all s from the s table.
     *
     * @return all s.
     */
    @Query("SELECT * FROM workout")
    suspend fun getAll(): List<LocalWorkout>

    /**
     * Select a workout by id.
     *
     * @param workoutId the workout id.
     * @return the workout with workoutId.
     */
    @Query("SELECT * FROM workout WHERE id = :workoutId")
    suspend fun getById(workoutId: String): LocalWorkout?

    /**
     * Insert or update a workout in the database. If a workout already exists, replace it.
     *
     * @param workout the workout to be inserted or updated.
     */
    @Upsert
    suspend fun upsert(workout: LocalWorkout)

    /**
     * Insert or update workouts in the database. If a workout already exists, replace it.
     *
     * @param workouts the workouts to be inserted or updated.
     */
    @Upsert
    suspend fun upsertAll(workouts: List<LocalWorkout>)

    /**
     * Update the complete status of a workout
     *
     * @param workoutId id of the workout
     * @param completed status to be updated
     */
    @Query("UPDATE workout SET isCompleted = :completed WHERE id = :workoutId")
    suspend fun updateCompleted(workoutId: String, completed: Boolean)

    /**
     * Delete a workout by id.
     *
     * @return the number of workouts deleted. This should always be 1.
     */
    @Query("DELETE FROM workout WHERE id = :workoutId")
    suspend fun deleteById(workoutId: String): Int

    /**
     * Delete all workouts.
     */
    @Query("DELETE FROM workout")
    suspend fun deleteAll()

    /**
     * Delete all completed workouts from the table.
     *
     * @return the number of workouts deleted.
     */
    @Query("DELETE FROM workout WHERE isCompleted = 1")
    suspend fun deleteCompleted(): Int
}
