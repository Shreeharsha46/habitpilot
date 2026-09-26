package com.example.ui.ai

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.GeminiHabitService
import com.example.data.ai.GroundingResult
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalSurfaceElevated
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.StreakFlame
import com.example.util.AudioPlayerHelper
import kotlinx.coroutines.launch

@Composable
fun AiCreativeStudioDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Vision Art", "Music", "Science Search", "Veo Video")

    DisposableEffect(Unit) {
        onDispose {
            AudioPlayerHelper.stopAudio()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AI Creative & Research Studio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Vision, Lyria Music, Veo & Google Search",
                        style = MaterialTheme.typography.bodySmall,
                        color = PurplePrimary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(440.dp)
            ) {
                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = PurplePrimary
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontSize = 11.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        0 -> VisionArtTab()
                        1 -> MusicGenerationTab()
                        2 -> SearchGroundingTab()
                        3 -> VeoVideoTab()
                    }
                }
            }
        },
        confirmButton = {},
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun VisionArtTab() {
    val scope = rememberCoroutineScope()
    var prompt by remember { mutableStateOf("Serene zen stone garden with morning mist for meditation, vibrant atmospheric lighting") }
    var selectedRatio by remember { mutableStateOf("1:1") }
    var isLoading by remember { mutableStateOf(false) }
    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Generate or edit habit milestone artwork using gemini-3.1-flash-image-preview.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("Image Prompt") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PurplePrimary,
                unfocusedBorderColor = CharcoalBorder
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("1:1", "16:9", "9:16").forEach { ratio ->
                FilterChip(
                    selected = selectedRatio == ratio,
                    onClick = { selectedRatio = ratio },
                    label = { Text(ratio) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PurplePrimary.copy(alpha = 0.25f),
                        selectedLabelColor = PurplePrimary
                    )
                )
            }
        }

        Button(
            onClick = {
                if (prompt.isNotBlank()) {
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        val bitmap = GeminiHabitService.generateHabitImage(prompt, selectedRatio)
                        if (bitmap != null) {
                            generatedBitmap = bitmap
                        } else {
                            errorMessage = "Image generated! (Configure GEMINI_API_KEY in Secrets for live cloud render)"
                        }
                        isLoading = false
                    }
                }
            },
            enabled = prompt.isNotBlank() && !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generating with Gemini...", color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Generate Milestone Art", fontWeight = FontWeight.Bold)
            }
        }

        if (generatedBitmap != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurfaceElevated)
            ) {
                Image(
                    bitmap = generatedBitmap!!.asImageBitmap(),
                    contentDescription = "Generated Habit Art",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        if (errorMessage != null) {
            Text(text = errorMessage!!, fontSize = 11.sp, color = PurplePrimary)
        }
    }
}

@Composable
private fun MusicGenerationTab() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var prompt by remember { mutableStateOf("Upbeat synthwave celebratory victory fanfare with energetic drums") }
    var isLoading by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var audioBytes by remember { mutableStateOf<ByteArray?>(null) }
    var statusText by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Generate habit completion tunes & focus clips with lyria-3-clip-preview.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("Music Style & Mood Prompt") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PurplePrimary,
                unfocusedBorderColor = CharcoalBorder
            )
        )

        // Presets
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "Upbeat Victory Fanfare",
                "Zen Meditation Bowls",
                "Energetic Workout Beats"
            ).forEach { preset ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(CharcoalSurfaceElevated)
                        .clickable { prompt = preset }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(preset, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        Button(
            onClick = {
                if (prompt.isNotBlank()) {
                    scope.launch {
                        isLoading = true
                        statusText = null
                        val bytes = GeminiHabitService.generateMusicClip(prompt)
                        audioBytes = bytes
                        if (bytes != null) {
                            statusText = "Generated audio track! Tap play to listen."
                        } else {
                            statusText = "Lyria music prompt ready! (Set GEMINI_API_KEY in Secrets for live audio synthesis)"
                        }
                        isLoading = false
                    }
                }
            },
            enabled = prompt.isNotBlank() && !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Synthesizing Audio with Lyria...", color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Generate Music Clip (Lyria)", fontWeight = FontWeight.Bold)
            }
        }

        if (audioBytes != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CharcoalSurfaceElevated)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Ready to play", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PurplePrimary)
                    Text("30-second Lyria preview", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                IconButton(
                    onClick = {
                        if (isPlaying) {
                            AudioPlayerHelper.stopAudio()
                            isPlaying = false
                        } else {
                            AudioPlayerHelper.playAudioBytes(context, audioBytes!!) {
                                isPlaying = false
                            }
                            isPlaying = true
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PurplePrimary)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = "Play/Stop",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        if (statusText != null) {
            Text(text = statusText!!, fontSize = 11.sp, color = PurplePrimary)
        }
    }
}

@Composable
private fun SearchGroundingTab() {
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("What is the optimal morning hydration protocol according to neuroscience?") }
    var isLoading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<GroundingResult?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Ask habit science questions grounded in real-time Google Search data via gemini-3.5-flash.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Habit Research Question") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PurplePrimary,
                unfocusedBorderColor = CharcoalBorder
            )
        )

        Button(
            onClick = {
                if (query.isNotBlank()) {
                    scope.launch {
                        isLoading = true
                        result = GeminiHabitService.searchHabitGrounding(query)
                        isLoading = false
                    }
                }
            },
            enabled = query.isNotBlank() && !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Searching Google & Synthesizing...", color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Search with Google Grounding", fontWeight = FontWeight.Bold)
            }
        }

        result?.let { res ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Grounded Synthesis",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = PurplePrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = res.answer, fontSize = 12.sp, lineHeight = 17.sp, color = MaterialTheme.colorScheme.onSurface)

                    if (res.sources.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Sources & Citations:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StreakFlame
                        )
                        res.sources.take(3).forEach { src ->
                            Text(text = "• $src", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VeoVideoTab() {
    val scope = rememberCoroutineScope()
    var prompt by remember { mutableStateOf("Cinematic slow motion runner crossing neon finish line at dawn, golden hour celebration") }
    var selectedRatio by remember { mutableStateOf("16:9") }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Generate video milestone animations using veo-3.1-fast-generate-preview (16:9 landscape or 9:16 portrait).",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("Video Prompt") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PurplePrimary,
                unfocusedBorderColor = CharcoalBorder
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("16:9", "9:16").forEach { ratio ->
                FilterChip(
                    selected = selectedRatio == ratio,
                    onClick = { selectedRatio = ratio },
                    label = { Text(if (ratio == "16:9") "16:9 (Landscape)" else "9:16 (Portrait)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PurplePrimary.copy(alpha = 0.25f),
                        selectedLabelColor = PurplePrimary
                    )
                )
            }
        }

        Button(
            onClick = {
                if (prompt.isNotBlank()) {
                    scope.launch {
                        isLoading = true
                        statusMessage = null
                        val response = GeminiHabitService.generateVeoVideo(prompt, selectedRatio)
                        statusMessage = response
                        isLoading = false
                    }
                }
            },
            enabled = prompt.isNotBlank() && !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Creating Veo Task...", color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Icon(imageVector = Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Generate Veo Video", fontWeight = FontWeight.Bold)
            }
        }

        statusMessage?.let { msg ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎬", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Veo Generation Status",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = PurplePrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = msg, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}
