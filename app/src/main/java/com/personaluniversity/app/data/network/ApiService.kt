package com.personaluniversity.app.data.network

import com.personaluniversity.app.data.model.*
import retrofit2.http.*

interface ApiService {

    // ---------- Chat (Advisor / Tutor / Editor / Roommate) ----------

    @POST("api/chat")
    suspend fun sendChat(@Body request: ChatRequest): ChatResponse

    @GET("api/chat/history")
    suspend fun chatHistory(
        @Query("mode") mode: String,
        @Query("lesson_id") lessonId: Int? = null,
        @Query("thread_id") threadId: String? = null
    ): List<ChatMessageDto>

    // ---------- Librarian ----------

    @GET("api/librarian")
    suspend fun curateSources(
        @Query("topic") topic: String,
        @Query("goal_context") goalContext: String? = null
    ): LibrarianResponse

    // ---------- Courses ----------

    @POST("api/courses/generate")
    suspend fun generateCourse(@Body request: CourseGenerateRequest): CourseGenerateResponse

    @GET("api/courses")
    suspend fun listCourses(): List<CourseSummary>

    @GET("api/courses/{id}")
    suspend fun getCourse(@Path("id") id: Int): CourseDetail

    @GET("api/lessons/{id}")
    suspend fun getLesson(@Path("id") id: Int): LessonDetail

    @POST("api/lessons/{id}/complete")
    suspend fun completeLesson(@Path("id") id: Int): SimpleOk

    @POST("api/quiz/answer")
    suspend fun answerQuiz(@Body request: QuizAnswerRequest): QuizAnswerResponse
}
