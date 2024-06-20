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

package com.example.android.architecture.blueprints.todoapp.data.source.network

import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class WorkoutNetworkDataSource @Inject constructor() : NetworkDataSource {

    // A mutex is used to ensure that reads and writes are thread-safe.
    private val accessMutex = Mutex()
    private var workouts = listOf(
        NetworkWorkout(
            id = "PISA",
            title = "Build tower in Pisa",
            shortDescription = "Ground looks good, no foundation work required."
        ),
        NetworkWorkout(
            id = "TACOMA",
            title = "Finish bridge in Tacoma",
            shortDescription = "Found awesome girders at half the cost!"
        )
    )

    override suspend fun loadWorkouts(): List<NetworkWorkout> = accessMutex.withLock {
        delay(SERVICE_LATENCY_IN_MILLIS)
        return workouts
    }

    override suspend fun saveWorkouts(newWorkouts: List<NetworkWorkout>) = accessMutex.withLock {
        delay(SERVICE_LATENCY_IN_MILLIS)
        workouts = newWorkouts
    }
}

private const val SERVICE_LATENCY_IN_MILLIS = 2000L
