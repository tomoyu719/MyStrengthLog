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

package com.example.android.architecture.blueprints.todoapp.workoutdetail

import androidx.compose.material.Surface
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.android.architecture.blueprints.todoapp.HiltTestActivity
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
 * Integration test for the Workout Details screen.
 */
@MediumTest
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@ExperimentalCoroutinesApi
class WorkoutDetailScreenTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @Inject
    lateinit var repository: WorkoutRepository

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun activeWorkoutDetails_DisplayedInUi() = runTest {
        // GIVEN - Add active (incomplete) workout to the DB
        val activeWorkoutId = repository.createWorkout(
            title = "Active Workout",
            description = "AndroidX Rocks"
        )

        // WHEN - Details screen is opened
        setContent(activeWorkoutId)

        // THEN - Workout details are displayed on the screen
        // make sure that the title/description are both shown and correct
        composeTestRule.onNodeWithText("Active Workout").assertIsDisplayed()
        composeTestRule.onNodeWithText("AndroidX Rocks").assertIsDisplayed()
        // and make sure the "active" checkbox is shown unchecked
        composeTestRule.onNode(isToggleable()).assertIsOff()
    }

    @Test
    fun completedWorkoutDetails_DisplayedInUi() = runTest {
        // GIVEN - Add completed workout to the DB
        val completedWorkoutId = repository.createWorkout("Completed Workout", "AndroidX Rocks")
        repository.completeWorkout(completedWorkoutId)

        // WHEN - Details screen is opened
        setContent(completedWorkoutId)

        // THEN - Workout details are displayed on the screen
        // make sure that the title/description are both shown and correct
        composeTestRule.onNodeWithText("Completed Workout").assertIsDisplayed()
        composeTestRule.onNodeWithText("AndroidX Rocks").assertIsDisplayed()
        // and make sure the "active" checkbox is shown unchecked
        composeTestRule.onNode(isToggleable()).assertIsOn()
    }

    private fun setContent(activeWorkoutId: String) {
        composeTestRule.setContent {
            AppCompatTheme {
                Surface {
                    WorkoutDetailScreen(
                        viewModel = WorkoutDetailViewModel(
                            repository,
                            SavedStateHandle(mapOf("workoutId" to activeWorkoutId))
                        ),
                        onEditWorkout = { /*TODO*/ },
                        onBack = { },
                        onDeleteWorkout = { },
                    )
                }
            }
        }
    }
}
