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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.personaluniversity.app.data.model.CourseSummary
import com.personaluniversity.app.ui.theme.*
import com.personaluniversity.app.viewmodel.CourseListViewModel

@Composable
fun TutorEntryScreen(onOpenCourse: (Int) -> Unit) {
    val vm: CourseListViewModel = viewModel()
    var topic by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("beginner") }
    var numModules by remember { mutableStateOf("5") }
    var lessonsPerModule by remember { mutableStateOf("3") }
    var goal by remember { mutableStateOf("") }

    LaunchedEffect(vm.generatedCourseId.value) {
        vm.generatedCourseId.value?.let { onOpenCourse(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text("T — TUTOR", style = AppType.eyebrow.copy(color = Gold))
        Spacer(Modifier.height(6.dp))
        Text("Generate a course", style = AppType.displayTitle)
        Spacer(Modifier.height(6.dp))
        Text(
            "Not a lecture — a diagnostic. Generate a structured course, then the Tutor tests your understanding lesson by lesson.",
            style = AppType.lede
        )
        Spacer(Modifier.height(20.dp))

        AppTextField(value = topic, onValueChange = { topic = it }, label = "Topic",
            placeholder = "e.g. FastAPI backend development, SSC CGL quant")
        Spacer(Modifier.height(12.dp))

        DifficultyPicker(selected = difficulty, onSelect = { difficulty = it })
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AppTextField(
                value = numModules, onValueChange = { numModules = it.filter(Char::isDigit) },
                label = "Modules", placeholder = "5", modifier = Modifier.weight(1f)
            )
            AppTextField(
                value = lessonsPerModule, onValueChange = { lessonsPerModule = it.filter(Char::isDigit) },
                label = "Lessons/module", placeholder = "3", modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))

        AppTextField(value = goal, onValueChange = { goal = it }, label = "Goal context (optional)",
            placeholder = "e.g. from your Advisor roadmap")
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                vm.generate(
                    topic = topic,
                    difficulty = difficulty,
                    numModules = numModules.toIntOrNull() ?: 5,
                    lessonsPerModule = lessonsPerModule.toIntOrNull() ?: 3,
                    goalContext = goal
                )
            },
            enabled = !vm.isGenerating.value,
            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color(0xFF14100A)),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(if (vm.isGenerating.value) "Designing your course…" else "Generate course")
        }

        vm.error.value?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, style = AppType.lede.copy(color = ErrorRed))
        }
        if (vm.isGenerating.value) {
            Spacer(Modifier.height(8.dp))
            Text("This can take a minute — it's writing real lesson content.", style = AppType.lede)
        }

        Spacer(Modifier.height(32.dp))
        Text("YOUR COURSES", style = AppType.eyebrow.copy(color = Gold))
        Spacer(Modifier.height(10.dp))

        if (vm.isLoading.value) {
            Text("Loading…", style = AppType.lede)
        } else if (vm.courses.isEmpty()) {
            Text("No courses yet — generate your first one above.", style = AppType.lede)
        } else {
            vm.courses.forEach { course -> CourseCard(course) { onOpenCourse(course.id) } }
        }
    }
}

@Composable
private fun DifficultyPicker(selected: String, onSelect: (String) -> Unit) {
    val options = listOf("beginner", "intermediate", "advanced")
    Column {
        Text("Difficulty", style = AppType.meta.copy(color = TextMuted))
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { opt ->
                val isSel = opt == selected
                Box(
                    modifier = Modifier
                        .background(if (isSel) Gold else Surface, RoundedCornerShape(6.dp))
                        .border(1.dp, if (isSel) Gold else RuleLine, RoundedCornerShape(6.dp))
                        .clickable { onSelect(opt) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        opt.replaceFirstChar { it.uppercase() },
                        style = AppType.body.copy(color = if (isSel) Color(0xFF14100A) else Parchment)
                    )
                }
            }
        }
    }
}

@Composable
private fun CourseCard(course: CourseSummary, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .background(Surface, RoundedCornerShape(8.dp))
            .border(1.dp, RuleLine, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(course.title, style = AppType.displayCard)
        Spacer(Modifier.height(4.dp))
        Text("${course.difficulty} · ${course.topic}", style = AppType.meta.copy(color = TextMuted))
    }
}
