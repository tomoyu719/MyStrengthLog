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

import com.example.android.architecture.blueprints.todoapp.data.source.local.WorkoutDao
import com.example.android.architecture.blueprints.todoapp.data.source.network.NetworkDataSource
import com.example.android.architecture.blueprints.todoapp.di.ApplicationScope
import com.example.android.architecture.blueprints.todoapp.di.DefaultDispatcher
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Default implementation of [WorkoutRepository]. Single entry point for managing workouts' data.
 *
 * @param networkDataSource - The network data source
 * @param localDataSource - The local data source
 * @param dispatcher - The dispatcher to be used for long running or complex operations, such as ID
 * generation or mapping many models.
 * @param scope - The coroutine scope used for deferred jobs where the result isn't important, such
 * as sending data to the network.
 */
@Singleton
class DefaultWorkoutRepository @Inject constructor(
        private val networkDataSource: NetworkDataSource,
        private val localDataSource: WorkoutDao,
        @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
        @ApplicationScope private val scope: CoroutineScope,
) : WorkoutRepository {

    override suspend fun createWorkout(title: String, description: String): String {
        // ID creation might be a complex operation so it's executed using the supplied
        // coroutine dispatcher
        val workoutId = withContext(dispatcher) {
            UUID.randomUUID().toString()
        }
        val workout = Workout(
            title = title,
            description = description,
            id = workoutId,
        )
        localDataSource.upsert(workout.toLocal())
        saveWorkoutsToNetwork()
        return workoutId
    }

    override suspend fun updateWorkout(workoutId: String, title: String, description: String) {
        val workout = getWorkout(workoutId)?.copy(
            title = title,
            description = description
        ) ?: throw Exception("Workout (id $workoutId) not found")

        localDataSource.upsert(workout.toLocal())
        saveWorkoutsToNetwork()
    }

    override suspend fun getWorkouts(forceUpdate: Boolean): List<Workout> {
        if (forceUpdate) {
            refresh()
        }
        return withContext(dispatcher) {
            localDataSource.getAll().toExternal()
        }
    }

    override fun getWorkoutsStream(): Flow<List<Workout>> {
        return localDataSource.observeAll().map { workouts ->
            withContext(dispatcher) {
                workouts.toExternal()
            }
        }
    }

    override suspend fun refreshWorkout(workoutId: String) {
        refresh()
    }

    override fun getWorkoutStream(workoutId: String): Flow<Workout?> {
        return localDataSource.observeById(workoutId).map { it.toExternal() }
    }

    /**
     * Get a Workout with the given ID. Will return null if the workout cannot be found.
     *
     * @param workoutId - The ID of the workout
     * @param forceUpdate - true if the workout should be updated from the network data source first.
     */
    override suspend fun getWorkout(workoutId: String, forceUpdate: Boolean): Workout? {
        if (forceUpdate) {
            refresh()
        }
        return localDataSource.getById(workoutId)?.toExternal()
    }

    override suspend fun completeWorkout(workoutId: String) {
        localDataSource.updateCompleted(workoutId = workoutId, completed = true)
        saveWorkoutsToNetwork()
    }

    override suspend fun activateWorkout(workoutId: String) {
        localDataSource.updateCompleted(workoutId = workoutId, completed = false)
        saveWorkoutsToNetwork()
    }

    override suspend fun clearCompletedWorkouts() {
        localDataSource.deleteCompleted()
        saveWorkoutsToNetwork()
    }

    override suspend fun deleteAllWorkouts() {
        localDataSource.deleteAll()
        saveWorkoutsToNetwork()
    }

    override suspend fun deleteWorkout(workoutId: String) {
        localDataSource.deleteById(workoutId)
        saveWorkoutsToNetwork()
    }

    /**
     * The following methods load workouts from (refresh), and save workouts to, the network.
     *
     * Real apps may want to do a proper sync, rather than the "one-way sync everything" approach
     * below. See https://developer.android.com/topic/architecture/data-layer/offline-first
     * for more efficient and robust synchronisation strategies.
     *
     * Note that the refresh operation is a suspend function (forces callers to wait) and the save
     * operation is not. It returns immediately so callers don't have to wait.
     */

    /**
     * Delete everything in the local data source and replace it with everything from the network
     * data source.
     *
     * `withContext` is used here in case the bulk `toLocal` mapping operation is complex.
     */
    override suspend fun refresh() {
        withContext(dispatcher) {
            val remoteWorkouts = networkDataSource.loadWorkouts()
            localDataSource.deleteAll()
            localDataSource.upsertAll(remoteWorkouts.toLocal())
        }
    }

    /**
     * Send the workouts from the local data source to the network data source
     *
     * Returns immediately after launching the job. Real apps may want to suspend here until the
     * operation is complete or (better) use WorkManager to schedule this work. Both approaches
     * should provide a mechanism for failures to be communicated back to the user so that
     * they are aware that their data isn't being backed up.
     */
    private fun saveWorkoutsToNetwork() {
        scope.launch {
            try {
                val localWorkouts = localDataSource.getAll()
                val networkWorkouts = withContext(dispatcher) {
                    localWorkouts.toNetwork()
                }
                networkDataSource.saveWorkouts(networkWorkouts)
            } catch (e: Exception) {
                // In a real app you'd handle the exception e.g. by exposing a `networkStatus` flow
                // to an app level UI state holder which could then display a Toast message.
            }
        }
    }
}
