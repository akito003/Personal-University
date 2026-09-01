package com.personaluniversity.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.personaluniversity.app.data.model.QuizQuestionDto
import com.personaluniversity.app.ui.theme.*
import com.personaluniversity.app.viewmodel.LessonChatViewModel
import com.personaluniversity.app.viewmodel.LessonViewModel

@Composable
fun LessonScreen(lessonId: Int, courseId: Int, onBack: () -> Unit) {
    val vm: LessonViewModel = viewModel(key = "lesson_$lessonId") { LessonViewModel(lessonId, courseId) }
    val chatVm: LessonChatViewModel = viewModel(key = "lessonchat_$lessonId") { LessonChatViewModel(lessonId) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        BackRow(label = "Back to course", onBack = onBack)
        Spacer(Modifier.height(12.dp))

        if (vm.isLoading.value) {
            Text("Loading…", style = AppType.lede)
            return@Column
        }
        val lesson = vm.lesson.value ?: return@Column

        Text("T — TUTOR", style = AppType.eyebrow.copy(color = Gold))
        Spacer(Modifier.height(6.dp))
        Text(lesson.title, style = AppType.displayTitle)
        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface, RoundedCornerShape(8.dp))
                .border(1.dp, RuleLine, RoundedCornerShape(8.dp))
                .padding(20.dp)
        ) {
            Text(lesson.content, style = AppType.body)
        }
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { vm.markComplete() },
            enabled = !lesson.completed,
            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color(0xFF14100A)),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(if (lesson.completed) "✓ Completed" else "Mark as complete")
        }

        if (lesson.quiz.isNotEmpty()) {
            Spacer(Modifier.height(28.dp))
            Text("QUICK CHECK", style = AppType.eyebrow.copy(color = Gold))
            Spacer(Modifier.height(10.dp))
            lesson.quiz.forEach { q ->
                QuizCard(
                    question = q,
                    result = vm.quizResults.firstOrNull { it.first == q.id }?.second,
                    onAnswer = { selected -> vm.answerQuiz(q.id, selected) }
                )
            }
        }

        Spacer(Modifier.height(32.dp))
        Text("DIAGNOSTIC CHAT", style = AppType.eyebrow.copy(color = Gold))
        Spacer(Modifier.height(4.dp))
        Text(
            "The Tutor won't lecture — it'll test you one question at a time on this lesson.",
            style = AppType.lede
        )
        Spacer(Modifier.height(12.dp))

        ChatPane(
            messages = chatVm.messages,
            isSending = chatVm.isSending.value,
            placeholder = "Ask a question, or say 'quiz me'",
            onSend = { chatVm.send(it) },
            modifier = Modifier.heightIn(min = 200.dp)
        )
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun QuizCard(
    question: QuizQuestionDto,
    result: com.personaluniversity.app.data.model.QuizAnswerResponse?,
    onAnswer: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .background(Surface, RoundedCornerShape(8.dp))
            .border(1.dp, RuleLine, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(question.question, style = AppType.body)
        Spacer(Modifier.height(10.dp))
        question.options.forEach { opt ->
            val isAnswered = result != null
            val isCorrectOpt = result?.correctAnswer == opt
            val bg = when {
                !isAnswered -> Surface
                isCorrectOpt -> Color(0xFF23301F)
                else -> Surface
            }
            val border = when {
                !isAnswered -> RuleLine
                isCorrectOpt -> SuccessGreen
                else -> RuleLine
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .background(bg, RoundedCornerShape(6.dp))
                    .border(1.dp, border, RoundedCornerShape(6.dp))
                    .clickable(enabled = !isAnswered) { onAnswer(opt) }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(opt, style = AppType.body)
            }
        }
        result?.let {
            Spacer(Modifier.height(6.dp))
            Text(
                if (it.correct) "Correct." else "Not quite — correct answer: ${it.correctAnswer}",
                style = AppType.lede.copy(color = if (it.correct) SuccessGreen else ErrorRed)
            )
            it.explanation?.let { exp ->
                Spacer(Modifier.height(2.dp))
                Text(exp, style = AppType.lede)
            }
        }
    }
}
