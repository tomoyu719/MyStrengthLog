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

package com.example.android.architecture.blueprints.todoapp.workouts

import androidx.annotation.StringRes
import androidx.compose.material.Surface
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.android.architecture.blueprints.todoapp.HiltTestActivity
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.WorkoutRepository
import com.google.accompanist.appcompattheme.AppCompatTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Integration test for the Workout List screen.
 */
// TODO - Move to the sharedTest folder when https://issuetracker.google.com/224974381 is fixed
@RunWith(AndroidJUnit4::class)
@MediumTest
// @LooperMode(LooperMode.Mode.PAUSED)
// @TextLayoutMode(TextLayoutMode.Mode.REALISTIC)
@HiltAndroidTest
@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutsScreenTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()
    private val activity get() = composeTestRule.activity

    @Inject
    lateinit var repository: WorkoutRepository

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun displayWorkout_whenRepositoryHasData() = runTest {
        // GIVEN - One workout already in the repository
        repository.createWorkout("TITLE1", "DESCRIPTION1")

        // WHEN - On startup
        setContent()

        // THEN - Verify workout is displayed on screen
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
    }

    @Test
    fun displayActiveWorkout() = runTest {
        repository.createWorkout("TITLE1", "DESCRIPTION1")

        setContent()

        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()

        openFilterAndSelectOption(R.string.nav_active)
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()

        openFilterAndSelectOption(R.string.nav_completed)

        composeTestRule.onNodeWithText("TITLE1").assertDoesNotExist()
    }

    @Test
    fun displayCompletedWorkout() = runTest {
        repository.apply {
            createWorkout("TITLE1", "DESCRIPTION1").also { completeWorkout(it) }
        }

        setContent()

        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()

        openFilterAndSelectOption(R.string.nav_active)
        composeTestRule.onNodeWithText("TITLE1").assertDoesNotExist()

        openFilterAndSelectOption(R.string.nav_completed)
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
    }

    @Test
    fun markWorkoutAsComplete() = runTest {
        repository.createWorkout("TITLE1", "DESCRIPTION1")

        setContent()

        // Mark the workout as complete
        composeTestRule.onNode(isToggleable()).performClick()

        // Verify workout is shown as complete
        openFilterAndSelectOption(R.string.nav_all)
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
        openFilterAndSelectOption(R.string.nav_active)
        composeTestRule.onNodeWithText("TITLE1").assertDoesNotExist()
        openFilterAndSelectOption(R.string.nav_completed)
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
    }

    @Test
    fun markWorkoutAsActive() = runTest {
        repository.apply {
            createWorkout("TITLE1", "DESCRIPTION1").also { completeWorkout(it) }
        }

        setContent()

        // Mark the workout as active
        composeTestRule.onNode(isToggleable()).performClick()

        // Verify workout is shown as active
        openFilterAndSelectOption(R.string.nav_all)
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
        openFilterAndSelectOption(R.string.nav_active)
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
        openFilterAndSelectOption(R.string.nav_completed)
        composeTestRule.onNodeWithText("TITLE1").assertDoesNotExist()
    }

    @Test
    fun showAllWorkouts() = runTest {
        // Add one active workout and one completed workout
        repository.apply {
            createWorkout("TITLE1", "DESCRIPTION1")
            createWorkout("TITLE2", "DESCRIPTION2").also { completeWorkout(it) }
        }

        setContent()

        // Verify that both of our workouts are shown
        openFilterAndSelectOption(R.string.nav_all)
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
        composeTestRule.onNodeWithText("TITLE2").assertIsDisplayed()
    }

    @Test
    fun showActiveWorkouts() = runTest {
        // Add 2 active workouts and one completed workout
        repository.apply {
            createWorkout("TITLE1", "DESCRIPTION1")
            createWorkout("TITLE2", "DESCRIPTION2")
            createWorkout("TITLE3", "DESCRIPTION3").also { completeWorkout(it) }
        }

        setContent()

        // Verify that the active workouts (but not the completed workout) are shown
        openFilterAndSelectOption(R.string.nav_active)
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
        composeTestRule.onNodeWithText("TITLE2").assertIsDisplayed()
        composeTestRule.onNodeWithText("TITLE3").assertDoesNotExist()
    }

    @Test
    fun showCompletedWorkouts() = runTest {
        // Add one active workout and 2 completed workouts
        repository.apply {
            createWorkout("TITLE1", "DESCRIPTION1")
            createWorkout("TITLE2", "DESCRIPTION2").also { completeWorkout(it) }
            createWorkout("TITLE3", "DESCRIPTION3").also { completeWorkout(it) }
        }

        setContent()

        // Verify that the completed workouts (but not the active workout) are shown
        openFilterAndSelectOption(R.string.nav_completed)
        composeTestRule.onNodeWithText("TITLE1").assertDoesNotExist()
        composeTestRule.onNodeWithText("TITLE2").assertIsDisplayed()
        composeTestRule.onNodeWithText("TITLE3").assertIsDisplayed()
    }

    @Test
    fun clearCompletedWorkouts() = runTest {
        // Add one active workout and one completed workout
        repository.apply {
            createWorkout("TITLE1", "DESCRIPTION1")
            createWorkout("TITLE2", "DESCRIPTION2").also { completeWorkout(it) }
        }

        setContent()

        // Click clear completed in menu
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.menu_more))
            .performClick()
        composeTestRule.onNodeWithText(activity.getString(R.string.menu_clear)).assertIsDisplayed()
        composeTestRule.onNodeWithText(activity.getString(R.string.menu_clear)).performClick()

        openFilterAndSelectOption(R.string.nav_all)
        // Verify that only the active workout is shown
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
        composeTestRule.onNodeWithText("TITLE2").assertDoesNotExist()
    }

    @Test
    fun noWorkouts_AllWorkoutsFilter_AddWorkoutViewVisible() {
        setContent()

        openFilterAndSelectOption(R.string.nav_all)

        // Verify the "You have no workouts!" text is shown
        composeTestRule.onNodeWithText("You have no workouts!").assertIsDisplayed()
    }

    @Test
    fun noWorkouts_CompletedWorkoutsFilter_AddWorkoutViewNotVisible() {
        setContent()

        openFilterAndSelectOption(R.string.nav_completed)
        // Verify the "You have no completed workouts!" text is shown
        composeTestRule.onNodeWithText("You have no completed workouts!").assertIsDisplayed()
    }

    @Test
    fun noWorkouts_ActiveWorkoutsFilter_AddWorkoutViewNotVisible() {
        setContent()

        openFilterAndSelectOption(R.string.nav_active)
        // Verify the "You have no active workouts!" text is shown
        composeTestRule.onNodeWithText("You have no active workouts!").assertIsDisplayed()
    }

    private fun setContent() {
        composeTestRule.setContent {
            AppCompatTheme {
                Surface {
                    WorkoutsScreen(
                        viewModel = WorkoutsViewModel(repository, SavedStateHandle()),
                        userMessage = R.string.successfully_added_workout_message,
                        onUserMessageDisplayed = { },
                        onAddWorkout = { },
                        onWorkoutClick = { },
                    )
                }
            }
        }
    }

    private fun openFilterAndSelectOption(@StringRes option: Int) {
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.menu_filter))
            .performClick()
        composeTestRule.onNodeWithText(activity.getString(option)).assertIsDisplayed()
        composeTestRule.onNodeWithText(activity.getString(option)).performClick()
    }
}
