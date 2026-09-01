package com.personaluniversity.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.personaluniversity.app.data.model.ExamCatalogEntry
import com.personaluniversity.app.data.repository.SpacedRepetitionRepository
import com.personaluniversity.app.ui.theme.*

@Composable
fun SyllabusCatalogScreen(
    onOpenCourse: (Int) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val repo = remember { SpacedRepetitionRepository.instance }
    val exams = remember { repo.getExamCatalog() }

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CatalogTabButton(
                title = "Exam Syllabi (PYQ)",
                selected = selectedTab == 0,
                modifier = Modifier.weight(1f)
            ) { selectedTab = 0 }

            CatalogTabButton(
                title = "AI Tutor Courses",
                selected = selectedTab == 1,
                modifier = Modifier.weight(1f)
            ) { selectedTab = 1 }
        }

        HorizontalDivider(color = RuleLine)

        if (selectedTab == 0) {
            ExamCatalogTab(exams = exams)
        } else {
            TutorEntryScreen(onOpenCourse = onOpenCourse)
        }
    }
}

@Composable
private fun CatalogTabButton(
    title: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .background(if (selected) SurfaceRaised else Surface, RoundedCornerShape(6.dp))
            .border(1.dp, if (selected) GoldDim else RuleLine, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            title,
            style = AppType.body.copy(
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) Gold else TextMuted
            )
        )
    }
}

@Composable
private fun ExamCatalogTab(exams: List<ExamCatalogEntry>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text("PREDEFINED EXAMS · PYQ WEIGHTAGE", style = AppType.eyebrow.copy(color = Gold))
        Spacer(Modifier.height(6.dp))
        Text("Targeted Syllabus Breakdown", style = AppType.displayTitle)
        Spacer(Modifier.height(4.dp))
        Text(
            "Topics analyzed and ranked by historical appearance frequency in real past papers (PYQ).",
            style = AppType.lede
        )
        Spacer(Modifier.height(20.dp))

        exams.forEach { exam ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .background(Surface, RoundedCornerShape(10.dp))
                    .border(1.dp, RuleLine, RoundedCornerShape(10.dp))
                    .padding(18.dp)
            ) {
                Text(exam.category.uppercase(), style = AppType.meta.copy(color = Gold))
                Spacer(Modifier.height(4.dp))
                Text(exam.name, style = AppType.displayCard.copy(fontSize = 17.sp))
                Text(exam.targetAudience, style = AppType.lede.copy(color = TextMuted))
                Spacer(Modifier.height(14.dp))

                Text("HIGH-YIELD TOPICS", style = AppType.meta.copy(color = TextMuted))
                Spacer(Modifier.height(8.dp))

                exam.syllabusTopics.forEach { topic ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .background(SurfaceRaised, RoundedCornerShape(6.dp))
                            .border(1.dp, RuleLine, RoundedCornerShape(6.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(topic.title, style = AppType.body.copy(fontWeight = FontWeight.Medium))
                            Text(topic.frequencySummary, style = AppType.meta.copy(color = TextMuted))
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF282F1F), RoundedCornerShape(4.dp))
                                .border(1.dp, SuccessGreen, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "${(topic.weightageScore * 100).toInt()}% PYQ",
                                style = AppType.meta.copy(color = SuccessGreen, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}
