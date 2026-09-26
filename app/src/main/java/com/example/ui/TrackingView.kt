package com.example.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.ui.graphics.vector.ImageVector

enum class TrackingView(
    val title: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
    val testTag: String
) {
    DASHBOARD(
        title = "Today",
        filledIcon = Icons.Filled.CheckCircle,
        outlinedIcon = Icons.Outlined.CheckCircle,
        testTag = "nav_today"
    ),
    ANALYTICS(
        title = "Analytics",
        filledIcon = Icons.Filled.Timeline,
        outlinedIcon = Icons.Outlined.Timeline,
        testTag = "nav_analytics"
    ),
    FOCUS_TIMER(
        title = "Focus",
        filledIcon = Icons.Filled.Timer,
        outlinedIcon = Icons.Outlined.Timer,
        testTag = "nav_focus"
    ),
    ACHIEVEMENTS(
        title = "Badges",
        filledIcon = Icons.Filled.EmojiEvents,
        outlinedIcon = Icons.Outlined.EmojiEvents,
        testTag = "nav_badges"
    ),
    STUDIO(
        title = "AI Studio",
        filledIcon = Icons.Filled.AutoAwesome,
        outlinedIcon = Icons.Outlined.AutoAwesome,
        testTag = "nav_studio"
    )
}
