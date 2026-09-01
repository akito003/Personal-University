package com.personaluniversity.app.data.repository

import com.personaluniversity.app.data.model.*
import com.personaluniversity.app.data.network.RetrofitClient

/**
 * Thin wrapper around ApiService that turns exceptions into Result so
 * ViewModels don't need try/catch scattered everywhere.
 */
class UniversityRepository(private val api: com.personaluniversity.app.data.network.ApiService = RetrofitClient.api) {

    suspend fun sendChat(message: String, mode: String, lessonId: Int? = null, threadId: String? = null): Result<ChatResponse> =
        runCatching { api.sendChat(ChatRequest(lessonId = lessonId, message = message, mode = mode, threadId = threadId)) }

    suspend fun chatHistory(mode: String, lessonId: Int? = null, threadId: String? = null): Result<List<ChatMessageDto>> =
        runCatching { api.chatHistory(mode, lessonId, threadId) }

    suspend fun curateSources(topic: String, goalContext: String?): Result<LibrarianResponse> =
        runCatching { api.curateSources(topic, goalContext) }

    suspend fun generateCourse(
        topic: String,
        difficulty: String,
        numModules: Int,
        lessonsPerModule: Int,
        goalContext: String?
    ): Result<CourseGenerateResponse> =
        runCatching {
            api.generateCourse(
                CourseGenerateRequest(topic, difficulty, numModules, lessonsPerModule, goalContext)
            )
        }

    suspend fun listCourses(): Result<List<CourseSummary>> =
        runCatching { api.listCourses() }

    suspend fun getCourse(id: Int): Result<CourseDetail> =
        runCatching { api.getCourse(id) }

    suspend fun getLesson(id: Int): Result<LessonDetail> =
        runCatching { api.getLesson(id) }

    suspend fun completeLesson(id: Int): Result<SimpleOk> =
        runCatching { api.completeLesson(id) }

    suspend fun answerQuiz(questionId: Int, selectedAnswer: String): Result<QuizAnswerResponse> =
        runCatching { api.answerQuiz(QuizAnswerRequest(questionId, selectedAnswer)) }
}
