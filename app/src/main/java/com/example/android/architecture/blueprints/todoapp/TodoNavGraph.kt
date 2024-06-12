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
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.android.architecture.blueprints.todoapp.TodoDestinationsArgs.TASK_ID_ARG
import com.example.android.architecture.blueprints.todoapp.TodoDestinationsArgs.TITLE_ARG
import com.example.android.architecture.blueprints.todoapp.TodoDestinationsArgs.USER_MESSAGE_ARG
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskScreen
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsScreen
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailScreen
import com.example.android.architecture.blueprints.todoapp.tasks.TasksScreen

@Composable
fun TodoNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = TodoDestinations.TASKS_ROUTE,
    navActions: TodoNavigationActions = remember(navController) {
        TodoNavigationActions(navController)
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
                        icon = { Icon(screen.icon, contentDescription = null) }
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
                TodoDestinations.TASKS_ROUTE,
                arguments = listOf(
                    navArgument(USER_MESSAGE_ARG) { type = NavType.IntType; defaultValue = 0 }
                )
            ) { entry ->
                TasksScreen(
                    userMessage = entry.arguments?.getInt(USER_MESSAGE_ARG)!!,
                    onUserMessageDisplayed = { entry.arguments?.putInt(USER_MESSAGE_ARG, 0) },
                    onAddTask = { navActions.navigateToAddEditTask(R.string.add_task, null) },
                    onTaskClick = { task -> navActions.navigateToTaskDetail(task.id) },
                )

            }
            composable(TodoDestinations.STATISTICS_ROUTE) {
                StatisticsScreen()
            }
            composable(
                TodoDestinations.ADD_EDIT_TASK_ROUTE,
                arguments = listOf(
                    navArgument(TITLE_ARG) { type = NavType.IntType },
                    navArgument(TASK_ID_ARG) { type = NavType.StringType; nullable = true },
                )
            ) { entry ->
                val taskId = entry.arguments?.getString(TASK_ID_ARG)
                AddEditTaskScreen(
                    topBarTitle = entry.arguments?.getInt(TITLE_ARG)!!,
                    onTaskUpdate = {
                        navActions.navigateToTasks(
                            if (taskId == null) ADD_EDIT_RESULT_OK else EDIT_RESULT_OK
                        )
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(TodoDestinations.TASK_DETAIL_ROUTE) {
                TaskDetailScreen(
                    onEditTask = { taskId ->
                        navActions.navigateToAddEditTask(R.string.edit_task, taskId)
                    },
                    onBack = { navController.popBackStack() },
                    onDeleteTask = { navActions.navigateToTasks(DELETE_RESULT_OK) }
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
sealed class Screen(val route: String, val icon: ImageVector) {
    object Tasks : Screen(TodoDestinations.TASKS_ROUTE, Icons.Default.List)

    object Statistics : Screen(TodoDestinations.STATISTICS_ROUTE, Icons.Default.Info)
}

val items = listOf(
    Screen.Tasks,
    Screen.Statistics,
)