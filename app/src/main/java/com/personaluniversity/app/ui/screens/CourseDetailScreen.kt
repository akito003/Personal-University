package com.personaluniversity.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.personaluniversity.app.data.model.LessonSummary
import com.personaluniversity.app.data.model.ModuleDetail
import com.personaluniversity.app.ui.theme.*
import com.personaluniversity.app.viewmodel.CourseDetailViewModel

@Composable
fun CourseDetailScreen(courseId: Int, onBack: () -> Unit, onOpenLesson: (Int) -> Unit) {
    val vm: CourseDetailViewModel = viewModel(key = "course_$courseId") { CourseDetailViewModel(courseId) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        BackRow(label = "All courses", onBack = onBack)
        Spacer(Modifier.height(12.dp))

        if (vm.isLoading.value) {
            Text("Loading…", style = AppType.lede)
            return@Column
        }
        vm.error.value?.let { Text(it, style = AppType.lede.copy(color = ErrorRed)); return@Column }

        val course = vm.course.value ?: return@Column
        Text("T — TUTOR", style = AppType.eyebrow.copy(color = Gold))
        Spacer(Modifier.height(6.dp))
        Text(course.title, style = AppType.displayTitle)
        course.description?.let {
            Spacer(Modifier.height(6.dp))
            Text(it, style = AppType.lede)
        }
        Spacer(Modifier.height(20.dp))

        course.modules.forEach { module -> ModuleBlock(module, onOpenLesson) }
    }
}

@Composable
private fun ModuleBlock(module: ModuleDetail, onOpenLesson: (Int) -> Unit) {
    Column(Modifier.padding(bottom = 20.dp)) {
        Text(module.title, style = AppType.displayCard)
        module.summary?.let {
            Spacer(Modifier.height(2.dp))
            Text(it, style = AppType.lede)
        }
        Spacer(Modifier.height(8.dp))
        module.lessons.forEach { lesson -> LessonRow(lesson) { onOpenLesson(lesson.id) } }
    }
}

@Composable
private fun LessonRow(lesson: LessonSummary, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
            .background(Surface, RoundedCornerShape(6.dp))
            .border(1.dp, RuleLine, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(lesson.title, style = AppType.body)
        Text(
            if (lesson.completed) "✓ done" else "pending",
            style = AppType.meta.copy(color = if (lesson.completed) SuccessGreen else TextMuted)
        )
    }
}

@Composable
fun BackRow(label: String, onBack: () -> Unit) {
    Row(
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        modifier = Modifier.clickable(onClick = onBack)
    ) {
        Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, style = AppType.body.copy(color = TextMuted))
    }
}
