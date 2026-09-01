package com.personaluniversity.app.data.repository

import com.personaluniversity.app.data.model.*
import com.personaluniversity.app.data.spacedrepetition.RecallRating
import com.personaluniversity.app.data.spacedrepetition.Sm2Scheduler
import com.personaluniversity.app.data.spacedrepetition.Sm2State
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Repository managing Kaizen Spaced Repetition recall units, exam syllabi,
 * and automated harvesting from completed lessons.
 */
class SpacedRepetitionRepository private constructor() {

    private val units = mutableListOf<RecallUnit>()
    private val _queueState = MutableStateFlow<List<RecallUnit>>(emptyList())
    val queueState: StateFlow<List<RecallUnit>> = _queueState.asStateFlow()

    private val _progressState = MutableStateFlow(DailyProgress())
    val progressState: StateFlow<DailyProgress> = _progressState.asStateFlow()

    init {
        seedInitialUnits()
        refreshQueue()
    }

    private fun seedInitialUnits() {
        if (units.isNotEmpty()) return
        units.addAll(
            listOf(
                RecallUnit(
                    id = "seed_1",
                    question = "In FastAPI, what is the key difference between using 'async def' vs standard 'def' for an endpoint?",
                    answer = "FastAPI runs 'async def' directly in the main event loop, whereas standard 'def' runs inside an external threadpool.",
                    explanation = "If you perform blocking I/O inside 'async def', you will stall the whole event loop!",
                    topic = "FastAPI Architecture",
                    difficulty = "medium"
                ),
                RecallUnit(
                    id = "seed_2",
                    question = "What is the primary trade-off of the SM-2 spaced repetition algorithm?",
                    answer = "It assumes memory follows an exponential decay and only adjusts based on user self-rating, making it simple but vulnerable to subjective grading bias.",
                    explanation = "Newer algorithms like FSRS use machine learning, but SM-2 remains robust and computationally lightweight.",
                    topic = "Learning Science",
                    difficulty = "easy"
                ),
                RecallUnit(
                    id = "seed_3",
                    question = "In relational databases, what does the ACID 'Isolation' property guarantee?",
                    answer = "Concurrent transactions execute without interfering with one another, producing the same state as if run sequentially.",
                    explanation = "Isolation levels include Read Uncommitted, Read Committed, Repeatable Read, and Serializable.",
                    topic = "Database Engineering",
                    difficulty = "hard"
                )
            )
        )
    }

    fun refreshQueue() {
        val now = System.currentTimeMillis()
        val due = units.filter { it.nextReviewEpochMs <= now }
        val fresh = units.filter { it.repetitions == 0 && it.nextReviewEpochMs > now }
        _queueState.value = Sm2Scheduler.buildDailyQueue(due, fresh, maxDueQuota = 20, maxNewQuota = 5)
        
        _progressState.value = _progressState.value.copy(
            reviewsDueToday = _queueState.value.size,
            totalMasteredUnits = units.count { it.repetitions >= 3 }
        )
    }

    fun recordReview(unitId: String, rating: RecallRating) {
        val index = units.indexOfFirst { it.id == unitId }
        if (index == -1) return

        val current = units[index]
        val sm2State = Sm2Scheduler.schedule(
            currentState = Sm2State(
                easeFactor = current.easeFactor,
                intervalDays = current.intervalDays,
                repetitions = current.repetitions,
                nextReviewEpochMs = current.nextReviewEpochMs
            ),
            rating = rating
        )

        val updated = current.copy(
            easeFactor = sm2State.easeFactor,
            intervalDays = sm2State.intervalDays,
            repetitions = sm2State.repetitions,
            nextReviewEpochMs = sm2State.nextReviewEpochMs,
            lastReviewedEpochMs = System.currentTimeMillis()
        )

        units[index] = updated
        _queueState.value = _queueState.value.filter { it.id != unitId }

        val oldProg = _progressState.value
        _progressState.value = oldProg.copy(
            reviewsCompletedToday = oldProg.reviewsCompletedToday + 1,
            reviewsDueToday = _queueState.value.size,
            totalMasteredUnits = units.count { it.repetitions >= 3 }
        )
    }

    /**
     * Harvests quiz questions from a completed course lesson into the user's
     * active Spaced Repetition flashcard deck.
     */
    fun harvestFromLesson(
        courseId: Int,
        lessonId: Int,
        topic: String,
        quiz: List<QuizQuestionDto>
    ) {
        quiz.forEach { q ->
            // Avoid duplicate cards
            val cardId = "lesson_${lessonId}_q_${q.id}"
            if (units.none { it.id == cardId }) {
                units.add(
                    RecallUnit(
                        id = cardId,
                        question = q.question,
                        answer = q.options.firstOrNull() ?: "See lesson content",
                        explanation = "Generated from Lesson #$lessonId check-in quiz.",
                        topic = topic,
                        sourceCourseId = courseId,
                        sourceLessonId = lessonId,
                        nextReviewEpochMs = System.currentTimeMillis() // Due immediately
                    )
                )
            }
        }
        refreshQueue()
    }

    fun getExamCatalog(): List<ExamCatalogEntry> {
        return listOf(
            ExamCatalogEntry(
                examId = "ssc_cgl",
                name = "SSC CGL (Combined Graduate Level)",
                category = "Civil Services & Administration",
                targetAudience = "National recruitment examination",
                syllabusTopics = listOf(
                    SyllabusTopic("ssc_quant", "Quantitative Aptitude", 0.92, "Appears in 25% of Tier 1 & 2 exams", listOf("Number Systems", "Profit & Loss", "Time & Work", "Trigonometry")),
                    SyllabusTopic("ssc_reasoning", "General Intelligence & Reasoning", 0.88, "High scoring, high frequency", listOf("Analogy", "Blood Relations", "Syllogisms")),
                    SyllabusTopic("ssc_english", "English Comprehension", 0.84, "Grammar, Cloze Test, Reading Passages", listOf("Active/Passive", "Vocabulary", "Idioms"))
                )
            ),
            ExamCatalogEntry(
                examId = "rbi_grade_b",
                name = "RBI Grade B Officer Examination",
                category = "Central Banking & Finance",
                targetAudience = "Finance & Economics aspirants",
                syllabusTopics = listOf(
                    SyllabusTopic("rbi_esi", "Economic & Social Issues (ESI)", 0.95, "Mandatory descriptive + objective", listOf("Monetary Policy", "Inflation Targeting", "Union Budget")),
                    SyllabusTopic("rbi_fm", "Finance & Management", 0.90, "Corporate governance & Financial markets", listOf("Derivatives", "Fintech", "Leadership theories"))
                )
            ),
            ExamCatalogEntry(
                examId = "cs_core",
                name = "Computer Science Core Systems",
                category = "Software Engineering",
                targetAudience = "Software & Systems Engineers",
                syllabusTopics = listOf(
                    SyllabusTopic("cs_os", "Operating Systems & Concurrency", 0.94, "Essential for backend interviews", listOf("Virtual Memory", "Threads & Locks", "File Systems")),
                    SyllabusTopic("cs_networks", "Computer Networking (TCP/IP)", 0.91, "Core infrastructure fundamentals", listOf("TCP Handshake", "DNS & HTTP/3", "TLS Security"))
                )
            )
        )
    }

    companion object {
        val instance by lazy { SpacedRepetitionRepository() }
    }
}
