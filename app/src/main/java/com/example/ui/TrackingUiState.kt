package com.example.ui

import com.example.data.Badge
import com.example.data.Habit
import com.example.data.HabitLog

data class TrackingUiState(
    val currentView: TrackingView = TrackingView.DASHBOARD,
    val habits: List<Habit> = emptyList(),
    val filteredHabits: List<Habit> = emptyList(),
    val logs: List<HabitLog> = emptyList(),
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val isDarkMode: Boolean = true,
    val showConfetti: Boolean = false,
    val xp: Int = 0,
    val level: Int = 1,
    val levelProgress: Float = 0f,
    val badges: List<Badge> = emptyList(),
    val activeFocusHabit: Habit? = null,
    val timerSeconds: Int = 0,
    val isTimerRunning: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
