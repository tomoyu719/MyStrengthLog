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
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.android.architecture.blueprints.todoapp.HiltTestActivity
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.MyStrengthLogNavGraph
import com.example.android.architecture.blueprints.todoapp.data.WorkoutRepository
import com.google.accompanist.appcompattheme.AppCompatTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Tests for scenarios that requires navigating within the app.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
class AppNavigationTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    // Executes Tasks in the Architecture Components in the same thread
    @get:Rule(order = 1)
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()
    private val activity get() = composeTestRule.activity

    @Inject
    lateinit var workoutRepository: WorkoutRepository

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun bottomNavigationFromWorkoutsToStatistics() {
        setContent()

        // Start statistics screen.
        composeTestRule.onNodeWithText(activity.getString(R.string.statistics_title)).performClick()

        // Check that statistics screen was opened.
        composeTestRule.onNodeWithText(activity.getString(R.string.statistics_no_workouts))
            .assertIsDisplayed()

        // Start workouts screen.
        composeTestRule.onNodeWithText(activity.getString(R.string.list_title)).performClick()

        // Check that workouts screen was opened.
        composeTestRule.onNodeWithText(activity.getString(R.string.no_workouts_all))
            .assertIsDisplayed()
    }


    @Test
    fun workoutDetailScreen_doubleUIBackButton() = runTest {
        val workoutName = "UI <- button"
        workoutRepository.createWorkout(workoutName, "Description")

        setContent()

        // Click on the workout on the list
        composeTestRule.onNodeWithText(activity.getString(R.string.label_all)).assertIsDisplayed()
        composeTestRule.onNodeWithText(workoutName).assertIsDisplayed()
        composeTestRule.onNodeWithText(workoutName).performClick()

        // Click on the edit workout button
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.edit_workout))
            .assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.edit_workout))
            .performClick()

        // Confirm that if we click "<-" once, we end up back at the workout details page
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.menu_back))
            .performClick()
        composeTestRule.onNodeWithText(workoutName).assertIsDisplayed()

        // Confirm that if we click "<-" a second time, we end up back at the home screen
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.menu_back))
            .performClick()
        composeTestRule.onNodeWithText(activity.getString(R.string.label_all)).assertIsDisplayed()
    }

    @Test
    fun workoutDetailScreen_doubleBackButton() = runTest {
        val workoutName = "Back button"
        workoutRepository.createWorkout(workoutName, "Description")

        setContent()

        // Click on the workout on the list
        composeTestRule.onNodeWithText(workoutName).assertIsDisplayed()
        composeTestRule.onNodeWithText(workoutName).performClick()
        // Click on the edit workout button
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.edit_workout))
            .performClick()

        // Confirm that if we click back once, we end up back at the workout details page
        pressBack()
        composeTestRule.onNodeWithText(workoutName).assertIsDisplayed()

        // Confirm that if we click back a second time, we end up back at the home screen
        pressBack()
        composeTestRule.onNodeWithText(activity.getString(R.string.label_all)).assertIsDisplayed()
    }

    private fun setContent() {
        composeTestRule.setContent {
            AppCompatTheme {
                MyStrengthLogNavGraph()
            }
        }
    }
}
