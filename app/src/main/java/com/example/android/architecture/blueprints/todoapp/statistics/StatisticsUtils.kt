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

/**
 * Function that does some trivial computation. Used to showcase unit tests.
 */
internal fun getActiveAndCompletedStats(workouts: List<Workout>): StatsResult {

    return if (workouts.isEmpty()) {
        StatsResult(0f, 0f)
    } else {
        val totalWorkouts = workouts.size
        val numberOfActiveWorkouts = workouts.count { it.isActive }
        StatsResult(
            activeWorkoutsPercent = 100f * numberOfActiveWorkouts / workouts.size,
            completedWorkoutsPercent = 100f * (totalWorkouts - numberOfActiveWorkouts) / workouts.size
        )
    }
}

data class StatsResult(val activeWorkoutsPercent: Float, val completedWorkoutsPercent: Float)
