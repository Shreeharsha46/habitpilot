package com.example.ui.ai

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Habit
import com.example.data.HabitLog
import com.example.data.ai.GeminiHabitService
import com.example.data.ai.ParsedHabit
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalSurfaceElevated
import com.example.ui.theme.PurplePrimary
import kotlinx.coroutines.launch

data class ChatMessage(
    val sender: String, // "AI" or "USER"
    val message: String
)

@Composable
fun AiCoachDialog(
    habits: List<Habit>,
    logs: List<HabitLog>,
    onDismiss: () -> Unit,
    onAddParsedHabit: (ParsedHabit) -> Unit
) {
    val scope = rememberCoroutineScope()
    var userMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val chatMessages = remember {
        mutableStateListOf(
            ChatMessage("AI", "Hi! I'm your Gemini Consistency Coach. Ask me how to optimize your routine, stack habits, or parse a natural language goal!")
        )
    }

    LaunchedEffect(Unit) {
        // Automatically provide first personalized observation
        isLoading = true
        val advice = GeminiHabitService.getCoachAdvice(
            habits = habits,
            logs = logs,
            userMessage = "Provide a quick daily summary of my momentum and 1 actionable consistency tip."
        )
        chatMessages.add(ChatMessage("AI", advice))
        isLoading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(PurplePrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = PurplePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Consistency Coach",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
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
                    .height(380.dp)
            ) {
                // Messages List
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    chatMessages.forEach { msg ->
                        val isAi = msg.sender == "AI"
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentAlignment = if (isAi) Alignment.CenterStart else Alignment.CenterEnd
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isAi) CharcoalSurfaceElevated else PurplePrimary.copy(alpha = 0.25f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = msg.message,
                                    fontSize = 13.sp,
                                    color = if (isAi) MaterialTheme.colorScheme.onSurface else PurplePrimary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    if (isLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = PurplePrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Coach is thinking...",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Input Box
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = userMessage,
                        onValueChange = { userMessage = it },
                        placeholder = { Text("Ask advice or add habit...", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = CharcoalBorder
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (userMessage.isNotBlank()) {
                                val query = userMessage.trim()
                                userMessage = ""
                                chatMessages.add(ChatMessage("USER", query))
                                scope.launch {
                                    isLoading = true
                                    // Check if user is asking to create/add a habit
                                    val lower = query.lowercase()
                                    if (lower.startsWith("add ") || lower.startsWith("create ") || lower.contains("every morning") || lower.contains("every day")) {
                                        val parsed = GeminiHabitService.parseNaturalLanguageHabit(query)
                                        chatMessages.add(
                                            ChatMessage("AI", "Created new habit: ${parsed.emoji} ${parsed.title} (${parsed.category})!")
                                        )
                                        onAddParsedHabit(parsed)
                                    } else {
                                        val advice = GeminiHabitService.getCoachAdvice(habits, logs, query)
                                        chatMessages.add(ChatMessage("AI", advice))
                                    }
                                    isLoading = false
                                }
                            }
                        },
                        enabled = userMessage.isNotBlank() && !isLoading,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(PurplePrimary)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}
