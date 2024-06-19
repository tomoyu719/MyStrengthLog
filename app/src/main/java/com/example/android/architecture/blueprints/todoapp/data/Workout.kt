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

/**
 * Immutable model class for a Workout.
 *
 * @param title title of the workout
 * @param description description of the workout
 * @param isCompleted whether or not this workout is completed
 * @param id id of the workout
 *
 * TODO: The constructor of this class should be `internal` but it is used in previews and tests
 *  so that's not possible until those previews/tests are refactored.
 */
data class Workout(
    val title: String = "",
    val description: String = "",
    val isCompleted: Boolean = false,
    val id: String,
) {

    val titleForList: String
        get() = if (title.isNotEmpty()) title else description

    val isActive
        get() = !isCompleted

    val isEmpty
        get() = title.isEmpty() || description.isEmpty()
}
