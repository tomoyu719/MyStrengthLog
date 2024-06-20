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

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class WorkoutDaoTest {

    // using an in-memory database because the information stored here disappears when the
    // process is killed
    private lateinit var database: MyStrengthLogDatabase

    // Ensure that we use a new database for each test.
    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            MyStrengthLogDatabase::class.java
        ).allowMainThreadQueries().build()
    }
    @Test
    fun insertWorkoutAndGetById() = runTest {
        // GIVEN - insert a workout
        val workout = LocalWorkout(
            title = "title",
            description = "description",
            id = "id",
            isCompleted = false,
        )
        database.workoutDao().upsert(workout)

        // WHEN - Get the workout by id from the database
        val loaded = database.workoutDao().getById(workout.id)

        // THEN - The loaded data contains the expected values
        assertNotNull(loaded as LocalWorkout)
        assertEquals(workout.id, loaded.id)
        assertEquals(workout.title, loaded.title)
        assertEquals(workout.description, loaded.description)
        assertEquals(workout.isCompleted, loaded.isCompleted)
    }

    @Test
    fun insertWorkoutReplacesOnConflict() = runTest {
        // Given that a workout is inserted
        val workout = LocalWorkout(
            title = "title",
            description = "description",
            id = "id",
            isCompleted = false,
        )
        database.workoutDao().upsert(workout)

        // When a workout with the same id is inserted
        val newWorkout = LocalWorkout(
            title = "title2",
            description = "description2",
            isCompleted = true,
            id = workout.id
        )
        database.workoutDao().upsert(newWorkout)

        // THEN - The loaded data contains the expected values
        val loaded = database.workoutDao().getById(workout.id)
        assertEquals(workout.id, loaded?.id)
        assertEquals("title2", loaded?.title)
        assertEquals("description2", loaded?.description)
        assertEquals(true, loaded?.isCompleted)
    }

    @Test
    fun insertWorkoutAndGetWorkouts() = runTest {
        // GIVEN - insert a workout
        val workout = LocalWorkout(
            title = "title",
            description = "description",
            id = "id",
            isCompleted = false,
        )
        database.workoutDao().upsert(workout)

        // WHEN - Get workouts from the database
        val workouts = database.workoutDao().getAll()

        // THEN - There is only 1 workout in the database, and contains the expected values
        assertEquals(1, workouts.size)
        assertEquals(workouts[0].id, workout.id)
        assertEquals(workouts[0].title, workout.title)
        assertEquals(workouts[0].description, workout.description)
        assertEquals(workouts[0].isCompleted, workout.isCompleted)
    }

    @Test
    fun updateWorkoutAndGetById() = runTest {
        // When inserting a workout
        val originalWorkout = LocalWorkout(
            title = "title",
            description = "description",
            id = "id",
            isCompleted = false,
        )

        database.workoutDao().upsert(originalWorkout)

        // When the workout is updated
        val updatedWorkout = LocalWorkout(
            title = "new title",
            description = "new description",
            isCompleted = true,
            id = originalWorkout.id
        )
        database.workoutDao().upsert(updatedWorkout)

        // THEN - The loaded data contains the expected values
        val loaded = database.workoutDao().getById(originalWorkout.id)
        assertEquals(originalWorkout.id, loaded?.id)
        assertEquals("new title", loaded?.title)
        assertEquals("new description", loaded?.description)
        assertEquals(true, loaded?.isCompleted)
    }

    @Test
    fun updateCompletedAndGetById() = runTest {
        // When inserting a workout
        val workout = LocalWorkout(
            title = "title",
            description = "description",
            id = "id",
            isCompleted = true
        )
        database.workoutDao().upsert(workout)

        // When the workout is updated
        database.workoutDao().updateCompleted(workout.id, false)

        // THEN - The loaded data contains the expected values
        val loaded = database.workoutDao().getById(workout.id)
        assertEquals(workout.id, loaded?.id)
        assertEquals(workout.title, loaded?.title)
        assertEquals(workout.description, loaded?.description)
        assertEquals(false, loaded?.isCompleted)
    }

    @Test
    fun deleteWorkoutByIdAndGettingWorkouts() = runTest {
        // Given a workout inserted
        val workout = LocalWorkout(
            title = "title",
            description = "description",
            id = "id",
            isCompleted = false,
        )
        database.workoutDao().upsert(workout)

        // When deleting a workout by id
        database.workoutDao().deleteById(workout.id)

        // THEN - The list is empty
        val workouts = database.workoutDao().getAll()
        assertEquals(true, workouts.isEmpty())
    }

    @Test
    fun deleteWorkoutsAndGettingWorkouts() = runTest {
        // Given a workout inserted
        database.workoutDao().upsert(
            LocalWorkout(
                title = "title",
                description = "description",
                id = "id",
                isCompleted = false,
            )
        )

        // When deleting all workouts
        database.workoutDao().deleteAll()

        // THEN - The list is empty
        val workouts = database.workoutDao().getAll()
        assertEquals(true, workouts.isEmpty())
    }

    @Test
    fun deleteCompletedWorkoutsAndGettingWorkouts() = runTest {
        // Given a completed workout inserted
        database.workoutDao().upsert(
            LocalWorkout(title = "completed", description = "workout", id = "id", isCompleted = true)
        )

        // When deleting completed workouts
        database.workoutDao().deleteCompleted()

        // THEN - The list is empty
        val workouts = database.workoutDao().getAll()
        assertEquals(true, workouts.isEmpty())
    }
}
