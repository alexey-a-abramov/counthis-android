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
     * - Uniformly randomizes correct answer position (including extremes)
     *
     * Strategy for position distribution:
     * - Randomly chooses whether correct should be: minimum, maximum, or middle
     * - Generates offsets accordingly to achieve the target position
     * - This ensures correct answer appears at extremes, not just middle values
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

        // For very small numbers (1-5), use simpler algorithm without position forcing
        // as certain positions may be mathematically impossible
        if (correct <= 5) {
            return generateOptionsSimple(correct, numOptions, maxAttempts)
        }

        // Adaptive base offset: 25% with minimum of 4 to avoid sequential runs
        val baseOffset = maxOf(4, (correct * 0.25).toInt())

        // Randomly choose target position - this is our primary goal
        val primaryPosition = Random.nextInt(numOptions)

        // Try hard to achieve the primary position (80% of attempts)
        val primaryAttempts = (maxAttempts * 0.8).toInt()
        val result = tryGenerateWithPosition(
            correct, numOptions, baseOffset, primaryPosition, primaryAttempts
        )
        if (result != null) return result

        // If primary position fails, try others as fallback (20% of attempts)
        val fallbackPositions = (0 until numOptions).filter { it != primaryPosition }.shuffled(Random.Default)
        val fallbackAttemptsPerPosition = (maxAttempts * 0.2).toInt() / (numOptions - 1)

        for (targetPosition in fallbackPositions) {
            val fallbackResult = tryGenerateWithPosition(
                correct, numOptions, baseOffset, targetPosition, fallbackAttemptsPerPosition
            )
            if (fallbackResult != null) return fallbackResult
        }

        throw IllegalStateException(
            "Could not generate valid options for correct=$correct after trying all positions"
        )
    }

    /**
     * Simplified generation for very small numbers where position forcing may be impossible.
     */
    private fun generateOptionsSimple(
        correct: Int,
        numOptions: Int,
        maxAttempts: Int
    ): List<Int> {
        val baseOffset = 4 // Fixed offset for small numbers

        for (attempt in 0 until maxAttempts) {
            val maxOffset = baseOffset + (attempt / 5)

            // Build pool of all possible offsets
            val possibleOffsets = (-maxOffset..maxOffset)
                .filter { o -> o != 0 && (correct + o) >= 1 }
                .toList()

            if (possibleOffsets.size < numOptions - 1) continue

            // Sample offsets randomly
            val selectedOffsets = possibleOffsets.shuffled(Random.Default).take(numOptions - 1)
            val options = (selectedOffsets.map { correct + it } + correct).sorted()

            // Validation 1: Reject consecutive sequences
            val spread = options.last() - options.first()
            if (spread <= numOptions - 1) continue

            // Validation 2: Ensure max/min ratio >= 1.4
            val ratio = options.last().toDouble() / options.first().toDouble()
            if (ratio < 1.4) continue

            // Validation 3: Ensure minimum difference of 2
            var minDiff = Int.MAX_VALUE
            for (i in 0 until options.size - 1) {
                minDiff = minOf(minDiff, options[i + 1] - options[i])
            }
            if (minDiff < 2) continue

            // Shuffle and return
            return options.shuffled(Random.Default)
        }

        throw IllegalStateException(
            "Could not generate valid options for correct=$correct"
        )
    }

    private fun tryGenerateWithPosition(
        correct: Int,
        numOptions: Int,
        baseOffset: Int,
        targetPosition: Int,
        maxAttempts: Int
    ): List<Int>? {
        for (attempt in 0 until maxAttempts) {
            // Gradually expand range if stuck - more aggressive for small numbers
            val expansionRate = if (correct <= 10) 5 else 10
            val maxOffset = baseOffset + (attempt / expansionRate)

            // Build offset pools based on target position
            val positiveOffsets = (2..maxOffset).filter { correct + it >= 1 }.toList()
            val negativeOffsets = (-maxOffset..-2).filter { correct + it >= 1 }.toList()

            // Generate offsets based on where we want the correct answer
            val selectedOffsets = when (targetPosition) {
                0 -> {
                    // Correct should be MINIMUM: use only positive offsets
                    if (positiveOffsets.size < numOptions - 1) continue
                    positiveOffsets.shuffled(Random.Default).take(numOptions - 1)
                }
                numOptions - 1 -> {
                    // Correct should be MAXIMUM: use only negative offsets
                    if (negativeOffsets.size < numOptions - 1) continue
                    negativeOffsets.shuffled(Random.Default).take(numOptions - 1)
                }
                else -> {
                    // Correct should be in MIDDLE: use mixed offsets
                    // Need at least targetPosition items below and (numOptions-1-targetPosition) above
                    val needBelow = targetPosition
                    val needAbove = numOptions - 1 - targetPosition

                    if (negativeOffsets.size < needBelow || positiveOffsets.size < needAbove) continue

                    val below = negativeOffsets.shuffled(Random.Default).take(needBelow)
                    val above = positiveOffsets.shuffled(Random.Default).take(needAbove)
                    below + above
                }
            }

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

            // Validation 4: Verify correct answer is at target position
            if (options.indexOf(correct) != targetPosition) continue

            // Shuffle to randomize display order (but position in sorted order is controlled)
            return options.shuffled(Random.Default)
        }

        return null
    }
}
