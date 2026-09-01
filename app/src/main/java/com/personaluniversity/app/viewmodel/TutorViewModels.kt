package com.personaluniversity.app.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personaluniversity.app.data.model.*
import com.personaluniversity.app.data.repository.UniversityRepository
import kotlinx.coroutines.launch

class CourseListViewModel(
    private val repo: UniversityRepository = UniversityRepository()
) : ViewModel() {

    val courses = mutableStateListOf<CourseSummary>()
    val isLoading = mutableStateOf(false)
    val isGenerating = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)
    val generatedCourseId = mutableStateOf<Int?>(null)

    init { refresh() }

    fun refresh() {
        isLoading.value = true
        viewModelScope.launch {
            repo.listCourses()
                .onSuccess { courses.apply { clear(); addAll(it) } }
                .onFailure { error.value = "Couldn't reach the backend. Is it running?" }
            isLoading.value = false
        }
    }

    fun generate(topic: String, difficulty: String, numModules: Int, lessonsPerModule: Int, goalContext: String?) {
        if (topic.isBlank()) return
        isGenerating.value = true
        error.value = null
        generatedCourseId.value = null

        viewModelScope.launch {
            repo.generateCourse(topic, difficulty, numModules, lessonsPerModule, goalContext.takeUnless { it.isNullOrBlank() })
                .onSuccess {
                    generatedCourseId.value = it.courseId
                    refresh()
                }
                .onFailure { error.value = "Generation failed — check the backend logs and your API key." }
            isGenerating.value = false
        }
    }
}

class CourseDetailViewModel(
    private val courseId: Int,
    private val repo: UniversityRepository = UniversityRepository()
) : ViewModel() {

    val course = mutableStateOf<CourseDetail?>(null)
    val isLoading = mutableStateOf(true)
    val error = mutableStateOf<String?>(null)

    init { load() }

    fun load() {
        isLoading.value = true
        viewModelScope.launch {
            repo.getCourse(courseId)
                .onSuccess { course.value = it }
                .onFailure { error.value = "Couldn't load this course." }
            isLoading.value = false
        }
    }
}

class LessonViewModel(
    private val lessonId: Int,
    private val courseId: Int = 0,
    private val repo: UniversityRepository = UniversityRepository(),
    private val srsRepo: com.personaluniversity.app.data.repository.SpacedRepetitionRepository = com.personaluniversity.app.data.repository.SpacedRepetitionRepository.instance
) : ViewModel() {

    val lesson = mutableStateOf<LessonDetail?>(null)
    val isLoading = mutableStateOf(true)
    val quizResults = mutableStateListOf<Pair<Int, QuizAnswerResponse>>() // questionId -> result

    init { load() }

    fun load() {
        isLoading.value = true
        viewModelScope.launch {
            repo.getLesson(lessonId)
                .onSuccess { lesson.value = it }
            isLoading.value = false
        }
    }

    fun markComplete() {
        viewModelScope.launch {
            repo.completeLesson(lessonId).onSuccess {
                val current = lesson.value ?: return@onSuccess
                lesson.value = current.copy(completed = true)
                // Automatically harvest quiz check questions into spaced repetition queue
                srsRepo.harvestFromLesson(
                    courseId = courseId,
                    lessonId = lessonId,
                    topic = current.title,
                    quiz = current.quiz
                )
            }
        }
    }

    fun answerQuiz(questionId: Int, selected: String) {
        viewModelScope.launch {
            repo.answerQuiz(questionId, selected).onSuccess { result ->
                quizResults.removeAll { it.first == questionId }
                quizResults.add(questionId to result)
            }
        }
    }
}
