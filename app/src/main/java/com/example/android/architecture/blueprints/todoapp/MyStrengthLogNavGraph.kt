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

import android.app.Activity
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.android.architecture.blueprints.todoapp.WorkoutDestinationsArgs.WORKOUT_ID_ARG
import com.example.android.architecture.blueprints.todoapp.WorkoutDestinationsArgs.TITLE_ARG
import com.example.android.architecture.blueprints.todoapp.WorkoutDestinationsArgs.USER_MESSAGE_ARG
import com.example.android.architecture.blueprints.todoapp.addeditworkout.AddEditWorkoutScreen
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsScreen
import com.example.android.architecture.blueprints.todoapp.workoutdetail.WorkoutDetailScreen
import com.example.android.architecture.blueprints.todoapp.workouts.WorkoutsScreen

@Composable
fun MyStrengthLogNavGraph(
        navController: NavHostController = rememberNavController(),
        startDestination: String = MyStrengthLogDestinations.WORKOUTS_ROUTE,
        navActions: MyStrengthLogNavigationActions = remember(navController) {
        MyStrengthLogNavigationActions(navController)
    }
) {
    Scaffold(
        bottomBar = {
            BottomNavigation {
                val currentNavBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentNavBackStackEntry?.destination?.route ?: startDestination
                items.forEach { screen ->
                    BottomNavigationItem(
                        selected = currentRoute == screen.route,
                        onClick = { navController.navigate(screen.route) },
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(stringResource(screen.resourceId)) }
                    )
                }
            }

        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(
                MyStrengthLogDestinations.WORKOUTS_ROUTE,
                arguments = listOf(
                    navArgument(USER_MESSAGE_ARG) { type = NavType.IntType; defaultValue = 0 }
                )
            ) { entry ->
                WorkoutsScreen(
                    userMessage = entry.arguments?.getInt(USER_MESSAGE_ARG)!!,
                    onUserMessageDisplayed = { entry.arguments?.putInt(USER_MESSAGE_ARG, 0) },
                    onAddWorkout = { navActions.navigateToAddEditWorkout(R.string.add_workout, null) },
                    onWorkoutClick = { workout -> navActions.navigateToWorkoutDetail(workout.id) },
                )

            }
            composable(MyStrengthLogDestinations.STATISTICS_ROUTE) {
                StatisticsScreen()
            }
            composable(
                MyStrengthLogDestinations.ADD_EDIT_WORKOUT_ROUTE,
                arguments = listOf(
                    navArgument(TITLE_ARG) { type = NavType.IntType },
                    navArgument(WORKOUT_ID_ARG) { type = NavType.StringType; nullable = true },
                )
            ) { entry ->
                val workoutId = entry.arguments?.getString(WORKOUT_ID_ARG)
                AddEditWorkoutScreen(
                    topBarTitle = entry.arguments?.getInt(TITLE_ARG)!!,
                    onWorkoutUpdate = {
                        navActions.navigateToWorkouts(
                            if (workoutId == null) ADD_EDIT_RESULT_OK else EDIT_RESULT_OK
                        )
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(MyStrengthLogDestinations.WORKOUT_DETAIL_ROUTE) {
                WorkoutDetailScreen(
                    onEditWorkout = { workoutId ->
                        navActions.navigateToAddEditWorkout(R.string.edit_workout, workoutId)
                    },
                    onBack = { navController.popBackStack() },
                    onDeleteWorkout = { navActions.navigateToWorkouts(DELETE_RESULT_OK) }
                )
            }
        }
    }
}

// Keys for navigation
const val ADD_EDIT_RESULT_OK = Activity.RESULT_FIRST_USER + 1
const val DELETE_RESULT_OK = Activity.RESULT_FIRST_USER + 2
const val EDIT_RESULT_OK = Activity.RESULT_FIRST_USER + 3

//    sealed class Screen(val route: String, @StringRes val resourceId: Int) {
sealed class Screen(val route: String, val icon: ImageVector, @StringRes val resourceId: Int) {
    object Workouts : Screen(MyStrengthLogDestinations.WORKOUTS_ROUTE, Icons.Default.List, R.string.list_title)

    object Statistics :
        Screen(MyStrengthLogDestinations.STATISTICS_ROUTE, Icons.Default.Info, R.string.statistics_title)
}

val items = listOf(
    Screen.Workouts,
    Screen.Statistics,
)