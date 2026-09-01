package com.personaluniversity.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personaluniversity.app.data.repository.SpacedRepetitionRepository
import com.personaluniversity.app.ui.theme.*

@Composable
fun ProgressScreen() {
    val repo = remember { SpacedRepetitionRepository.instance }
    val progress by repo.progressState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text("KAIZEN · RETENTION METRICS", style = AppType.eyebrow.copy(color = Gold))
        Spacer(Modifier.height(6.dp))
        Text("Consistency & Mastery", style = AppType.displayTitle)
        Spacer(Modifier.height(4.dp))
        Text(
            "Tracking your memory stability and daily study consistency over time.",
            style = AppType.lede
        )
        Spacer(Modifier.height(20.dp))

        // Big Stats Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatBox(
                title = "CURRENT STREAK",
                value = "${progress.currentStreak} Days",
                subtitle = "Active streak",
                color = Gold,
                modifier = Modifier.weight(1f)
            )
            StatBox(
                title = "RETENTION RATE",
                value = "${progress.retentionRatePercent}%",
                subtitle = "SM-2 accuracy",
                color = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatBox(
                title = "COMPLETED TODAY",
                value = "${progress.reviewsCompletedToday} Cards",
                subtitle = "Daily queue cleared",
                color = Parchment,
                modifier = Modifier.weight(1f)
            )
            StatBox(
                title = "MASTERED CONCEPTS",
                value = "${progress.totalMasteredUnits}",
                subtitle = "Long interval units",
                color = Color(0xFF8AB4F8),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(28.dp))

        // Consistency Heatmap (Last 14 Days)
        Text("STUDY CONSISTENCY (LAST 14 DAYS)", style = AppType.eyebrow.copy(color = Gold))
        Spacer(Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface, RoundedCornerShape(10.dp))
                .border(1.dp, RuleLine, RoundedCornerShape(10.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Mock 14-day history dots
                val activity = listOf(1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1)
                activity.forEachIndexed { idx, done ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(
                                    if (done == 1) Gold else SurfaceRaised,
                                    RoundedCornerShape(3.dp)
                                )
                                .border(1.dp, if (done == 1) GoldDim else RuleLine, RoundedCornerShape(3.dp))
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("D${idx + 1}", style = AppType.meta.copy(fontSize = 8.sp, color = TextMuted))
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("Tip: Short 5-minute reviews protect your ease factors and prevent exponential forgotten decay.", style = AppType.lede)
        }

        Spacer(Modifier.height(28.dp))

        // Topic Mastery Distribution
        Text("ACTIVE TOPIC RETENTION", style = AppType.eyebrow.copy(color = Gold))
        Spacer(Modifier.height(10.dp))

        TopicMasteryRow("FastAPI Architecture & Async", 0.94)
        TopicMasteryRow("Spaced Repetition & SM-2 Algorithmic Memory", 0.88)
        TopicMasteryRow("Database Engineering & ACID Properties", 0.76)

        Spacer(Modifier.height(28.dp))

        // Server Connection Settings Card
        Text("BACKEND CONFIGURATION", style = AppType.eyebrow.copy(color = Gold))
        Spacer(Modifier.height(10.dp))
        ServerConfigCard()
    }
}

@Composable
private fun ServerConfigCard() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var currentUrl by remember { mutableStateOf(com.personaluniversity.app.data.network.RetrofitClient.getBaseUrl()) }
    var inputUrl by remember { mutableStateOf(currentUrl) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(8.dp))
            .border(1.dp, RuleLine, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Backend Host", style = AppType.body.copy(fontWeight = FontWeight.SemiBold))
                Spacer(Modifier.height(2.dp))
                Text(currentUrl, style = AppType.meta.copy(color = Gold))
            }
            Button(
                onClick = {
                    inputUrl = currentUrl
                    showDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceRaised, contentColor = Gold),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldDim),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("Edit URL", fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Connect to local Wi-Fi, Cloudflare Tunnel, or a 24/7 Render cloud deployment from any network.",
            style = AppType.lede
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Configure Backend Host", style = AppType.displayCard) },
            text = {
                Column {
                    Text(
                        "Enter the URL of your ALTER backend (e.g. https://your-app.onrender.com or http://192.168.1.5:8000/):",
                        style = AppType.lede
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = { inputUrl = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Gold,
                            unfocusedBorderColor = RuleLine,
                            focusedContainerColor = Surface,
                            unfocusedContainerColor = Surface
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputUrl.isNotBlank()) {
                            com.personaluniversity.app.data.network.RetrofitClient.updateBaseUrl(context, inputUrl)
                            currentUrl = com.personaluniversity.app.data.network.RetrofitClient.getBaseUrl()
                            showDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color(0xFF14100A))
                ) {
                    Text("Save & Connect")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceRaised,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun StatBox(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Surface, RoundedCornerShape(8.dp))
            .border(1.dp, RuleLine, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(title, style = AppType.meta.copy(color = TextMuted, fontSize = 9.sp))
        Spacer(Modifier.height(6.dp))
        Text(value, style = AppType.displayCard.copy(fontSize = 20.sp, color = color, fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(2.dp))
        Text(subtitle, style = AppType.meta.copy(color = TextMuted, fontSize = 10.sp))
    }
}

@Composable
private fun TopicMasteryRow(name: String, progressFraction: Double) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .background(Surface, RoundedCornerShape(8.dp))
            .border(1.dp, RuleLine, RoundedCornerShape(8.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(name, style = AppType.body.copy(fontWeight = FontWeight.Medium, fontSize = 13.sp))
            Text("${(progressFraction * 100).toInt()}%", style = AppType.meta.copy(color = Gold))
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progressFraction.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = Gold,
            trackColor = SurfaceRaised,
        )
    }
}
