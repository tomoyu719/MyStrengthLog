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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.android.architecture.blueprints.todoapp.HiltTestActivity
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.MyStrengthLogNavGraph
import com.example.android.architecture.blueprints.todoapp.data.WorkoutRepository
import com.google.accompanist.appcompattheme.AppCompatTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Large End-to-End test for the workouts module.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutsTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    // Executes workouts in the Architecture Components in the same thread
    @get:Rule(order = 1)
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()
    private val activity get() = composeTestRule.activity

    @Inject
    lateinit var repository: WorkoutRepository

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun editWorkout() = runTest {
        val originalWorkoutTitle = "TITLE1"
        repository.createWorkout(originalWorkoutTitle, "DESCRIPTION")

        setContent()

        // Click on the workout on the list and verify that all the data is correct
        composeTestRule.onNodeWithText(activity.getString(R.string.label_all)).assertIsDisplayed()
        composeTestRule.onNodeWithText(originalWorkoutTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText(originalWorkoutTitle).performClick()

        // Workout detail screen
        composeTestRule.onNodeWithText(activity.getString(R.string.workout_details))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(originalWorkoutTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText("DESCRIPTION").assertIsDisplayed()
        composeTestRule.onNode(isToggleable()).assertIsOff()

        // Click on the edit button, edit, and save
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.edit_workout))
            .performClick()
        composeTestRule.onNodeWithText(activity.getString(R.string.edit_workout)).assertIsDisplayed()
        findTextField(originalWorkoutTitle).performTextReplacement("NEW TITLE")
        findTextField("DESCRIPTION").performTextReplacement("NEW DESCRIPTION")
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.cd_save_workout))
            .performClick()

        // Verify workout is displayed on screen in the workout list.
        composeTestRule.onNodeWithText(activity.getString(R.string.label_all)).assertIsDisplayed()
        composeTestRule.onNodeWithText("NEW TITLE").assertIsDisplayed()
        // Verify previous workout is not displayed
        composeTestRule.onNodeWithText(originalWorkoutTitle).assertDoesNotExist()
    }

    @Test
    fun createOneWorkout_deleteWorkout() {
        setContent()

        val workoutTitle = "TITLE1"
        // Add active workout
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.add_workout))
            .performClick()
        findTextField(R.string.title_hint).performTextInput(workoutTitle)
        findTextField(R.string.description_hint).performTextInput("DESCRIPTION")
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.cd_save_workout))
            .performClick()
        composeTestRule.onNodeWithText(activity.getString(R.string.label_all)).assertIsDisplayed()
        composeTestRule.onNodeWithText(workoutTitle).assertIsDisplayed()

        // Open the workout detail screen
        composeTestRule.onNodeWithText(workoutTitle).performClick()
        composeTestRule.onNodeWithText(activity.getString(R.string.workout_details))
            .assertIsDisplayed()
        // Click delete workout in menu
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.menu_delete_workout))
            .performClick()

        // Verify it was deleted
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.menu_filter))
            .performClick()
        composeTestRule.onNodeWithText(activity.getString(R.string.nav_all)).assertIsDisplayed()
        composeTestRule.onNodeWithText(workoutTitle).assertDoesNotExist()
    }

    @Test
    fun createTwoWorkouts_deleteOneWorkout() = runTest {
        repository.apply {
            createWorkout("TITLE1", "DESCRIPTION")
            createWorkout("TITLE2", "DESCRIPTION")
        }

        setContent()

        // Open the second workout in details view
        composeTestRule.onNodeWithText(activity.getString(R.string.label_all)).assertIsDisplayed()
        composeTestRule.onNodeWithText("TITLE2").assertIsDisplayed()
        composeTestRule.onNodeWithText("TITLE2").performClick()
        // Click delete workout in menu
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.menu_delete_workout))
            .performClick()

        // Verify only one workout was deleted
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.menu_filter))
            .performClick()
        composeTestRule.onNodeWithText(activity.getString(R.string.label_all)).performClick()
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
        composeTestRule.onNodeWithText("TITLE2").assertDoesNotExist()
    }

    @Test
    fun markWorkoutAsCompleteOnDetailScreen_workoutIsCompleteInList() = runTest {
        // Add 1 active workout
        val workoutTitle = "COMPLETED"
        repository.createWorkout(workoutTitle, "DESCRIPTION")

        setContent()

        // Click on the workout on the list
        composeTestRule.onNodeWithText(activity.getString(R.string.label_all)).assertIsDisplayed()
        composeTestRule.onNodeWithText(workoutTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText(workoutTitle).performClick()

        // Click on the checkbox in workout details screen
        composeTestRule.onNode(isToggleable()).performClick()

        // Click on the navigation up button to go back to the list
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.menu_back))
            .performClick()

        // Check that the workout is marked as completed
        composeTestRule.onNodeWithText(activity.getString(R.string.label_all)).assertIsDisplayed()
        composeTestRule.onNode(isToggleable()).assertIsOn()
    }

    @Test
    fun markWorkoutAsActiveOnDetailScreen_workoutIsActiveInList() = runTest {
        // Add 1 completed workout
        val workoutTitle = "ACTIVE"
        repository.apply {
            createWorkout(workoutTitle, "DESCRIPTION").also { completeWorkout(it) }
        }

        setContent()

        // Click on the workout on the list
        composeTestRule.onNodeWithText(activity.getString(R.string.label_all)).assertIsDisplayed()
        composeTestRule.onNodeWithText(workoutTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText(workoutTitle).performClick()

        // Click on the checkbox in workout details screen
        composeTestRule.onNode(isToggleable()).performClick()

        // Click on the navigation up button to go back to the list
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.menu_back))
            .performClick()

        // Check that the workout is marked as active
        composeTestRule.onNodeWithText(activity.getString(R.string.label_all)).assertIsDisplayed()
        composeTestRule.onNode(isToggleable()).assertIsOff()
    }

    @Test
    fun markWorkoutAsCompleteAndActiveOnDetailScreen_workoutIsActiveInList() = runTest {
        // Add 1 active workout
        val workoutTitle = "ACT-COMP"
        repository.createWorkout(workoutTitle, "DESCRIPTION")

        setContent()

        // Click on the workout on the list
        composeTestRule.onNodeWithText(activity.getString(R.string.label_all)).assertIsDisplayed()
        composeTestRule.onNodeWithText(workoutTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText(workoutTitle).performClick()

        // Click on the checkbox in workout details screen
        composeTestRule.onNode(isToggleable()).performClick()
        // Click again to restore it to original state
        composeTestRule.onNode(isToggleable()).performClick()

        // Click on the navigation up button to go back to the list
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.menu_back))
            .performClick()

        // Check that the workout is marked as active
        composeTestRule.onNodeWithText(activity.getString(R.string.label_all)).assertIsDisplayed()
        composeTestRule.onNode(isToggleable()).assertIsOff()
    }

    @Test
    fun markWorkoutAsActiveAndCompleteOnDetailScreen_workoutIsCompleteInList() = runTest {
        // Add 1 completed workout
        val workoutTitle = "COMP-ACT"
        repository.apply {
            createWorkout(workoutTitle, "DESCRIPTION").also { completeWorkout(it) }
        }

        setContent()

        // Click on the workout on the list
        composeTestRule.onNodeWithText(activity.getString(R.string.label_all)).assertIsDisplayed()
        composeTestRule.onNodeWithText(workoutTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText(workoutTitle).performClick()
        // Click on the checkbox in workout details screen
        composeTestRule.onNode(isToggleable()).performClick()
        // Click again to restore it to original state
        composeTestRule.onNode(isToggleable()).performClick()

        // Click on the navigation up button to go back to the list
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.menu_back))
            .performClick()

        // Check that the workout is marked as active
        composeTestRule.onNodeWithText(activity.getString(R.string.label_all)).assertIsDisplayed()
        composeTestRule.onNode(isToggleable()).assertIsOn()
    }

    @Test
    fun createWorkout() {
        setContent()

        // Click on the "+" button, add details, and save
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.add_workout))
            .performClick()
        findTextField(R.string.title_hint).performTextInput("title")
        findTextField(R.string.description_hint).performTextInput("description")
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.cd_save_workout))
            .performClick()

        // Then verify workout is displayed on screen
        composeTestRule.onNodeWithText(activity.getString(R.string.label_all)).assertIsDisplayed()
        composeTestRule.onNodeWithText("title").assertIsDisplayed()
    }

    private fun setContent() {
        composeTestRule.setContent {
            AppCompatTheme {
                MyStrengthLogNavGraph()
            }
        }
    }

    private fun findTextField(textId: Int): SemanticsNodeInteraction {
        return composeTestRule.onNode(
            hasSetTextAction() and hasText(activity.getString(textId))
        )
    }

    private fun findTextField(text: String): SemanticsNodeInteraction {
        return composeTestRule.onNode(
            hasSetTextAction() and hasText(text)
        )
    }
}
