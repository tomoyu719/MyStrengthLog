/*
 * Copyright 2022 The Android Open Source Project
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

package com.example.android.architecture.blueprints.todoapp

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.example.android.architecture.blueprints.todoapp.WorkoutDestinationsArgs.WORKOUT_ID_ARG
import com.example.android.architecture.blueprints.todoapp.WorkoutDestinationsArgs.TITLE_ARG
import com.example.android.architecture.blueprints.todoapp.WorkoutDestinationsArgs.USER_MESSAGE_ARG
import com.example.android.architecture.blueprints.todoapp.MyStrengthLogScreens.ADD_EDIT_WORKOUT_SCREEN
import com.example.android.architecture.blueprints.todoapp.MyStrengthLogScreens.STATISTICS_SCREEN
import com.example.android.architecture.blueprints.todoapp.MyStrengthLogScreens.WORKOUTS_SCREEN
import com.example.android.architecture.blueprints.todoapp.MyStrengthLogScreens.WORKOUT_DETAIL_SCREEN

/**
 * Screens used in [MyStrengthLogDestinations]
 */
private object MyStrengthLogScreens {
    const val WORKOUTS_SCREEN = "workouts"
    const val STATISTICS_SCREEN = "statistics"
    const val WORKOUT_DETAIL_SCREEN = "workout"
    const val ADD_EDIT_WORKOUT_SCREEN = "addEditWorkout"
}

/**
 * Arguments used in [MyStrengthLogDestinations] routes
 */
object WorkoutDestinationsArgs {
    const val USER_MESSAGE_ARG = "userMessage"
    const val WORKOUT_ID_ARG = "workoutId"
    const val TITLE_ARG = "title"
}

/**
 * Destinations used in the [MyStrengthLogActivity]
 */
object MyStrengthLogDestinations {
    const val WORKOUTS_ROUTE = "$WORKOUTS_SCREEN?$USER_MESSAGE_ARG={$USER_MESSAGE_ARG}"
    const val STATISTICS_ROUTE = STATISTICS_SCREEN
    const val WORKOUT_DETAIL_ROUTE = "$WORKOUT_DETAIL_SCREEN/{$WORKOUT_ID_ARG}"
    const val ADD_EDIT_WORKOUT_ROUTE = "$ADD_EDIT_WORKOUT_SCREEN/{$TITLE_ARG}?$WORKOUT_ID_ARG={$WORKOUT_ID_ARG}"
}

/**
 * Models the navigation actions in the app.
 */
class MyStrengthLogNavigationActions(private val navController: NavHostController) {

    fun navigateToWorkouts(userMessage: Int = 0) {
        val navigatesFromDrawer = userMessage == 0
        navController.navigate(
            WORKOUTS_SCREEN.let {
                if (userMessage != 0) "$it?$USER_MESSAGE_ARG=$userMessage" else it
            }
        ) {
            popUpTo(navController.graph.findStartDestination().id) {
                inclusive = !navigatesFromDrawer
                saveState = navigatesFromDrawer
            }
            launchSingleTop = true
            restoreState = navigatesFromDrawer
        }
    }

    fun navigateToStatistics() {
        navController.navigate(MyStrengthLogDestinations.STATISTICS_ROUTE) {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }
    }

    fun navigateToWorkoutDetail(workoutId: String) {
        navController.navigate("$WORKOUT_DETAIL_SCREEN/$workoutId")
    }

    fun navigateToAddEditWorkout(title: Int, workoutId: String?) {
        navController.navigate(
            "$ADD_EDIT_WORKOUT_SCREEN/$title".let {
                if (workoutId != null) "$it?$WORKOUT_ID_ARG=$workoutId" else it
            }
        )
    }
}
