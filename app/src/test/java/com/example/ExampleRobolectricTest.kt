package com.example

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.Gamification
import com.example.data.Habit
import com.example.data.HabitLog
import com.example.data.HabitType
import com.example.ui.HabitViewModel
import com.example.ui.TrackingView
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Habit Tracker", appName)
    }

    @Test
    fun `room database persistence for habit entity`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val dao = db.habitDao()

        // 1. Insert Habit
        val habit = Habit(
            title = "Morning Meditation",
            emoji = "🧘",
            category = "Mind",
            type = HabitType.TIMER,
            targetValue = 15
        )
        val id = dao.insertHabit(habit)
        assertTrue(id > 0)

        // 2. Query Habit by ID
        val retrieved = dao.getHabitById(id)
        assertNotNull(retrieved)
        assertEquals("Morning Meditation", retrieved?.title)
        assertEquals("Mind", retrieved?.category)
        assertEquals(HabitType.TIMER, retrieved?.type)
        assertEquals(15, retrieved?.targetValue)

        // 3. Query All Active Habits Flow
        val activeHabits = dao.getActiveHabits().first()
        assertEquals(1, activeHabits.size)
        assertEquals(id, activeHabits[0].id)

        // 4. Update Habit
        dao.updateHabit(retrieved!!.copy(streak = 3, isCompleted = true))
        val updated = dao.getHabitById(id)
        assertEquals(3, updated?.streak)
        assertTrue(updated?.isCompleted == true)

        // 5. Delete Habit
        dao.deleteHabitById(id)
        val afterDelete = dao.getHabitById(id)
        assertEquals(null, afterDelete)

        db.close()
    }

    @Test
    fun `gamification level calculation`() {
        val habits = listOf(
            Habit(title = "Water", streak = 7),
            Habit(title = "Read", streak = 3)
        )
        val logs = listOf(
            HabitLog(habitId = 1, date = "2026-09-26", completed = true),
            HabitLog(habitId = 2, date = "2026-09-26", completed = true)
        )
        val xp = Gamification.calculateXp(habits, logs)
        assertTrue(xp > 0)
        val level = Gamification.getLevel(xp)
        assertEquals(1, level)
    }

    @Test
    fun `tracking view transitions in viewmodel`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = HabitViewModel(app)

        assertEquals(TrackingView.DASHBOARD, viewModel.currentTrackingView.value)

        // Navigate to Analytics
        viewModel.navigateTo(TrackingView.ANALYTICS)
        assertEquals(TrackingView.ANALYTICS, viewModel.currentTrackingView.value)

        // Navigate to Focus Timer
        viewModel.navigateTo(TrackingView.FOCUS_TIMER)
        assertEquals(TrackingView.FOCUS_TIMER, viewModel.currentTrackingView.value)

        // Handle back press returns to Dashboard
        val handled = viewModel.handleBackPress()
        assertTrue(handled)
        assertEquals(TrackingView.DASHBOARD, viewModel.currentTrackingView.value)

        // Back press on Dashboard returns false
        val handledAgain = viewModel.handleBackPress()
        assertFalse(handledAgain)
    }
}
