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

package com.example.android.architecture.blueprints.todoapp.statistics

import com.example.android.architecture.blueprints.todoapp.data.Workout
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Test

/**
 * Unit tests for [getActiveAndCompletedStats].
 */
class StatisticsUtilsTest {

    @Test
    fun getActiveAndCompletedStats_noCompleted() {
        val workouts = listOf(
            Workout(
                id = "id",
                title = "title",
                description = "desc",
                isCompleted = false,
            )
        )
        // When the list of workouts is computed with an active workout
        val result = getActiveAndCompletedStats(workouts)

        // Then the percentages are 100 and 0
        assertThat(result.activeWorkoutsPercent, `is`(100f))
        assertThat(result.completedWorkoutsPercent, `is`(0f))
    }

    @Test
    fun getActiveAndCompletedStats_noActive() {
        val workouts = listOf(
            Workout(
                id = "id",
                title = "title",
                description = "desc",
                isCompleted = true,
            )
        )
        // When the list of workouts is computed with a completed workout
        val result = getActiveAndCompletedStats(workouts)

        // Then the percentages are 0 and 100
        assertThat(result.activeWorkoutsPercent, `is`(0f))
        assertThat(result.completedWorkoutsPercent, `is`(100f))
    }

    @Test
    fun getActiveAndCompletedStats_both() {
        // Given 3 completed workouts and 2 active workouts
        val workouts = listOf(
            Workout(id = "1", title = "title", description = "desc", isCompleted = true),
            Workout(id = "2", title = "title", description = "desc", isCompleted = true),
            Workout(id = "3", title = "title", description = "desc", isCompleted = true),
            Workout(id = "4", title = "title", description = "desc", isCompleted = false),
            Workout(id = "5", title = "title", description = "desc", isCompleted = false),
        )
        // When the list of workouts is computed
        val result = getActiveAndCompletedStats(workouts)

        // Then the result is 40-60
        assertThat(result.activeWorkoutsPercent, `is`(40f))
        assertThat(result.completedWorkoutsPercent, `is`(60f))
    }

    @Test
    fun getActiveAndCompletedStats_empty() {
        // When there are no workouts
        val result = getActiveAndCompletedStats(emptyList())

        // Both active and completed workouts are 0
        assertThat(result.activeWorkoutsPercent, `is`(0f))
        assertThat(result.completedWorkoutsPercent, `is`(0f))
    }
}
