package com.example.data

data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val tier: BadgeTier,
    val isUnlocked: Boolean,
    val progress: Float
)

enum class BadgeTier {
    COMMON,
    RARE,
    LEGENDARY
}

object Gamification {
    fun calculateXp(habits: List<Habit>, logs: List<HabitLog>): Int {
        var xp = 0
        // Total completions
        val completedLogsCount = logs.count { it.completed }
        xp += completedLogsCount * 15

        // Streak bonuses
        habits.forEach { habit ->
            if (habit.streak >= 3) xp += 10
            if (habit.streak >= 7) xp += 30
            if (habit.streak >= 30) xp += 100
        }

        // Notes count
        val notesCount = logs.count { !it.note.isNullOrBlank() }
        xp += notesCount * 10

        return xp
    }

    fun getLevel(xp: Int): Int = (xp / 100) + 1

    fun getLevelProgress(xp: Int): Float = (xp % 100) / 100f

    fun getBadges(habits: List<Habit>, logs: List<HabitLog>, xp: Int): List<Badge> {
        val totalCompletions = logs.count { it.completed }
        val maxStreak = (habits.map { it.bestStreak } + habits.map { it.streak }).maxOrNull() ?: 0
        val activeHabitsCount = habits.count { !it.isArchived }
        val notesCount = logs.count { !it.note.isNullOrBlank() }
        val playerLevel = getLevel(xp)

        return listOf(
            Badge(
                id = "first_step",
                title = "First Step",
                description = "Complete your first daily habit",
                icon = "🌱",
                tier = BadgeTier.COMMON,
                isUnlocked = totalCompletions >= 1,
                progress = (totalCompletions / 1f).coerceIn(0f, 1f)
            ),
            Badge(
                id = "week_warrior",
                title = "Week Warrior",
                description = "Maintain a 7-day streak on any habit",
                icon = "⚔️",
                tier = BadgeTier.RARE,
                isUnlocked = maxStreak >= 7,
                progress = (maxStreak / 7f).coerceIn(0f, 1f)
            ),
            Badge(
                id = "habit_architect",
                title = "Habit Architect",
                description = "Build an active routine of 5+ habits",
                icon = "🏛️",
                tier = BadgeTier.COMMON,
                isUnlocked = activeHabitsCount >= 5,
                progress = (activeHabitsCount / 5f).coerceIn(0f, 1f)
            ),
            Badge(
                id = "habit_master",
                title = "Habit Master",
                description = "Reach an incredible 30-day streak",
                icon = "👑",
                tier = BadgeTier.LEGENDARY,
                isUnlocked = maxStreak >= 30,
                progress = (maxStreak / 30f).coerceIn(0f, 1f)
            ),
            Badge(
                id = "century_club",
                title = "Century Club",
                description = "Achieve 100 lifetime habit completions",
                icon = "💯",
                tier = BadgeTier.LEGENDARY,
                isUnlocked = totalCompletions >= 100,
                progress = (totalCompletions / 100f).coerceIn(0f, 1f)
            ),
            Badge(
                id = "mindful_journaler",
                title = "Mindful Journaler",
                description = "Add 5 reflection notes on your check-ins",
                icon = "📝",
                tier = BadgeTier.RARE,
                isUnlocked = notesCount >= 5,
                progress = (notesCount / 5f).coerceIn(0f, 1f)
            ),
            Badge(
                id = "grandmaster",
                title = "Grandmaster",
                description = "Attain Player Level 5 in consistency",
                icon = "⚡",
                tier = BadgeTier.LEGENDARY,
                isUnlocked = playerLevel >= 5,
                progress = (playerLevel / 5f).coerceIn(0f, 1f)
            )
        )
    }
}
