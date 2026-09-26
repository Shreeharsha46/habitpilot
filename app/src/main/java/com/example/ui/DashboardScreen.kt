package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Habit
import com.example.data.HabitType
import com.example.data.ai.GeminiHabitService
import com.example.ui.ai.AiCoachDialog
import com.example.ui.ai.AiCreativeStudioDialog
import com.example.ui.analytics.HeatmapView
import com.example.ui.analytics.TrendChartView
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CharcoalSurfaceElevated
import com.example.ui.theme.PurpleGlow
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurpleSecondary
import com.example.ui.theme.StreakFlame
import com.example.ui.views.AchievementsView
import com.example.ui.views.AnalyticsTrackingView
import com.example.ui.views.FocusTimerView
import com.example.ui.views.StudioView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var quickInputText by remember { mutableStateOf("") }
    var isAiParsing by remember { mutableStateOf(false) }

    // Dialog states
    var showAddDialog by remember { mutableStateOf(false) }
    var habitToEdit by remember { mutableStateOf<Habit?>(null) }
    var habitForNote by remember { mutableStateOf<Habit?>(null) }
    var showAiCoachDialog by remember { mutableStateOf(false) }
    var showAiStudioDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "Health", "Fitness", "Mind", "Productivity", "Routine")

    // Calculations for Top Progress Feed
    val totalCount = uiState.habits.size
    val completedCount = uiState.habits.count { it.isCompleted }
    val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
    val progressPercentage = (progressFraction * 100).toInt()

    val todayFormatted = remember {
        val sdf = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        sdf.format(Date()).uppercase()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                NavigationBar(
                    containerColor = CharcoalSurface,
                    contentColor = PurplePrimary,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("tracking_nav_bar")
                ) {
                    TrackingView.entries.forEach { viewItem ->
                        val isSelected = uiState.currentView == viewItem
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.navigateTo(viewItem)
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) viewItem.filledIcon else viewItem.outlinedIcon,
                                    contentDescription = viewItem.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = viewItem.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                selectedTextColor = PurplePrimary,
                                indicatorColor = PurplePrimary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag(viewItem.testTag)
                        )
                    }
                }
            },
            floatingActionButton = {
                if (uiState.currentView == TrackingView.DASHBOARD) {
                    FloatingActionButton(
                        onClick = { showAddDialog = true },
                        containerColor = PurplePrimary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = CircleShape,
                        modifier = Modifier.testTag("add_habit_fab")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Habit")
                    }
                }
            }
        ) { innerPadding ->
            // Animated UI state transitions between different tracking views
            AnimatedContent(
                targetState = uiState.currentView,
                transitionSpec = {
                    if (targetState.ordinal > initialState.ordinal) {
                        (slideInHorizontally(tween(300)) { it / 3 } + fadeIn(tween(300)))
                            .togetherWith(slideOutHorizontally(tween(300)) { -it / 3 } + fadeOut(tween(300)))
                    } else {
                        (slideInHorizontally(tween(300)) { -it / 3 } + fadeIn(tween(300)))
                            .togetherWith(slideOutHorizontally(tween(300)) { it / 3 } + fadeOut(tween(300)))
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                label = "trackingViewTransition"
            ) { targetView ->
                when (targetView) {
                    TrackingView.DASHBOARD -> {
                        TodayDashboardView(
                            uiState = uiState,
                            viewModel = viewModel,
                            todayFormatted = todayFormatted,
                            totalCount = totalCount,
                            completedCount = completedCount,
                            progressFraction = progressFraction,
                            progressPercentage = progressPercentage,
                            quickInputText = quickInputText,
                            isAiParsing = isAiParsing,
                            categories = categories,
                            onQuickInputChange = { quickInputText = it },
                            onQuickAddSubmit = { input ->
                                if (input.contains(" ") && (input.contains("min") || input.contains("every") || input.contains("water") || input.contains("run"))) {
                                    scope.launch {
                                        isAiParsing = true
                                        val parsed = GeminiHabitService.parseNaturalLanguageHabit(input)
                                        viewModel.addParsedHabit(parsed)
                                        isAiParsing = false
                                    }
                                } else {
                                    viewModel.addHabit(title = input)
                                }
                            },
                            onOpenAiCoach = { showAiCoachDialog = true },
                            onOpenAiStudio = { viewModel.navigateTo(TrackingView.STUDIO) },
                            onOpenBackup = { showBackupDialog = true },
                            onEditHabit = { habitToEdit = it },
                            onAddNoteForHabit = { habitForNote = it }
                        )
                    }

                    TrackingView.ANALYTICS -> {
                        AnalyticsTrackingView(
                            uiState = uiState,
                            viewModel = viewModel
                        )
                    }

                    TrackingView.FOCUS_TIMER -> {
                        FocusTimerView(
                            uiState = uiState,
                            viewModel = viewModel
                        )
                    }

                    TrackingView.ACHIEVEMENTS -> {
                        AchievementsView(
                            uiState = uiState,
                            viewModel = viewModel
                        )
                    }

                    TrackingView.STUDIO -> {
                        StudioView(
                            uiState = uiState,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }

        // Celebratory Confetti Particle Explosion
        ConfettiEffect(
            isActive = uiState.showConfetti,
            onFinished = { viewModel.dismissConfetti() }
        )
    }

    // Add / Edit Dialog
    if (showAddDialog || habitToEdit != null) {
        AddEditHabitDialog(
            initialHabit = habitToEdit,
            onDismiss = {
                showAddDialog = false
                habitToEdit = null
            },
            onConfirm = { title, emoji, category, type, target, reminder, reminderEnabled ->
                if (habitToEdit == null) {
                    viewModel.addHabit(
                        title = title,
                        emoji = emoji,
                        category = category,
                        type = type,
                        targetValue = target,
                        reminderTime = reminder,
                        reminderEnabled = reminderEnabled
                    )
                } else {
                    viewModel.updateHabit(
                        habitToEdit!!.copy(
                            title = title,
                            emoji = emoji,
                            category = category,
                            type = type,
                            targetValue = target,
                            reminderTime = reminder,
                            reminderEnabled = reminderEnabled
                        )
                    )
                }
                showAddDialog = false
                habitToEdit = null
            }
        )
    }

    // Daily Reflection Note Dialog
    if (habitForNote != null) {
        HabitNoteDialog(
            habit = habitForNote!!,
            onDismiss = { habitForNote = null },
            onSaveNote = { note ->
                viewModel.addReflectionNote(habitForNote!!.id, note)
                habitForNote = null
            }
        )
    }

    // AI Coach Dialog
    if (showAiCoachDialog) {
        AiCoachDialog(
            habits = uiState.habits,
            logs = uiState.logs,
            onDismiss = { showAiCoachDialog = false },
            onAddParsedHabit = { parsed ->
                viewModel.addParsedHabit(parsed)
            }
        )
    }

    // AI Creative Studio Dialog
    if (showAiStudioDialog) {
        AiCreativeStudioDialog(
            onDismiss = { showAiStudioDialog = false }
        )
    }

    // Data Backup / Restore Dialog
    if (showBackupDialog) {
        DataManagementDialog(
            habits = uiState.habits,
            onDismiss = { showBackupDialog = false },
            onImportHabits = { imported ->
                viewModel.importHabits(imported)
            },
            onClearAll = {
                viewModel.clearAll()
            }
        )
    }
}

@Composable
private fun TodayDashboardView(
    uiState: TrackingUiState,
    viewModel: HabitViewModel,
    todayFormatted: String,
    totalCount: Int,
    completedCount: Int,
    progressFraction: Float,
    progressPercentage: Int,
    quickInputText: String,
    isAiParsing: Boolean,
    categories: List<String>,
    onQuickInputChange: (String) -> Unit,
    onQuickAddSubmit: (String) -> Unit,
    onOpenAiCoach: () -> Unit,
    onOpenAiStudio: () -> Unit,
    onOpenBackup: () -> Unit,
    onEditHabit: (Habit) -> Unit,
    onAddNoteForHabit: (Habit) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Bar (Brand & Quick Actions)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = todayFormatted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = PurplePrimary
                    )
                    Text(
                        text = "Habit Tracker",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // AI Coach Button
                    IconButton(
                        onClick = onOpenAiCoach,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PurplePrimary.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Coach",
                            tint = PurplePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Backup & Restore
                    IconButton(
                        onClick = onOpenBackup,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SettingsBackupRestore,
                            contentDescription = "Backup",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Theme Toggle
                    IconButton(
                        onClick = { viewModel.toggleTheme() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Dynamic Progress Card (Top Progress Bar)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(
                        listOf(PurplePrimary.copy(alpha = 0.4f), CharcoalBorder.copy(alpha = 0.2f))
                    )
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TODAY'S MOMENTUM",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                color = PurplePrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (totalCount == 0) "No habits added yet" else "$completedCount of $totalCount completed",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(PurplePrimary.copy(alpha = 0.18f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$progressPercentage%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = PurplePrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Custom Linear Gradient Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(CharcoalSurfaceElevated)
                    ) {
                        val animatedProgress by animateFloatAsState(
                            targetValue = progressFraction,
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                            label = "progress"
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(PurpleSecondary, PurplePrimary, PurpleGlow)
                                    )
                                )
                        )
                    }

                    if (totalCount > 0 && completedCount == totalCount) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "🎉 All done! Perfect consistency day achieved.",
                            fontSize = 12.sp,
                            color = PurplePrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Quick-Add Input Field (with AI natural language parsing support)
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = quickInputText,
                        onValueChange = onQuickInputChange,
                        placeholder = {
                            Text("Add habit or try 'Read 15 min every night'...", fontSize = 13.sp)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_add_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = CharcoalBorder,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (quickInputText.isNotBlank()) {
                                val input = quickInputText.trim()
                                onQuickInputChange("")
                                onQuickAddSubmit(input)
                            }
                        },
                        enabled = quickInputText.isNotBlank() && !isAiParsing,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (quickInputText.isNotBlank()) PurplePrimary else CharcoalSurfaceElevated)
                            .testTag("quick_add_button")
                    ) {
                        if (isAiParsing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = if (quickInputText.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Presets Carousel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DEFAULT_HABIT_PRESETS.take(6).forEach { preset ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(CharcoalSurfaceElevated)
                                .border(1.dp, CharcoalBorder.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .clickable {
                                viewModel.addHabit(
                                    title = preset.title,
                                    emoji = preset.emoji,
                                    category = preset.category,
                                    type = preset.type,
                                    targetValue = preset.targetValue,
                                    reminderTime = preset.reminderTime,
                                    reminderEnabled = preset.reminderTime != null
                                )
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${preset.emoji} ${preset.title}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Category Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val count = if (category == "All") uiState.habits.size else uiState.habits.count { it.category.equals(category, true) }
                    FilterChip(
                        selected = uiState.selectedCategory == category,
                        onClick = { viewModel.setSelectedCategory(category) },
                        label = { Text("$category ($count)", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PurplePrimary.copy(alpha = 0.25f),
                            selectedLabelColor = PurplePrimary,
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        }

        // Habits List Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Habits",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (uiState.habits.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = { viewModel.resetToday() },
                            modifier = Modifier.testTag("reset_today_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset Today", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        TextButton(
                            onClick = { viewModel.clearAll() },
                            modifier = Modifier.testTag("clear_all_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ClearAll,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear All", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        // Habits Rows
        if (uiState.filteredHabits.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "🌱", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (uiState.habits.isEmpty()) "No habits yet!" else "No habits match this category",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Start by adding a simple habit above or tap a preset.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(uiState.filteredHabits, key = { it.id }) { habit ->
                HabitItemCard(
                    habit = habit,
                    onToggleComplete = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.toggleHabitCompletion(habit)
                    },
                    onProgressDelta = { delta ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.updateHabitProgress(habit, delta)
                    },
                    onEdit = { onEditHabit(habit) },
                    onDelete = { viewModel.deleteHabit(habit) },
                    onAddNote = { onAddNoteForHabit(habit) }
                )
            }
        }

        // Fast Tracking Navigation Banner Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickNavCard(
                    title = "Analytics",
                    subtitle = "7-Day & Heatmap",
                    icon = "📊",
                    onClick = { viewModel.navigateTo(TrackingView.ANALYTICS) },
                    modifier = Modifier.weight(1f)
                )
                QuickNavCard(
                    title = "Focus Timer",
                    subtitle = "Deep work",
                    icon = "⏱️",
                    onClick = { viewModel.navigateTo(TrackingView.FOCUS_TIMER) },
                    modifier = Modifier.weight(1f)
                )
                QuickNavCard(
                    title = "Badges",
                    subtitle = "${uiState.xp} XP",
                    icon = "🏆",
                    onClick = { viewModel.navigateTo(TrackingView.ACHIEVEMENTS) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun QuickNavCard(
    title: String,
    subtitle: String,
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(CharcoalBorder.copy(alpha = 0.4f))
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = PurplePrimary
            )
        }
    }
}

@Composable
fun HabitItemCard(
    habit: Habit,
    onToggleComplete: () -> Unit,
    onProgressDelta: (Int) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBackground by animateColorAsState(
        targetValue = if (habit.isCompleted) CharcoalSurfaceElevated.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surface,
        label = "cardBg"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("habit_card_${habit.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (habit.isCompleted) PurplePrimary.copy(alpha = 0.3f) else CharcoalBorder.copy(alpha = 0.4f)
            )
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Micro-Animated Circular Checkbox
                CircularCheckbox(
                    isCompleted = habit.isCompleted,
                    onCheckedChange = { onToggleComplete() },
                    modifier = Modifier.testTag("checkbox_${habit.id}")
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Emoji and Title
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = habit.emoji, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = habit.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (habit.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                            textDecoration = if (habit.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Metadata row: Category, Streak, Reminders
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Category pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CharcoalSurfaceElevated)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = habit.category,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Streak Badge
                        if (habit.streak > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🔥", fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${habit.streak}d streak",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StreakFlame
                                )
                            }
                        }

                        // Reminder Icon
                        if (habit.reminderEnabled && !habit.reminderTime.isNullOrBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = PurplePrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = habit.reminderTime,
                                    fontSize = 11.sp,
                                    color = PurplePrimary
                                )
                            }
                        }
                    }
                }

                // Actions: Note, Edit, Delete
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onAddNote,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Add Note",
                            tint = PurplePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Counter Type Controls
            if (habit.type == HabitType.COUNTER) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CharcoalSurfaceElevated)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Progress: ${habit.currentValue} / ${habit.targetValue}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(CharcoalBorder.copy(alpha = 0.5f))
                                .clickable { onProgressDelta(-1) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("-", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }

                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(PurplePrimary)
                                .clickable { onProgressDelta(1) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }

            // Timer Type Controls
            if (habit.type == HabitType.TIMER) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CharcoalSurfaceElevated)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Timer: ${habit.currentValue} / ${habit.targetValue} mins",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(PurplePrimary)
                                .clickable { onProgressDelta(5) }
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+5m", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CircularCheckbox(
    isCompleted: Boolean,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isCompleted) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isCompleted) PurplePrimary else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "bgColor"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isCompleted) PurplePrimary else CharcoalBorder,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "borderColor"
    )

    Box(
        modifier = modifier
            .size(36.dp)
            .scale(scale)
            .clip(CircleShape)
            .border(2.dp, borderColor, CircleShape)
            .background(bgColor)
            .clickable { onCheckedChange() },
        contentAlignment = Alignment.Center
    ) {
        if (isCompleted) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Completed",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
