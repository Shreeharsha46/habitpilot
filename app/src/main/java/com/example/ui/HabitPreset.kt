package com.example.ui

import com.example.data.Habit
import com.example.data.HabitType

data class HabitPreset(
    val title: String,
    val emoji: String,
    val category: String,
    val type: HabitType = HabitType.CHECKBOX,
    val targetValue: Int = 1,
    val reminderTime: String? = null
)

val DEFAULT_HABIT_PRESETS = listOf(
    HabitPreset(
        title = "Drink 2L Water",
        emoji = "💧",
        category = "Health",
        type = HabitType.COUNTER,
        targetValue = 8,
        reminderTime = "09:00"
    ),
    HabitPreset(
        title = "30m Workout",
        emoji = "🏃",
        category = "Fitness",
        type = HabitType.TIMER,
        targetValue = 30,
        reminderTime = "07:30"
    ),
    HabitPreset(
        title = "Read 20 Pages",
        emoji = "📖",
        category = "Mind",
        type = HabitType.COUNTER,
        targetValue = 20,
        reminderTime = "21:00"
    ),
    HabitPreset(
        title = "10m Meditation",
        emoji = "🧘",
        category = "Mind",
        type = HabitType.TIMER,
        targetValue = 10,
        reminderTime = "08:00"
    ),
    HabitPreset(
        title = "Code Project",
        emoji = "💻",
        category = "Productivity",
        type = HabitType.CHECKBOX,
        reminderTime = "14:00"
    ),
    HabitPreset(
        title = "Eat Healthy",
        emoji = "🥗",
        category = "Health",
        type = HabitType.CHECKBOX,
        reminderTime = "12:30"
    ),
    HabitPreset(
        title = "Sleep by 11 PM",
        emoji = "💤",
        category = "Health",
        type = HabitType.CHECKBOX,
        reminderTime = "22:45"
    ),
    HabitPreset(
        title = "Evening Walk",
        emoji = "🚶",
        category = "Fitness",
        type = HabitType.TIMER,
        targetValue = 20,
        reminderTime = "18:30"
    )
)
