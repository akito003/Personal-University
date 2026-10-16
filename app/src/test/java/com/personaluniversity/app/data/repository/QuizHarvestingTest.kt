package com.personaluniversity.app.data.repository

import com.personaluniversity.app.data.model.QuizAnswerResponse
import com.personaluniversity.app.data.model.QuizQuestionDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizHarvestingTest {

    private val quiz = listOf(
        QuizQuestionDto(
            id = 10,
            question = "Which option is correct?",
            options = listOf("Distractor A", "Verified answer", "Distractor C")
        ),
        QuizQuestionDto(
            id = 11,
            question = "This question has not been answered",
            options = listOf("Option A", "Option B")
        ),
        QuizQuestionDto(
            id = 12,
            question = "This response is malformed",
            options = listOf("Option A", "Option B")
        )
    )

    @Test
    fun harvestUsesBackendCorrectAnswerAndIgnoresUnansweredQuestions() {
        val cards = buildVerifiedRecallUnits(
            courseId = 4,
            lessonId = 7,
            topic = "Networking",
            quiz = quiz,
            verifiedAnswers = mapOf(
                10 to QuizAnswerResponse(
                    correct = false,
                    correctAnswer = "Verified answer",
                    explanation = "The backend confirmed this answer."
                )
            ),
            nowEpochMs = 1234L
        )

        assertEquals(1, cards.size)
        assertEquals("lesson_7_q_10", cards.single().id)
        assertEquals("Verified answer", cards.single().answer)
        assertEquals("The backend confirmed this answer.", cards.single().explanation)
        assertEquals(4, cards.single().sourceCourseId)
        assertEquals(7, cards.single().sourceLessonId)
        assertEquals(1234L, cards.single().nextReviewEpochMs)
    }

    @Test
    fun harvestIgnoresBlankVerifiedAnswers() {
        val cards = buildVerifiedRecallUnits(
            courseId = 4,
            lessonId = 7,
            topic = "Networking",
            quiz = quiz,
            verifiedAnswers = mapOf(
                10 to QuizAnswerResponse(
                    correct = false,
                    correctAnswer = "   ",
                    explanation = null
                )
            ),
            nowEpochMs = 1234L
        )

        assertTrue(cards.isEmpty())
    }

    @Test
    fun harvestPreservesQuizOrderAndOnlyIncludesAnsweredQuestions() {
        val cards = buildVerifiedRecallUnits(
            courseId = 4,
            lessonId = 7,
            topic = "Networking",
            quiz = quiz,
            verifiedAnswers = mapOf(
                12 to QuizAnswerResponse(true, "Answer C", null),
                10 to QuizAnswerResponse(true, "Answer A", null)
            ),
            nowEpochMs = 1234L
        )

        assertEquals(listOf("lesson_7_q_10", "lesson_7_q_12"), cards.map { it.id })
        assertEquals(listOf("Answer A", "Answer C"), cards.map { it.answer })
    }
}
