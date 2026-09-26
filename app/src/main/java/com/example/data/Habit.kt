package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class HabitType {
    CHECKBOX,
    COUNTER,
    TIMER
}

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val emoji: String = "✨",
    val category: String = "General",
    val type: HabitType = HabitType.CHECKBOX,
    val targetValue: Int = 1,
    val currentValue: Int = 0,
    val isCompleted: Boolean = false,
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val lastCompletedDate: String? = null, // YYYY-MM-DD
    val reminderTime: String? = null,      // HH:mm format
    val reminderEnabled: Boolean = false,
    val frequencyType: String = "DAILY",   // DAILY, WEEKDAYS, CUSTOM
    val orderIndex: Int = 0,
    val isArchived: Boolean = false,
    val createdTimestamp: Long = System.currentTimeMillis()
) {
    companion object {
        fun getTodayDateString(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }

        fun getYesterdayDateString(): String {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(cal.time)
        }
    }
}
