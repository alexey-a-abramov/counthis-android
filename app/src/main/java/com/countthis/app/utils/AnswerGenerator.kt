package com.countthis.app.utils

import kotlin.random.Random

/**
 * Generates answer options for counting exercises using an adaptive algorithm
 * that prevents sequential runs and ensures proper distribution.
 */
object AnswerGenerator {

    /**
     * Generates answer options for counting exercises.
     *
     * Algorithm requirements:
     * - Prevents consecutive sequences like [5, 6, 7, 8]
     * - Ensures max/min ratio >= 1.4 for intuitive differences
     * - Minimum difference of 2 between any two options
     * - Uniformly randomizes correct answer position
     *
     * @param correct The true count of objects shown
     * @param numOptions Total number of answer choices (default 4)
     * @param maxAttempts Maximum retry attempts (default 200)
     * @return List of shuffled answer options including the correct answer
     */
    fun generateOptions(
        correct: Int,
        numOptions: Int = 4,
        maxAttempts: Int = 200
    ): List<Int> {
        require(correct >= 1) { "Correct value must be >= 1" }
        require(numOptions >= 2) { "Must have at least 2 options" }

        // Adaptive base offset: 25% with minimum of 4 to avoid sequential runs
        val baseOffset = maxOf(4, (correct * 0.25).toInt())

        for (attempt in 0 until maxAttempts) {
            // Gradually expand range if stuck
            val maxOffset = baseOffset + (attempt / 30)

            // Build pool of possible offsets (excluding 0 and values that would be < 1)
            val possibleOffsets = (-maxOffset..maxOffset)
                .filter { o -> o != 0 && (correct + o) >= 1 }
                .toList()

            if (possibleOffsets.size < numOptions - 1) continue

            // Sample offsets and build options
            val selectedOffsets = possibleOffsets.shuffled(Random.Default).take(numOptions - 1)
            val options = (selectedOffsets.map { correct + it } + correct).sorted()

            // Validation 1: Reject consecutive sequences (e.g. [5,6,7,8])
            // A run is consecutive iff spread == numOptions - 1
            val spread = options.last() - options.first()
            if (spread <= numOptions - 1) continue

            // Validation 2: Ensure max/min ratio >= 1.4 for intuitive differences
            val ratio = options.last().toDouble() / options.first().toDouble()
            if (ratio < 1.4) continue

            // Validation 3: Ensure minimum difference of 2 between adjacent options
            var minDiff = Int.MAX_VALUE
            for (i in 0 until options.size - 1) {
                minDiff = minOf(minDiff, options[i + 1] - options[i])
            }
            if (minDiff < 2) continue

            // Shuffle to randomize position of correct answer
            return options.shuffled(Random.Default)
        }

        throw IllegalStateException(
            "Could not generate valid options for correct=$correct after $maxAttempts attempts"
        )
    }
}
