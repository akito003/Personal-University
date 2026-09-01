package com.personaluniversity.app.data.model

import com.google.gson.annotations.SerializedName

// ---------- Chat ----------

data class ChatMessageDto(
    val role: String,          // "user" | "assistant"
    val content: String
)

data class ChatRequest(
    @SerializedName("lesson_id") val lessonId: Int? = null,
    val message: String,
    val mode: String = "tutor",          // advisor | tutor | editor | roommate
    @SerializedName("thread_id") val threadId: String? = null
)

data class ChatResponse(
    val reply: String
)

// ---------- Librarian ----------

data class LibrarianSource(
    val title: String,
    val type: String,
    val why: String
)

data class LibrarianResponse(
    val sources: List<LibrarianSource>,
    @SerializedName("study_tip") val studyTip: String
)

// ---------- Courses ----------

data class CourseGenerateRequest(
    val topic: String,
    val difficulty: String = "beginner",
    @SerializedName("num_modules") val numModules: Int = 5,
    @SerializedName("lessons_per_module") val lessonsPerModule: Int = 3,
    @SerializedName("goal_context") val goalContext: String? = null
)

data class CourseGenerateResponse(
    @SerializedName("course_id") val courseId: Int,
    val title: String
)

data class CourseSummary(
    val id: Int,
    val title: String,
    val topic: String,
    val difficulty: String
)

data class LessonSummary(
    val id: Int,
    val title: String,
    val completed: Boolean
)

data class ModuleDetail(
    val id: Int,
    val title: String,
    val summary: String?,
    val lessons: List<LessonSummary>
)

data class CourseDetail(
    val id: Int,
    val title: String,
    val description: String?,
    val modules: List<ModuleDetail>
)

data class QuizQuestionDto(
    val id: Int,
    val question: String,
    val options: List<String>
)

data class LessonDetail(
    val id: Int,
    val title: String,
    val content: String,
    val completed: Boolean,
    val quiz: List<QuizQuestionDto>
)

data class QuizAnswerRequest(
    @SerializedName("question_id") val questionId: Int,
    @SerializedName("selected_answer") val selectedAnswer: String
)

data class QuizAnswerResponse(
    val correct: Boolean,
    @SerializedName("correct_answer") val correctAnswer: String,
    val explanation: String?
)

data class SimpleOk(val ok: Boolean)

// ---------- Kaizen Spaced Repetition & Exam Catalog ----------

data class RecallUnit(
    val id: String,
    val question: String,
    val answer: String,
    val explanation: String? = null,
    val topic: String,
    val difficulty: String = "medium",
    val easeFactor: Double = 2.5,
    val intervalDays: Int = 0,
    val repetitions: Int = 0,
    val nextReviewEpochMs: Long = System.currentTimeMillis(),
    val lastReviewedEpochMs: Long? = null,
    val sourceCourseId: Int? = null,
    val sourceLessonId: Int? = null
)

data class SyllabusTopic(
    val topicId: String,
    val title: String,
    val weightageScore: Double,          // Historical frequency score (0.0 to 1.0)
    val frequencySummary: String,        // e.g. "Appears in ~18% of past papers"
    val subtopics: List<String> = emptyList()
)

data class ExamCatalogEntry(
    val examId: String,
    val name: String,
    val category: String,
    val targetAudience: String,
    val syllabusTopics: List<SyllabusTopic>
)

data class DailyProgress(
    val currentStreak: Int = 3,
    val reviewsDueToday: Int = 12,
    val reviewsCompletedToday: Int = 5,
    val totalMasteredUnits: Int = 48,
    val retentionRatePercent: Int = 89
)
