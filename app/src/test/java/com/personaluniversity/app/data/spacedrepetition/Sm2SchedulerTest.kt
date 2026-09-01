package com.personaluniversity.app.data.spacedrepetition

import org.junit.Assert.*
import org.junit.Test

class Sm2SchedulerTest {

    @Test
    fun testFirstReviewGood() {
        val initial = Sm2State()
        val next = Sm2Scheduler.schedule(initial, RecallRating.GOOD, nowEpochMs = 1_000_000L)

        assertEquals(1, next.repetitions)
        assertEquals(1, next.intervalDays)
        // EF for Good (grade 4): delta = 0.1 - (1) * (0.08 + 0.02) = 0.0 -> remains 2.50
        assertEquals(2.50, next.easeFactor, 0.01)
        assertEquals(1_000_000L + 86_400_000L, next.nextReviewEpochMs)
    }

    @Test
    fun testSecondReviewGood() {
        val first = Sm2State(easeFactor = 2.5, intervalDays = 1, repetitions = 1)
        val second = Sm2Scheduler.schedule(first, RecallRating.GOOD)

        assertEquals(2, second.repetitions)
        assertEquals(6, second.intervalDays)
    }

    @Test
    fun testThirdReviewGood() {
        val second = Sm2State(easeFactor = 2.5, intervalDays = 6, repetitions = 2)
        val third = Sm2Scheduler.schedule(second, RecallRating.GOOD)

        assertEquals(3, third.repetitions)
        // 6 * 2.5 = 15 days
        assertEquals(15, third.intervalDays)
    }

    @Test
    fun testFailedRecallResetsRepetitionsAndSchedulesTomorrow() {
        val advanced = Sm2State(easeFactor = 2.6, intervalDays = 30, repetitions = 5)
        val afterFailure = Sm2Scheduler.schedule(advanced, RecallRating.AGAIN)

        assertEquals(0, afterFailure.repetitions)
        assertEquals(1, afterFailure.intervalDays)
        // Ease factor decreases on failure
        assertTrue(afterFailure.easeFactor < 2.6)
    }

    @Test
    fun testEaseFactorDoesNotDropBelowMinimum() {
        var state = Sm2State(easeFactor = 1.35)
        repeat(5) {
            state = Sm2Scheduler.schedule(state, RecallRating.AGAIN)
        }
        assertEquals(Sm2State.MIN_EASE_FACTOR, state.easeFactor, 0.001)
    }

    @Test
    fun testAntiBurnoutQueueCapping() {
        val dueCards = (1..50).map { "Due Card $it" }
        val newCards = (1..20).map { "New Card $it" }

        val queue = Sm2Scheduler.buildDailyQueue(dueCards, newCards, maxDueQuota = 15, maxNewQuota = 5)

        assertEquals(20, queue.size)
        assertEquals("Due Card 1", queue.first())
        assertEquals("New Card 5", queue.last())
    }
}
