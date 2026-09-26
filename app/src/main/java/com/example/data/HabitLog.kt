package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habit_logs")
data class HabitLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val date: String, // YYYY-MM-DD
    val completed: Boolean = true,
    val value: Int = 1,
    val note: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
