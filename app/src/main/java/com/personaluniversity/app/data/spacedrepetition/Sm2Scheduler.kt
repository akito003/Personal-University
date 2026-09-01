package com.personaluniversity.app.data.spacedrepetition

import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Recall rating corresponding to user self-assessment.
 * Matches Kaizen SRS FR-3.2 (Again / Hard / Good / Easy).
 */
enum class RecallRating(val grade: Int) {
    AGAIN(1),  // Complete blackout, concept not recognized
    HARD(3),   // Recalled with significant effort or partially wrong
    GOOD(4),   // Correct response with normal hesitation
    EASY(5)    // Effortless, instant recall
}

/**
 * State representing a unit's SM-2 learning progress.
 */
data class Sm2State(
    val easeFactor: Double = DEFAULT_EASE_FACTOR,
    val intervalDays: Int = 0,
    val repetitions: Int = 0,
    val nextReviewEpochMs: Long = System.currentTimeMillis()
) {
    companion object {
        const val MIN_EASE_FACTOR = 1.3
        const val DEFAULT_EASE_FACTOR = 2.5
        const val MS_PER_DAY = 86_400_000L
    }
}

object Sm2Scheduler {

    /**
     * Calculates the next SM-2 state after a review.
     * Implements standard SuperMemo-2 formula with minimum ease factor clamp.
     */
    fun schedule(currentState: Sm2State, rating: RecallRating, nowEpochMs: Long = System.currentTimeMillis()): Sm2State {
        val q = rating.grade
        val currentEf = currentState.easeFactor

        // Calculate new Ease Factor:
        // EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        val delta = 0.1 - (5 - q) * (0.08 + (5 - q) * 0.02)
        val newEf = max(Sm2State.MIN_EASE_FACTOR, currentEf + delta)

        val newRepetitions: Int
        val newIntervalDays: Int

        if (q < 3) {
            // Failed recall (Again) - reset repetitions and review tomorrow
            newRepetitions = 0
            newIntervalDays = 1
        } else {
            // Successful recall
            newRepetitions = currentState.repetitions + 1
            newIntervalDays = when (currentState.repetitions) {
                0 -> 1
                1 -> 6
                else -> {
                    val multiplier = if (rating == RecallRating.EASY) newEf * 1.3 else newEf
                    val calculated = (currentState.intervalDays * multiplier).roundToInt()
                    max(currentState.intervalDays + 1, calculated)
                }
            }
        }

        val nextReviewMs = nowEpochMs + (newIntervalDays * Sm2State.MS_PER_DAY)

        return Sm2State(
            easeFactor = (newEf * 100.0).roundToInt() / 100.0, // round to 2 decimals
            intervalDays = newIntervalDays,
            repetitions = newRepetitions,
            nextReviewEpochMs = nextReviewMs
        )
    }

    /**
     * Filters and prioritizes today's review queue with anti-burnout protection (SRS FR-3.3, FR-4.1, FR-4.2).
     * Caches backlog to prevent overload if the student missed several days.
     */
    fun <T> buildDailyQueue(
        dueUnits: List<T>,
        newUnits: List<T>,
        maxDueQuota: Int = 20,
        maxNewQuota: Int = 5
    ): List<T> {
        val cappedDue = dueUnits.take(maxDueQuota)
        val cappedNew = newUnits.take(maxNewQuota)
        return cappedDue + cappedNew
    }
}
