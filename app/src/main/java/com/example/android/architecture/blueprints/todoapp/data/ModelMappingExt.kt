/*
 * Copyright 2023 The Android Open Source Project
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

import com.example.android.architecture.blueprints.todoapp.data.source.local.LocalWorkout
import com.example.android.architecture.blueprints.todoapp.data.source.network.NetworkWorkout
import com.example.android.architecture.blueprints.todoapp.data.source.network.WorkoutStatus

/**
 * Data model mapping extension functions. There are three model types:
 *
 * - Workout: External model exposed to other layers in the architecture.
 * Obtained using `toExternal`.
 *
 * - NetworkWorkout: Internal model used to represent a workout from the network. Obtained using
 * `toNetwork`.
 *
 * - LocalWorkout: Internal model used to represent a workout stored locally in a database. Obtained
 * using `toLocal`.
 *
 */

// External to local
fun Workout.toLocal() = LocalWorkout(
    id = id,
    title = title,
    description = description,
    isCompleted = isCompleted,
)

fun List<Workout>.toLocal() = map(Workout::toLocal)

// Local to External
fun LocalWorkout.toExternal() = Workout(
    id = id,
    title = title,
    description = description,
    isCompleted = isCompleted,
)

// Note: JvmName is used to provide a unique name for each extension function with the same name.
// Without this, type erasure will cause compiler errors because these methods will have the same
// signature on the JVM.
@JvmName("localToExternal")
fun List<LocalWorkout>.toExternal() = map(LocalWorkout::toExternal)

// Network to Local
fun NetworkWorkout.toLocal() = LocalWorkout(
    id = id,
    title = title,
    description = shortDescription,
    isCompleted = (status == WorkoutStatus.COMPLETE),
)

@JvmName("networkToLocal")
fun List<NetworkWorkout>.toLocal() = map(NetworkWorkout::toLocal)

// Local to Network
fun LocalWorkout.toNetwork() = NetworkWorkout(
    id = id,
    title = title,
    shortDescription = description,
    status = if (isCompleted) { WorkoutStatus.COMPLETE } else { WorkoutStatus.ACTIVE }
)

fun List<LocalWorkout>.toNetwork() = map(LocalWorkout::toNetwork)

// External to Network
fun Workout.toNetwork() = toLocal().toNetwork()

@JvmName("externalToNetwork")
fun List<Workout>.toNetwork() = map(Workout::toNetwork)

// Network to External
fun NetworkWorkout.toExternal() = toLocal().toExternal()

@JvmName("networkToExternal")
fun List<NetworkWorkout>.toExternal() = map(NetworkWorkout::toExternal)
