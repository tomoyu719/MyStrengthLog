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

import com.example.android.architecture.blueprints.todoapp.data.source.local.FakeWorkoutDao
import com.example.android.architecture.blueprints.todoapp.data.source.network.FakeNetworkDataSource
import com.google.common.truth.Truth.assertThat
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for the implementation of the in-memory repository with cache.
 */
@ExperimentalCoroutinesApi
class DefaultWorkoutRepositoryTest {

    private val workout1 = Workout(id = "1", title = "Title1", description = "Description1")
    private val workout2 = Workout(id = "2", title = "Title2", description = "Description2")
    private val workout3 = Workout(id = "3", title = "Title3", description = "Description3")

    private val newWorkoutTitle = "Title new"
    private val newWorkoutDescription = "Description new"
    private val newWorkout = Workout(id = "new", title = newWorkoutTitle, description = newWorkoutDescription)
    private val newWorkouts = listOf(newWorkout)

    private val networkWorkouts = listOf(workout1, workout2).toNetwork()
    private val localWorkouts = listOf(workout3.toLocal())

    // Test dependencies
    private lateinit var networkDataSource: FakeNetworkDataSource
    private lateinit var localDataSource: FakeWorkoutDao

    private var testDispatcher = UnconfinedTestDispatcher()
    private var testScope = TestScope(testDispatcher)

    // Class under test
    private lateinit var workoutRepository: DefaultWorkoutRepository

    @ExperimentalCoroutinesApi
    @Before
    fun createRepository() {
        networkDataSource = FakeNetworkDataSource(networkWorkouts.toMutableList())
        localDataSource = FakeWorkoutDao(localWorkouts)
        // Get a reference to the class under test
        workoutRepository = DefaultWorkoutRepository(
            networkDataSource = networkDataSource,
            localDataSource = localDataSource,
            dispatcher = testDispatcher,
            scope = testScope
        )
    }

    @ExperimentalCoroutinesApi
    @Test
    fun getWorkouts_emptyRepositoryAndUninitializedCache() = testScope.runTest {
        networkDataSource.workouts?.clear()
        localDataSource.deleteAll()

        assertThat(workoutRepository.getWorkouts().size).isEqualTo(0)
    }

    @Test
    fun getWorkouts_repositoryCachesAfterFirstApiCall() = testScope.runTest {
        // Trigger the repository to load workouts from the remote data source
        val initial = workoutRepository.getWorkouts(forceUpdate = true)

        // Change the remote data source
        networkDataSource.workouts = newWorkouts.toNetwork().toMutableList()

        // Load the workouts again without forcing a refresh
        val second = workoutRepository.getWorkouts()

        // Initial and second should match because we didn't force a refresh (no workouts were loaded
        // from the remote data source)
        assertThat(second).isEqualTo(initial)
    }

    @Test
    fun getWorkouts_requestsAllWorkoutsFromRemoteDataSource() = testScope.runTest {
        // When workouts are requested from the workouts repository
        val workouts = workoutRepository.getWorkouts(true)

        // Then workouts are loaded from the remote data source
        assertThat(workouts).isEqualTo(networkWorkouts.toExternal())
    }

    @Test
    fun saveWorkout_savesToLocalAndRemote() = testScope.runTest {
        // When a workout is saved to the workouts repository
        val newWorkoutId = workoutRepository.createWorkout(newWorkout.title, newWorkout.description)

        // Then the remote and local sources contain the new workout
        assertThat(networkDataSource.workouts?.map { it.id }?.contains(newWorkoutId))
        assertThat(localDataSource.workouts?.map { it.id }?.contains(newWorkoutId))
    }

    @Test
    fun getWorkouts_WithDirtyCache_workoutsAreRetrievedFromRemote() = testScope.runTest {
        // First call returns from REMOTE
        val workouts = workoutRepository.getWorkouts()

        // Set a different list of workouts in REMOTE
        networkDataSource.workouts = newWorkouts.toNetwork().toMutableList()

        // But if workouts are cached, subsequent calls load from cache
        val cachedWorkouts = workoutRepository.getWorkouts()
        assertThat(cachedWorkouts).isEqualTo(workouts)

        // Now force remote loading
        val refreshedWorkouts = workoutRepository.getWorkouts(true)

        // Workouts must be the recently updated in REMOTE
        assertThat(refreshedWorkouts).isEqualTo(newWorkouts)
    }

    @Test(expected = Exception::class)
    fun getWorkouts_WithDirtyCache_remoteUnavailable_throwsException() = testScope.runTest {
        // Make remote data source unavailable
        networkDataSource.workouts = null

        // Load workouts forcing remote load
        workoutRepository.getWorkouts(true)

        // Exception should be thrown
    }

    @Test
    fun getWorkouts_WithRemoteDataSourceUnavailable_workoutsAreRetrievedFromLocal() =
        testScope.runTest {
            // When the remote data source is unavailable
            networkDataSource.workouts = null

            // The repository fetches from the local source
            assertThat(workoutRepository.getWorkouts()).isEqualTo(localWorkouts.toExternal())
        }

    @Test(expected = Exception::class)
    fun getWorkouts_WithBothDataSourcesUnavailable_throwsError() = testScope.runTest {
        // When both sources are unavailable
        networkDataSource.workouts = null
        localDataSource.workouts = null

        // The repository throws an error
        workoutRepository.getWorkouts()
    }

    @Test
    fun getWorkouts_refreshesLocalDataSource() = testScope.runTest {
        // Forcing an update will fetch workouts from remote
        val expectedWorkouts = networkWorkouts.toExternal()

        val newWorkouts = workoutRepository.getWorkouts(true)

        assertEquals(expectedWorkouts, newWorkouts)
        assertEquals(expectedWorkouts, localDataSource.workouts?.toExternal())
    }

    @Test
    fun completeWorkout_completesWorkoutToServiceAPIUpdatesCache() = testScope.runTest {
        // Save a workout
        val newWorkoutId = workoutRepository.createWorkout(newWorkout.title, newWorkout.description)

        // Make sure it's active
        assertThat(workoutRepository.getWorkout(newWorkoutId)?.isCompleted).isFalse()

        // Mark is as complete
        workoutRepository.completeWorkout(newWorkoutId)

        // Verify it's now completed
        assertThat(workoutRepository.getWorkout(newWorkoutId)?.isCompleted).isTrue()
    }

    @Test
    fun completeWorkout_activeWorkoutToServiceAPIUpdatesCache() = testScope.runTest {
        // Save a workout
        val newWorkoutId = workoutRepository.createWorkout(newWorkout.title, newWorkout.description)
        workoutRepository.completeWorkout(newWorkoutId)

        // Make sure it's completed
        assertThat(workoutRepository.getWorkout(newWorkoutId)?.isActive).isFalse()

        // Mark is as active
        workoutRepository.activateWorkout(newWorkoutId)

        // Verify it's now activated
        assertThat(workoutRepository.getWorkout(newWorkoutId)?.isActive).isTrue()
    }

    @Test
    fun getWorkout_repositoryCachesAfterFirstApiCall() = testScope.runTest {
        // Obtain a workout from the local data source
        localDataSource = FakeWorkoutDao(mutableListOf(workout1.toLocal()))
        val initial = workoutRepository.getWorkout(workout1.id)

        // Change the workouts on the remote
        networkDataSource.workouts = newWorkouts.toNetwork().toMutableList()

        // Obtain the same workout again
        val second = workoutRepository.getWorkout(workout1.id)

        // Initial and second workouts should match because we didn't force a refresh
        assertThat(second).isEqualTo(initial)
    }

    @Test
    fun getWorkout_forceRefresh() = testScope.runTest {
        // Trigger the repository to load data, which loads from remote and caches
        networkDataSource.workouts = mutableListOf(workout1.toNetwork())
        val workout1FirstTime = workoutRepository.getWorkout(workout1.id, forceUpdate = true)
        assertThat(workout1FirstTime?.id).isEqualTo(workout1.id)

        // Configure the remote data source to return a different workout
        networkDataSource.workouts = mutableListOf(workout2.toNetwork())

        // Force refresh
        val workout1SecondTime = workoutRepository.getWorkout(workout1.id, true)
        val workout2SecondTime = workoutRepository.getWorkout(workout2.id, true)

        // Only workout2 works because workout1 does not exist on the remote
        assertThat(workout1SecondTime).isNull()
        assertThat(workout2SecondTime?.id).isEqualTo(workout2.id)
    }

    @Test
    fun clearCompletedWorkouts() = testScope.runTest {
        val completedWorkout = workout1.copy(isCompleted = true)
        localDataSource.workouts = listOf(completedWorkout.toLocal(), workout2.toLocal())
        workoutRepository.clearCompletedWorkouts()

        val workouts = workoutRepository.getWorkouts(true)

        assertThat(workouts).hasSize(1)
        assertThat(workouts).contains(workout2)
        assertThat(workouts).doesNotContain(completedWorkout)
    }

    @Test
    fun deleteAllWorkouts() = testScope.runTest {
        val initialWorkouts = workoutRepository.getWorkouts()

        // Verify workouts are returned
        assertThat(initialWorkouts.size).isEqualTo(1)

        // Delete all workouts
        workoutRepository.deleteAllWorkouts()

        // Verify workouts are empty now
        val afterDeleteWorkouts = workoutRepository.getWorkouts()
        assertThat(afterDeleteWorkouts).isEmpty()
    }

    @Test
    fun deleteSingleWorkout() = testScope.runTest {
        val initialWorkoutsSize = workoutRepository.getWorkouts(true).size

        // Delete first workout
        workoutRepository.deleteWorkout(workout1.id)

        // Fetch data again
        val afterDeleteWorkouts = workoutRepository.getWorkouts(true)

        // Verify only one workout was deleted
        assertThat(afterDeleteWorkouts.size).isEqualTo(initialWorkoutsSize - 1)
        assertThat(afterDeleteWorkouts).doesNotContain(workout1)
    }
}
