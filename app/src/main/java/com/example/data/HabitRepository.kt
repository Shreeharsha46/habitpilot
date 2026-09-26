package com.example.data

import kotlinx.coroutines.flow.Flow

class HabitRepository(private val habitDao: HabitDao) {
    val activeHabits: Flow<List<Habit>> = habitDao.getActiveHabits()
    val allHabits: Flow<List<Habit>> = habitDao.getAllHabits()
    val allLogs: Flow<List<HabitLog>> = habitDao.getAllLogs()

    suspend fun getHabitById(id: Long): Habit? = habitDao.getHabitById(id)

    suspend fun insertHabit(habit: Habit): Long = habitDao.insertHabit(habit)

    suspend fun insertHabits(habits: List<Habit>) = habitDao.insertHabits(habits)

    suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)

    suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteHabit(habit)
        habitDao.deleteLogsForHabit(habit.id)
    }

    suspend fun deleteHabitById(id: Long) {
        habitDao.deleteHabitById(id)
        habitDao.deleteLogsForHabit(id)
    }

    suspend fun clearAll() {
        habitDao.clearAllHabits()
        habitDao.clearAllLogs()
    }

    suspend fun toggleHabitCompletion(habit: Habit): Habit {
        val today = Habit.getTodayDateString()
        val yesterday = Habit.getYesterdayDateString()
        val isNowCompleted = !habit.isCompleted

        val newStreak: Int
        val newBestStreak: Int
        val newLastCompletedDate: String?
        val newCurrentVal: Int

        if (isNowCompleted) {
            newCurrentVal = habit.targetValue
            newStreak = if (habit.lastCompletedDate == yesterday) {
                habit.streak + 1
            } else if (habit.lastCompletedDate == today) {
                habit.streak
            } else {
                1
            }
            newBestStreak = maxOf(habit.bestStreak, newStreak)
            newLastCompletedDate = today

            // Record log
            habitDao.insertLog(
                HabitLog(
                    habitId = habit.id,
                    date = today,
                    completed = true,
                    value = newCurrentVal
                )
            )
        } else {
            // Undo completion
            newCurrentVal = 0
            newStreak = maxOf(0, habit.streak - 1)
            newBestStreak = habit.bestStreak
            newLastCompletedDate = if (newStreak > 0) yesterday else null

            // Delete today's log
            habitDao.deleteLogForHabitAndDate(habit.id, today)
        }

        val updated = habit.copy(
            isCompleted = isNowCompleted,
            currentValue = newCurrentVal,
            streak = newStreak,
            bestStreak = newBestStreak,
            lastCompletedDate = newLastCompletedDate
        )
        habitDao.updateHabit(updated)
        return updated
    }

    suspend fun updateHabitProgress(habit: Habit, newValue: Int): Habit {
        val today = Habit.getTodayDateString()
        val yesterday = Habit.getYesterdayDateString()
        val clampedVal = newValue.coerceAtLeast(0)
        val isNowCompleted = clampedVal >= habit.targetValue

        val newStreak: Int
        val newBestStreak: Int
        val newLastCompletedDate: String?

        if (isNowCompleted && !habit.isCompleted) {
            newStreak = if (habit.lastCompletedDate == yesterday) {
                habit.streak + 1
            } else if (habit.lastCompletedDate == today) {
                habit.streak
            } else {
                1
            }
            newBestStreak = maxOf(habit.bestStreak, newStreak)
            newLastCompletedDate = today

            habitDao.insertLog(
                HabitLog(
                    habitId = habit.id,
                    date = today,
                    completed = true,
                    value = clampedVal
                )
            )
        } else if (!isNowCompleted && habit.isCompleted) {
            newStreak = maxOf(0, habit.streak - 1)
            newBestStreak = habit.bestStreak
            newLastCompletedDate = if (newStreak > 0) yesterday else null

            habitDao.deleteLogForHabitAndDate(habit.id, today)
        } else {
            newStreak = habit.streak
            newBestStreak = habit.bestStreak
            newLastCompletedDate = habit.lastCompletedDate
        }

        val updated = habit.copy(
            currentValue = clampedVal,
            isCompleted = isNowCompleted,
            streak = newStreak,
            bestStreak = newBestStreak,
            lastCompletedDate = newLastCompletedDate
        )
        habitDao.updateHabit(updated)
        return updated
    }

    suspend fun addReflectionNote(habitId: Long, note: String) {
        val today = Habit.getTodayDateString()
        val existingLog = habitDao.getLogForHabitAndDate(habitId, today)
        if (existingLog != null) {
            habitDao.insertLog(existingLog.copy(note = note))
        } else {
            habitDao.insertLog(
                HabitLog(
                    habitId = habitId,
                    date = today,
                    completed = true,
                    note = note
                )
            )
        }
    }

    suspend fun resetTodayProgress(habits: List<Habit>) {
        val today = Habit.getTodayDateString()
        val yesterday = Habit.getYesterdayDateString()
        habits.forEach { habit ->
            if (habit.isCompleted) {
                val newStreak = maxOf(0, habit.streak - 1)
                val updated = habit.copy(
                    isCompleted = false,
                    currentValue = 0,
                    streak = newStreak,
                    lastCompletedDate = if (newStreak > 0) yesterday else null
                )
                habitDao.updateHabit(updated)
                habitDao.deleteLogForHabitAndDate(habit.id, today)
            }
        }
    }
}
