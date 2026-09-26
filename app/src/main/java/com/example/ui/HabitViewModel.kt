package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Badge
import com.example.data.Gamification
import com.example.data.Habit
import com.example.data.HabitLog
import com.example.data.HabitRepository
import com.example.data.HabitType
import com.example.data.ReminderScheduler
import com.example.data.ai.ParsedHabit
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HabitViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HabitRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = HabitRepository(db.habitDao())
    }

    // Underlying Data Sources
    val allHabits: StateFlow<List<Habit>> = repository.activeHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLogs: StateFlow<List<HabitLog>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI State & Tracking View Navigation
    private val _currentTrackingView = MutableStateFlow(TrackingView.DASHBOARD)
    val currentTrackingView: StateFlow<TrackingView> = _currentTrackingView.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _showConfetti = MutableStateFlow(false)
    val showConfetti: StateFlow<Boolean> = _showConfetti.asStateFlow()

    // Focus Timer State
    private val _activeFocusHabitId = MutableStateFlow<Long?>(null)
    val activeFocusHabitId: StateFlow<Long?> = _activeFocusHabitId.asStateFlow()

    private val _timerSeconds = MutableStateFlow(0)
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private var timerJob: Job? = null

    // Filtered Habits Flow
    val filteredHabits: StateFlow<List<Habit>> = combine(
        allHabits,
        _selectedCategory,
        _searchQuery
    ) { habits, category, query ->
        habits.filter { habit ->
            val matchesCategory = (category == "All" || habit.category.equals(category, ignoreCase = true))
            val matchesQuery = query.isBlank() || habit.title.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Gamification Flows
    val xp: StateFlow<Int> = combine(allHabits, allLogs) { habits, logs ->
        Gamification.calculateXp(habits, logs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val badges: StateFlow<List<Badge>> = combine(allHabits, allLogs, xp) { habits, logs, currentXp ->
        Gamification.getBadges(habits, logs, currentXp)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Unified UI StateFlow combining all streams
    val uiState: StateFlow<TrackingUiState> = combine(
        combine(
            _currentTrackingView,
            allHabits,
            filteredHabits,
            allLogs,
            _selectedCategory
        ) { view, habits, filtered, logs, category ->
            StateBundleA(view, habits, filtered, logs, category)
        },
        combine(
            _searchQuery,
            _isDarkMode,
            _showConfetti,
            xp,
            badges
        ) { query, darkMode, confetti, currentXp, currentBadges ->
            StateBundleB(query, darkMode, confetti, currentXp, currentBadges)
        },
        combine(
            _activeFocusHabitId,
            _timerSeconds,
            _isTimerRunning
        ) { activeId, seconds, running ->
            StateBundleC(activeId, seconds, running)
        }
    ) { bundleA, bundleB, bundleC ->
        val activeHabit = bundleA.habits.firstOrNull { it.id == bundleC.activeHabitId }
            ?: bundleA.habits.firstOrNull { it.type == HabitType.TIMER }
            ?: bundleA.habits.firstOrNull()

        TrackingUiState(
            currentView = bundleA.view,
            habits = bundleA.habits,
            filteredHabits = bundleA.filtered,
            logs = bundleA.logs,
            selectedCategory = bundleA.category,
            searchQuery = bundleB.query,
            isDarkMode = bundleB.darkMode,
            showConfetti = bundleB.confetti,
            xp = bundleB.xp,
            level = Gamification.getLevel(bundleB.xp),
            levelProgress = Gamification.getLevelProgress(bundleB.xp),
            badges = bundleB.badges,
            activeFocusHabit = activeHabit,
            timerSeconds = bundleC.seconds,
            isTimerRunning = bundleC.running,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TrackingUiState(isLoading = true)
    )

    // Navigation & UI State Transitions
    fun navigateTo(view: TrackingView) {
        _currentTrackingView.value = view
    }

    fun handleBackPress(): Boolean {
        if (_currentTrackingView.value != TrackingView.DASHBOARD) {
            _currentTrackingView.value = TrackingView.DASHBOARD
            return true
        }
        return false
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleTheme() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun dismissConfetti() {
        _showConfetti.value = false
    }

    // Focus & Timer Engine
    fun selectFocusHabit(habit: Habit?) {
        _activeFocusHabitId.value = habit?.id
        pauseTimer()
        _timerSeconds.value = 0
    }

    fun startTimer() {
        if (_isTimerRunning.value) return
        _isTimerRunning.value = true
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && _isTimerRunning.value) {
                delay(1000)
                _timerSeconds.value += 1
            }
        }
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
        timerJob = null
    }

    fun resetTimer() {
        pauseTimer()
        _timerSeconds.value = 0
    }

    fun addTimerSeconds(secondsToAdd: Int) {
        _timerSeconds.value = (_timerSeconds.value + secondsToAdd).coerceAtLeast(0)
    }

    fun finishFocusSession(habit: Habit) {
        pauseTimer()
        val elapsedMinutes = (_timerSeconds.value / 60).coerceAtLeast(1)
        updateHabitProgress(habit, elapsedMinutes)
        _timerSeconds.value = 0
    }

    // Habit CRUD & Completion Logic
    fun toggleHabitCompletion(habit: Habit) {
        viewModelScope.launch {
            val wasAlreadyComplete = habit.isCompleted
            val updated = repository.toggleHabitCompletion(habit)

            // Trigger confetti if all active habits are now complete
            if (!wasAlreadyComplete && updated.isCompleted) {
                val currentHabits = allHabits.value
                val remainingIncomplete = currentHabits.count { it.id != habit.id && !it.isCompleted }
                if (remainingIncomplete == 0 && currentHabits.isNotEmpty()) {
                    _showConfetti.value = true
                }
            }
        }
    }

    fun updateHabitProgress(habit: Habit, delta: Int) {
        viewModelScope.launch {
            val newVal = habit.currentValue + delta
            val updated = repository.updateHabitProgress(habit, newVal)

            if (!habit.isCompleted && updated.isCompleted) {
                val currentHabits = allHabits.value
                val remainingIncomplete = currentHabits.count { it.id != habit.id && !it.isCompleted }
                if (remainingIncomplete == 0 && currentHabits.isNotEmpty()) {
                    _showConfetti.value = true
                }
            }
        }
    }

    fun addHabit(
        title: String,
        emoji: String = "✨",
        category: String = "General",
        type: HabitType = HabitType.CHECKBOX,
        targetValue: Int = 1,
        reminderTime: String? = null,
        reminderEnabled: Boolean = false
    ) {
        viewModelScope.launch {
            val habit = Habit(
                title = title,
                emoji = emoji,
                category = category,
                type = type,
                targetValue = targetValue,
                reminderTime = reminderTime,
                reminderEnabled = reminderEnabled
            )
            val id = repository.insertHabit(habit)
            if (reminderEnabled && reminderTime != null) {
                ReminderScheduler.scheduleHabitReminder(getApplication(), habit.copy(id = id))
            }
        }
    }

    fun addParsedHabit(parsed: ParsedHabit) {
        addHabit(
            title = parsed.title,
            emoji = parsed.emoji,
            category = parsed.category,
            type = parsed.type,
            targetValue = parsed.targetValue,
            reminderTime = parsed.reminderTime,
            reminderEnabled = parsed.reminderTime != null
        )
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            repository.updateHabit(habit)
            ReminderScheduler.scheduleHabitReminder(getApplication(), habit)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            ReminderScheduler.cancelHabitReminder(getApplication(), habit.id)
            repository.deleteHabit(habit)
            if (_activeFocusHabitId.value == habit.id) {
                _activeFocusHabitId.value = null
                resetTimer()
            }
        }
    }

    fun addReflectionNote(habitId: Long, note: String) {
        viewModelScope.launch {
            repository.addReflectionNote(habitId, note)
        }
    }

    fun resetToday() {
        viewModelScope.launch {
            repository.resetTodayProgress(allHabits.value)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            allHabits.value.forEach {
                ReminderScheduler.cancelHabitReminder(getApplication(), it.id)
            }
            repository.clearAll()
            resetTimer()
            _activeFocusHabitId.value = null
        }
    }

    fun importHabits(imported: List<Habit>) {
        viewModelScope.launch {
            repository.insertHabits(imported)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

private data class StateBundleA(
    val view: TrackingView,
    val habits: List<Habit>,
    val filtered: List<Habit>,
    val logs: List<HabitLog>,
    val category: String
)

private data class StateBundleB(
    val query: String,
    val darkMode: Boolean,
    val confetti: Boolean,
    val xp: Int,
    val badges: List<Badge>
)

private data class StateBundleC(
    val activeHabitId: Long?,
    val seconds: Int,
    val running: Boolean
)
